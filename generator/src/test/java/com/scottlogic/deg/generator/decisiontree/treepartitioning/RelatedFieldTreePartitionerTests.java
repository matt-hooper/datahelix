package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.generator.inputs.RuleInformation;
import com.scottlogic.deg.generator.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.*;
import com.scottlogic.deg.generator.decisiontree.testutils.*;
import com.scottlogic.deg.generator.decisiontree.testutils.EqualityComparer;
import com.scottlogic.deg.schemas.v0_1.RuleDTO;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RelatedFieldTreePartitionerTests {
    private static final TreeConstraintNode emptyConstraint
        = new TreeConstraintNode(Collections.emptySet(), Collections.emptySet());

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
                emptyConstraint));
    }

    @Test
    void shouldNotErrorIfNoFieldsConstrained() {
        givenTree(
            tree(fields("A", "B", "C"),
                emptyConstraint));

        expectTrees(
            tree(fields("A"), emptyConstraint),
            tree(fields("B"), emptyConstraint),
            tree(fields("C"), emptyConstraint));
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

    private AtomicConstraint atomicConstraint(String fieldName) {
        AtomicConstraint constraint = this.constraints.get(fieldName);

        if (constraint == null) {
            constraint = new IsInSetConstraint(new Field(fieldName), Collections.singleton("sample-value"), rules());
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

    private Map<String, AtomicConstraint> constraints;
    private List<DecisionTree> partitionedTrees;
    private DecisionTree decisionTree;

    private void givenTree(DecisionTree decisionTree) {
        this.decisionTree = decisionTree;
    }

    private void partitionTrees() {
        partitionedTrees = new RelatedFieldTreePartitioner(null)
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

    private static Set<RuleInformation> rules(){
        RuleDTO rule = new RuleDTO();
        rule.rule = "rules";
        return Collections.singleton(new RuleInformation(rule));
    }
}
