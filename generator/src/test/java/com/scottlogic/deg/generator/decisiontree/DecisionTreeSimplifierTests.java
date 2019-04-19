package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.generator.constraints.atomic.IsNullConstraint;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;

class DecisionTreeSimplifierTests {
    // TODO: Simplifier tests needs fleshing out

    @Test
    void simplify_decisionContainsSingleOptiontWithMatchingConstraintOnRootNode_doesNotSimplifyTree() {
        DecisionTree tree = new DecisionTree(
            new TreeConstraintNode(
                Arrays.asList(
                    new IsInSetConstraint(new Field("Field 1"), new HashSet<Object>() {{ add(1); add(2); }}, null),
                    new IsNullConstraint(new Field("Field 1"), null).negate()
                ),
                Collections.singletonList(
                    new TreeDecisionNode(
                        Collections.singletonList(
                            new TreeConstraintNode(
                                Collections.singletonList(
                                    new IsInSetConstraint(new Field("Field 1"), new HashSet<Object>() {{
                                        add(1);
                                        add(2);
                                    }}, null)
                                ),
                                Collections.emptyList()
                            )
                        )
                    )
                )
            ),
            new ProfileFields(
                new ArrayList<Field>() {{ add(new Field("Field 1")); }}
            ),
            ""
        );
        DecisionTreeSimplifier simplifier = new DecisionTreeSimplifier();

        final ConstraintNode result = simplifier.simplify(tree.rootNode);

        Assert.assertEquals(result.getAtomicConstraints(), tree.getRootNode().getAtomicConstraints());
        Assert.assertTrue(result.getDecisions().isEmpty());
    }

    @Test
    void simplify_decisionContainsSingleOptionWithDifferingConstraintOnRootNode_simplifiesDecision() {
        DecisionTree tree = new DecisionTree(
            new TreeConstraintNode(
                Arrays.asList(
                    new IsInSetConstraint(new Field("Field 1"), new HashSet<Object>() {{ add(1); add(2); }}, null),
                    new IsNullConstraint(new Field("Field 1"), null).negate()
                ),
                Collections.singletonList(
                    new TreeDecisionNode(
                        Collections.singletonList(
                            new TreeConstraintNode(
                                Collections.singletonList(
                                    new IsInSetConstraint(new Field("Field 2"), new HashSet<Object>() {{
                                        add("A");
                                        add("B");
                                    }}, null)
                                ),
                                Collections.emptyList()
                            )
                        )
                    )
                )
            ),
            new ProfileFields(
                new ArrayList<Field>() {{ add(new Field("Field 1")); }}
            ),
            ""
        );
        DecisionTreeSimplifier simplifier = new DecisionTreeSimplifier();

        final ConstraintNode result = simplifier.simplify(tree.rootNode);

        final List<AtomicConstraint> expectedConstraints = Arrays.asList(
            new IsInSetConstraint(new Field("Field 1"), new HashSet<Object>() {{
                add(1);
                add(2);
            }}, null),
            new IsNullConstraint(new Field("Field 1"), null).negate(),
            new IsInSetConstraint(new Field("Field 2"), new HashSet<Object>() {{
                add("A");
                add("B");
            }}, null)
        );
        Assert.assertTrue(result.getAtomicConstraints().containsAll(expectedConstraints));
        Assert.assertTrue(result.getDecisions().isEmpty());
    }
}
