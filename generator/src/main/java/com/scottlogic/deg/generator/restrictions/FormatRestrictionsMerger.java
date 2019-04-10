package com.scottlogic.deg.generator.restrictions;

public class FormatRestrictionsMerger {
    public FormatRestrictions merge(FormatRestrictions left, FormatRestrictions right){
        // We can't merge formats yet
        return left != null ? left : right;
    }
}
