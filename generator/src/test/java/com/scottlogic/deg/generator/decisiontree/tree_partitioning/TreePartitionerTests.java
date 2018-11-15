package com.scottlogic.deg.generator.decisiontree.tree_partitioning;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.constraints.IConstraint;
import com.scottlogic.deg.generator.constraints.IsEqualToConstantConstraint;
import com.scottlogic.deg.generator.decisiontree.*;
import com.scottlogic.deg.generator.decisiontree.tree_partitioning.test_utils.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TreePartitionerTests {
    @Test
    void shouldSplitTreeIntoPartitions() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    )
        )));

        expectTrees(
            tree(fields("A", "B"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B")
                    ))),
            tree(fields("C", "D"),
                constraint(
                    decision(
                        constraint("C"),
                        constraint("D")
                    ))),
            tree(fields("E", "F"),
                constraint(
                    decision(
                        constraint("E"),
                        constraint("F")
                    ))
        ));
    }

    @Test
    void shouldPartitionTwoNodesCorrectly() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("E")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    )
        )));

        expectTrees(
            tree(fields("A", "B", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    ))),
            tree(fields("C", "D"),
                constraint(
                    decision(
                        constraint("C"),
                        constraint("D")
                    ))
            ));
    }

    @Test
    void shouldNotPartition() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F", "G"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("C")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F"),
                        constraint("G")
                    )
        )));

        expectTrees(
            tree(fields("A", "B", "C", "D", "E", "F", "G"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("C")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F"),
                        constraint("G")
                    )
                )
            ));
    }

    @Test
    void shouldPartitionConstraintsCorrectly() {
        givenTree(
            tree(fields("A", "B", "C"),
                constraint(
                    new String[] {"A", "B", "C"},
                    decision(constraint("A")),
                    decision(constraint("B")),
                    decision(constraint("C"))
                )
        ));

        expectTrees(
            tree(fields("A"),
                constraint(
                    new String[] {"A"},
                    decision(constraint("A"))
                )),
            tree(fields("B"),
                constraint(
                    new String[] {"B"},
                    decision(constraint("B"))
                )),
            tree(fields("C"),
                constraint(
                    new String[] {"C"},
                    decision(constraint("C"))
                ))
        );
    }

    @Test
    void shouldNotErrorIfFieldsNotConstrained() {
        givenTree(
            tree(fields("A", "B"),
                constraint("A")));

        expectTrees(
            tree(fields("A"),
                constraint("A")),
            tree(fields("B"),
                new TreeConstraintNode()));
    }

    @Test
    void shouldNotErrorIfNoFieldsConstrained() {
        givenTree(
            tree(fields("A", "B", "C"),
                new TreeConstraintNode()));

        expectTrees(
            tree(fields("A"), new TreeConstraintNode()),
            tree(fields("B"), new TreeConstraintNode()),
            tree(fields("C"), new TreeConstraintNode()));
    }

    private ConstraintNode constraint(String... fieldNames) {
        return constraint(fieldNames, new DecisionNode[0]);
    }

    private ConstraintNode constraint(DecisionNode... decisions) {
        return constraint(new String[0], decisions);
    }

    private ConstraintNode constraint(String[] fieldNames, DecisionNode... decisions) {
        return new TreeConstraintNode(
            Stream.of(fieldNames)
                .map(this::atomicConstraint)
                .collect(Collectors.toList()),
            Arrays.asList(decisions));
    }

    private IConstraint atomicConstraint(String fieldName) {
        IConstraint constraint = this.constraints.get(fieldName);

        if (constraint == null) {
            constraint = new IsEqualToConstantConstraint(new Field(fieldName), "sample-value");
            this.constraints.put(fieldName, constraint);
        }

        return constraint;
    }

    private DecisionNode decision(ConstraintNode... constraints) {
        return new TreeDecisionNode(constraints);
    }

    private ProfileFields fields(String... fieldNames) {
        return new ProfileFields(
            Stream.of(fieldNames)
                .map(Field::new)
                .collect(Collectors.toList()));
    }

    private DecisionTree tree(ProfileFields fields, ConstraintNode rootNode) {
        return new DecisionTree(rootNode, fields, "Decision Tree");
    }

    @BeforeEach
    void beforeEach() {
        constraints = new HashMap<>();
        decisionTree = null;
        partitionedTrees = null;
    }

    private Map<String, IConstraint> constraints;
    private List<DecisionTree> partitionedTrees;
    private DecisionTree decisionTree;

    private void givenTree(DecisionTree decisionTree) {
        this.decisionTree = decisionTree;
    }

    private void partitionTrees() {
        partitionedTrees = new TreePartitioner()
            .splitTreeIntoPartitions(decisionTree)
            .collect(Collectors.toList());
    }
    private void expectTrees(DecisionTree... decisionTrees) {
        if (partitionedTrees == null)
            partitionTrees();

        TreeComparisonReporter reporter = new TreeComparisonReporter();
        TreeComparisonContext context = new TreeComparisonContext();
        AnyOrderCollectionEqualityComparer defaultAnyOrderCollectionEqualityComparer = new AnyOrderCollectionEqualityComparer();
        EqualityComparer anyOrderComparer = new AnyOrderCollectionEqualityComparer(
            new TreeComparer(
                new ConstraintNodeComparer(
                    context,
                    defaultAnyOrderCollectionEqualityComparer,
                    new DecisionComparer(),
                    defaultAnyOrderCollectionEqualityComparer,
                    new AnyOrderCollectionEqualityComparer(new DecisionComparer())),
                new ProfileFieldComparer(context, defaultAnyOrderCollectionEqualityComparer, defaultAnyOrderCollectionEqualityComparer),
                context
            )
        );

        boolean match = anyOrderComparer.equals(
            partitionedTrees,
            Arrays.asList(decisionTrees));

        if (!match) {
            reporter.reportMessages(context);
            Assert.fail("Trees do not match");
        }
    }
}