package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.Rule;
import com.scottlogic.deg.generator.constraints.*;
import com.scottlogic.deg.generator.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.constraints.grammatical.*;

import java.util.*;
import java.util.stream.Collectors;

public class ProfileDecisionTreeFactory implements DecisionTreeFactory {
    private final DecisionTreeSimplifier decisionTreeSimplifier = new DecisionTreeSimplifier();

    @Override
    public DecisionTree createTree(Profile profile) {
        Iterator<ConstraintNode> nodes = profile.rules.stream()
            .map(this::convertRule)
            .map(decisionTreeSimplifier::simplify)
            .iterator();

        return new DecisionTree(ConstraintNode.merge(nodes), profile.fields, profile.description);
    }

    private ConstraintNode convertRule(Rule rule) {
        return convertAndConstraint(new AndConstraint(rule.constraints));
    }

    private ConstraintNode convertConstraint(Constraint constraintToConvert) {
        if (constraintToConvert instanceof NegatedGrammaticalConstraint) {
            return convertNegatedConstraint(constraintToConvert);
        }
        else if (constraintToConvert instanceof AndConstraint) {
            return convertAndConstraint((AndConstraint) constraintToConvert);
        }
        else if (constraintToConvert instanceof OrConstraint) {
            return convertOrConstraint((OrConstraint) constraintToConvert);
        } else if (constraintToConvert instanceof ConditionalConstraint) {
            return convertConditionalConstraint((ConditionalConstraint) constraintToConvert);
        } else {
            AtomicConstraint atomicConstraint = (AtomicConstraint) constraintToConvert;
            return asConstraintNode(atomicConstraint);
        }
    }

    private ConstraintNode convertNegatedConstraint(Object constraintToConvert) {
        Constraint negatedConstraint = ((NegatedGrammaticalConstraint) constraintToConvert).negatedConstraint;

        // ¬AND(X, Y, Z) reduces to OR(¬X, ¬Y, ¬Z)
        if (negatedConstraint instanceof AndConstraint) {
            Collection<Constraint> subConstraints = ((AndConstraint) negatedConstraint).subConstraints;

            return convertOrConstraint(
                new OrConstraint(negateEach(subConstraints)));
        }
        // ¬OR(X, Y, Z) reduces to AND(¬X, ¬Y, ¬Z)
        else if (negatedConstraint instanceof OrConstraint) {
            Collection<Constraint> subConstraints = ((OrConstraint) negatedConstraint).subConstraints;

            return convertAndConstraint(
                new AndConstraint(negateEach(subConstraints)));
        }
        // ¬IF(X, then: Y) reduces to AND(X, ¬Y)
        // ¬IF(X, then: Y, else: Z) reduces to OR(AND(X, ¬Y), AND(¬X, ¬Z))
        else if (negatedConstraint instanceof ConditionalConstraint) {
            ConditionalConstraint conditional = (ConditionalConstraint) negatedConstraint;

            AndConstraint positiveNegation =
                new AndConstraint(conditional.condition, conditional.whenConditionIsTrue.negate());

            if (conditional.whenConditionIsFalse == null) {
                return convertAndConstraint(positiveNegation);
            }

            Constraint negativeNegation =
                new AndConstraint(conditional.condition.negate(), conditional.whenConditionIsFalse.negate());

            return convertOrConstraint(
                new OrConstraint(positiveNegation, negativeNegation));

        }
        // if we got this far, it must be an atomic constraint
        else {
            AtomicConstraint atomicConstraint = (AtomicConstraint) constraintToConvert;
            return asConstraintNode(atomicConstraint);
        }
    }

    private ConstraintNode convertAndConstraint(AndConstraint constraintToConvert) {
        // AND(X, Y, Z) becomes a flattened list of constraint nodes
        Collection<Constraint> subConstraints = constraintToConvert.subConstraints;

        Iterator<ConstraintNode> iterator = subConstraints.stream().map(this::convertConstraint)
            .iterator();

        return ConstraintNode.merge(iterator);
    }

    private ConstraintNode convertOrConstraint(OrConstraint constraintToConvert) {
        // OR(X, Y, Z) becomes a decision node
        Collection<Constraint> subConstraints = constraintToConvert.subConstraints;

        List<ConstraintNode> options = subConstraints.stream()
            .map(this::convertConstraint)
            .collect(Collectors.toList());

        return asConstraintNode(new TreeDecisionNode(options));
    }

    private ConstraintNode convertConditionalConstraint(ConditionalConstraint constraintToConvert) {
        Constraint ifConstraint = constraintToConvert.condition;
        Constraint thenConstraint = constraintToConvert.whenConditionIsTrue;
        Constraint elseConstraint = constraintToConvert.whenConditionIsFalse;

        OrConstraint convertedConstraint = new OrConstraint(
            new AndConstraint(ifConstraint, thenConstraint),
            elseConstraint == null ? ifConstraint.negate() : new AndConstraint(ifConstraint.negate(), elseConstraint));

        return convertOrConstraint(convertedConstraint);
    }

    private static Collection<Constraint> negateEach(Collection<Constraint> constraints) {
        return constraints.stream()
            .map(Constraint::negate)
            .collect(Collectors.toList());
    }

    private static ConstraintNode asConstraintNode(AtomicConstraint constraint) {
        return new TreeConstraintNode(
            Collections.singleton(constraint),
            Collections.emptyList());
    }

    private static ConstraintNode asConstraintNode(DecisionNode decision) {
        return new TreeConstraintNode(
            Collections.emptyList(),
            Collections.singleton(decision));
    }
}

