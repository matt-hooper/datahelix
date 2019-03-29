package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.FlatMappingSpliterator;
import com.scottlogic.deg.generator.constraints.atomic.IsOfTypeConstraint;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.*;
import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.DateTimeFieldValueSource;
import com.scottlogic.deg.generator.restrictions.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardFieldValueSourceEvaluator implements FieldValueSourceEvaluator {
    private static final CannedValuesFieldValueSource nullOnlySource = new CannedValuesFieldValueSource(Collections.singletonList(null));

    public List<FieldValueSource> getFieldValueSources(FieldSpec fieldSpec){

        if (mustBeNull(fieldSpec)){
            return Collections.singletonList(nullOnlySource);
        }

        if (fieldSpec.getSetRestrictions() != null && fieldSpec.getSetRestrictions().getWhitelist() != null) {
            return getSetRestrictionSources(fieldSpec);
        }

        List<FieldValueSource> validSources = new ArrayList<>();

        if (fieldSpec.getMustContainRestriction() != null && !fieldSpec.getMustContainRestriction().getRequiredObjects().isEmpty()) {
            List<FieldValueSource> mustContainRestrictionSources = getMustContainRestrictionSources(fieldSpec);
            if (!mustContainRestrictionSources.isEmpty()){
                return mustContainRestrictionSources;
            }
        }

        TypeRestrictions typeRestrictions = fieldSpec.getTypeRestrictions() != null
            ? fieldSpec.getTypeRestrictions()
            : DataTypeRestrictions.ALL_TYPES_PERMITTED;

        if (typeRestrictions.isTypeAllowed(IsOfTypeConstraint.Types.NUMERIC)) {
            validSources.add(getNumericSource(fieldSpec));
        }

        if (typeRestrictions.isTypeAllowed(IsOfTypeConstraint.Types.STRING)) {
            validSources.add(getStringSource(fieldSpec));
        }

        if (typeRestrictions.isTypeAllowed(IsOfTypeConstraint.Types.DATETIME)) {
            validSources.add(getDateTimeSource(fieldSpec));
        }

        if (mayBeNull(fieldSpec)){
            validSources.add(nullOnlySource);
        }

        return validSources;
    }

    private boolean mustBeNull(FieldSpec fieldSpec) {
        return fieldSpec.getNullRestrictions() != null
            && fieldSpec.getNullRestrictions().nullness == Nullness.MUST_BE_NULL;
    }

    private List<FieldValueSource> getSetRestrictionSources(FieldSpec fieldSpec) {
        Stream<Object> whitelist = fieldSpec.getSetRestrictions().getWhitelist().stream();

        if (mayBeNull(fieldSpec)) {
            return Arrays.asList(
                new CannedValuesFieldValueSource(whitelist.collect(Collectors.toList())),
                nullOnlySource);
        }

        return Collections.singletonList(new CannedValuesFieldValueSource(whitelist.collect(Collectors.toList())));
    }

    private boolean mayBeNull(FieldSpec fieldSpec) {
        return fieldSpec.getNullRestrictions() == null;
    }

    private List<FieldValueSource> getMustContainRestrictionSources(FieldSpec fieldSpec) {
        Set<FieldSpec> mustContainRestrictionFieldSpecs = fieldSpec.getMustContainRestriction().getRequiredObjects();

        return FlatMappingSpliterator.flatMap(mustContainRestrictionFieldSpecs.stream()
            .map(this::getFieldValueSources),
            List::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    private FieldValueSource getNumericSource(FieldSpec fieldSpec) {
        NumericRestrictions restrictions = fieldSpec.getNumericRestrictions() == null
            ? new NumericRestrictions()
            : fieldSpec.getNumericRestrictions();

        int numericScale = getNumericScale(fieldSpec.getGranularityRestrictions());

        return new RealNumberFieldValueSource(
            restrictions,
            getBlacklist(fieldSpec),
            numericScale);
    }

    private int getNumericScale(GranularityRestrictions granularityRestrictions) {
        //Note that the default granularity if not specified is 1e-20
        final int maximumGranularity = 20;

        if (granularityRestrictions != null) {
            int granularityScale = granularityRestrictions.getNumericScale();
            return Math.min(granularityScale, maximumGranularity);
        }

        return maximumGranularity;
    }

    private Set<Object> getBlacklist(FieldSpec fieldSpec) {
        if (fieldSpec.getSetRestrictions() == null)
            return Collections.emptySet();

        return new HashSet<>(fieldSpec.getSetRestrictions().getBlacklist());
    }

    private FieldValueSource getStringSource(FieldSpec fieldSpec) {
        StringGenerator generator = getStringGenerator(fieldSpec.getStringRestrictions());

        Set<Object> blacklist = getBlacklist(fieldSpec);
        if (blacklist.isEmpty()) {
            return generator.asFieldValueSource();
        }

        RegexStringGenerator blacklistGenerator = RegexStringGenerator.createFromBlacklist(blacklist);

        return generator.intersect(blacklistGenerator).asFieldValueSource();
    }

    private StringGenerator getStringGenerator(StringRestrictions stringRestrictions) {
        if (stringRestrictions == null || (stringRestrictions.stringGenerator == null)) {
            return anyStringsAllowed();
        }

        return stringRestrictions.stringGenerator;
    }

    private RegexStringGenerator anyStringsAllowed() {
        return new RegexStringGenerator(".{0,280}", true);
    }

    private FieldValueSource getDateTimeSource(FieldSpec fieldSpec) {
        DateTimeRestrictions restrictions = fieldSpec.getDateTimeRestrictions();

        return new DateTimeFieldValueSource(
            restrictions != null ? restrictions : new DateTimeRestrictions(),
            getBlacklist(fieldSpec));
    }
}
