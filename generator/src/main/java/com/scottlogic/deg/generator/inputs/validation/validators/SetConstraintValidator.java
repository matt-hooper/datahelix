package com.scottlogic.deg.generator.inputs.validation.validators;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.restrictions.*;
import com.scottlogic.deg.generator.inputs.validation.*;
import com.scottlogic.deg.generator.inputs.validation.messages.*;
import com.scottlogic.deg.generator.utils.SetUtils;

import java.util.*;

public class SetConstraintValidator implements ConstraintValidatorAlerts {

    public final ValidationType validationType = ValidationType.SET;

    private final List<ValidationAlert> alerts;
    private SetRestrictions currentRestrictions;

    public SetConstraintValidator() {
        alerts = new ArrayList<>();
    }

    public void isInSet(Field field, Set<Object> values) {

        SetRestrictions candidateRestrictions = SetRestrictions.fromWhitelist(values);

        SetRestrictionsMerger merger = new SetRestrictionsMerger();
        MergeResult<SetRestrictions> result = merger.merge(currentRestrictions, candidateRestrictions);

        if (result.successful) {
            currentRestrictions = result.restrictions;
        } else {
            logError(field, new SetConstraintValidationMessages(currentRestrictions.getWhitelist(), values));
        }
    }


    public void mustNotBeInSet(Field field, Set<Object> values) {

        SetRestrictions candidateRestrictions = SetRestrictions.fromBlacklist(values);

        SetRestrictionsMerger merger = new SetRestrictionsMerger();
        MergeResult<SetRestrictions> result = merger.merge(currentRestrictions, candidateRestrictions);

        if (result.successful) {
            currentRestrictions = result.restrictions;
        } else {
            logError(field, new SetConstraintValidationMessages(currentRestrictions.getWhitelist(), values));
        }
    }

    private void logError(Field field, StandardValidationMessages message) {
        alerts.add(new ValidationAlert(
            Criticality.ERROR,
            message,
            validationType,
            field));
    }

    @Override
    public List<ValidationAlert> getAlerts() {
        return alerts;
    }
}
