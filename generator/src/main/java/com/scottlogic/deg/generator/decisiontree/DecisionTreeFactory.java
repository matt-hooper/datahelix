package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.generator.Profile;

public interface DecisionTreeFactory {
    DecisionTree createTree(Profile profile);
}
