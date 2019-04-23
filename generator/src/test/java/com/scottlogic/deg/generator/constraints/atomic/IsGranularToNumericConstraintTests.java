package com.scottlogic.deg.generator.constraints.atomic;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.inputs.RuleInformation;
import com.scottlogic.deg.generator.restrictions.ParsedGranularity;
import com.scottlogic.deg.schemas.v0_1.RuleDTO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;

public class IsGranularToNumericConstraintTests {

    @Test
    public void testConstraintIsEqual() {
        Field field1 = new Field("TestField");
        Field field2 = new Field("TestField");
        IsGranularToNumericConstraint constraint1 = new IsGranularToNumericConstraint(field1, new ParsedGranularity(new BigDecimal(0.1)), rules());
        IsGranularToNumericConstraint constraint2 = new IsGranularToNumericConstraint(field2, new ParsedGranularity(new BigDecimal(0.1)), rules());
        Assert.assertThat(constraint1, equalTo(constraint2));
    }

    @Test
    public void testConstraintIsNotEqualDueToField() {
        Field field1 = new Field("TestField");
        Field field2 = new Field("TestField2");
        IsGranularToNumericConstraint constraint1 = new IsGranularToNumericConstraint(field1, new ParsedGranularity(new BigDecimal(0.1)), rules());
        IsGranularToNumericConstraint constraint2 = new IsGranularToNumericConstraint(field2, new ParsedGranularity(new BigDecimal(0.1)), rules());
        Assert.assertNotEquals(constraint1, constraint2);
    }

    @Test
    public void testConstraintIsNotEqualDueToValue() {
        Field field1 = new Field("TestField");
        Field field2 = new Field("TestField");
        IsGranularToNumericConstraint constraint1 = new IsGranularToNumericConstraint(field1, new ParsedGranularity(new BigDecimal(0.1)), rules());
        IsGranularToNumericConstraint constraint2 = new IsGranularToNumericConstraint(field2, new ParsedGranularity(new BigDecimal(1.0)), rules());
        Assert.assertNotEquals(constraint1, constraint2);
    }

    private static Set<RuleInformation> rules(){
        RuleDTO rule = new RuleDTO();
        rule.rule = "rules";
        return Collections.singleton(new RuleInformation(rule));
    }
}
