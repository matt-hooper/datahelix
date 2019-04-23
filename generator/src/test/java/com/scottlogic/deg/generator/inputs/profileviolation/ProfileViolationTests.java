package com.scottlogic.deg.generator.inputs.profileviolation;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.Rule;
import com.scottlogic.deg.generator.builders.*;
import com.scottlogic.deg.generator.constraints.Constraint;
import com.scottlogic.deg.generator.constraints.atomic.*;
import com.scottlogic.deg.generator.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.generator.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.generator.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.generator.outputs.manifest.ManifestWriter;
import com.scottlogic.deg.generator.restrictions.ParsedGranularity;
import com.scottlogic.deg.generator.violations.ViolatedProfile;
import com.scottlogic.deg.generator.violations.filters.ConstraintTypeViolationFilter;
import com.scottlogic.deg.generator.violations.filters.ViolationFilter;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.deg.generator.inputs.profileviolation.TypeEqualityHelper.assertProfileListsAreEquivalent;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

/**
 * Defines tests for all business logic involved in Profile Violation.
 */
public class ProfileViolationTests {
    private IndividualRuleProfileViolator profileViolator;
    private ArrayList<ViolationFilter> constraintsToNotViolate;

    @Mock private ManifestWriter mockManifestWriter;
    @Mock private Path mockPath;
    
    private Field field1;
    private Field field2;
    private Field field3;
    private Field field4;
    private Field field5;
    private static Field staticField = new Field("static field");

    private ConstraintChainBuilder<Constraint> A;
    private ConstraintChainBuilder<Constraint> B;
    private ConstraintChainBuilder<Constraint> C;
    private ConstraintChainBuilder<Constraint> D;
    private ConstraintChainBuilder<Constraint> E;


    private static Stream<Arguments> allAtomicConstraints() {
        OffsetDateTime sampleDate = OffsetDateTime.of(2019, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        final HashSet<Object> sampleSet = new HashSet<>(Arrays.asList("hello", 10));

        return Stream.of(
            Arguments.of(FormatConstraint.class, "%d"),

            Arguments.of(IsInSetConstraint.class, sampleSet),
            Arguments.of(IsNullConstraint.class, null),
            Arguments.of(IsOfTypeConstraint.class, IsOfTypeConstraint.Types.STRING),
            Arguments.of(MatchesStandardConstraint.class, StandardConstraintTypes.ISIN),

            Arguments.of(ContainsRegexConstraint.class, Pattern.compile("\\w+")),
            Arguments.of(MatchesRegexConstraint.class, Pattern.compile("\\d+")),
            Arguments.of(IsStringLongerThanConstraint.class, 10),
            Arguments.of(IsStringShorterThanConstraint.class, 20),
            Arguments.of(StringHasLengthConstraint.class, 15),

            Arguments.of(IsAfterConstantDateTimeConstraint.class, sampleDate),
            Arguments.of(IsAfterOrEqualToConstantDateTimeConstraint.class, sampleDate.plusDays(1)),
            Arguments.of(IsBeforeConstantDateTimeConstraint.class, sampleDate.minusDays(1)),
            Arguments.of(IsBeforeOrEqualToConstantDateTimeConstraint.class, sampleDate.plusDays(2)),

            Arguments.of(IsGranularToNumericConstraint.class, new ParsedGranularity(BigDecimal.ONE)),
            Arguments.of(IsGreaterThanConstantConstraint.class, 100),
            Arguments.of(IsGreaterThanOrEqualToConstantConstraint.class, 200),
            Arguments.of(IsLessThanConstantConstraint.class, 300),
            Arguments.of(IsLessThanOrEqualToConstantConstraint.class, 400)
        );
    }

    private static Stream<Arguments> nestingConstraints() {
        BaseConstraintBuilder<Constraint> notConstraintBuilder = new SingleConstraintBuilder()
            .withEqualToConstraint(staticField, "hello").negate();

        BaseConstraintBuilder<Constraint> violatedNotConstraintBuilder = new SingleConstraintBuilder()
            .withEqualToConstraint(staticField, "hello").wrapAtomicWithViolate();

        BaseConstraintBuilder<AndConstraint> andConstraintBuilder = new AndBuilder()
            .withGreaterThanConstraint(staticField, 100)
            .withLessThanConstraint(staticField, 200);

        BaseConstraintBuilder<OrConstraint> violatedAndConstraintBuilder = new OrBuilder()
            .withAndConstraint(new AndBuilder()
                .withGreaterThanConstraint(staticField, 100).negate().wrapAtomicWithViolate()
                .withLessThanConstraint(staticField, 200)
            )
            .withAndConstraint(new AndBuilder()
                .withGreaterThanConstraint(staticField, 100)
                .withLessThanConstraint(staticField, 200).negate().wrapAtomicWithViolate()
            );

        BaseConstraintBuilder<OrConstraint> orConstraintBuilder = new OrBuilder()
            .withAfterConstraint(staticField, OffsetDateTime.of(2018, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC))
            .withBeforeConstraint(staticField, OffsetDateTime.of(2019, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC));

        BaseConstraintBuilder<AndConstraint> violatedOrConstraintBuilder = new AndBuilder()
            .withAfterConstraint(staticField, OffsetDateTime.of(2018, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)).negate().wrapAtomicWithViolate()
            .withBeforeConstraint(staticField, OffsetDateTime.of(2019, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)).negate().wrapAtomicWithViolate();

        BaseConstraintBuilder<ConditionalConstraint> ifThenConstraintBuilder = new IfBuilder()
            .withIf(new SingleConstraintBuilder().withOfTypeConstraint(staticField, IsOfTypeConstraint.Types.NUMERIC))
            .withThen(new SingleConstraintBuilder().withInSetConstraint(staticField, new Object[]{10, 100}));

        BaseConstraintBuilder<AndConstraint> violatedIfThenConstraintBuilder = new AndBuilder()
            .withOfTypeConstraint(staticField, IsOfTypeConstraint.Types.NUMERIC)
            .withInSetConstraint(staticField, new Object[]{10, 100}).negate().wrapAtomicWithViolate();

        BaseConstraintBuilder<ConditionalConstraint> ifThenElseConstraintBuilder = new IfBuilder()
            .withIf(new SingleConstraintBuilder().withOfLengthConstraint(staticField, 5))
            .withThen(new SingleConstraintBuilder().withMatchesRegexConstraint(staticField, Pattern.compile("abcde")))
            .withElse(new SingleConstraintBuilder().withContainsRegexConstraint(staticField, Pattern.compile("z")));

        BaseConstraintBuilder<OrConstraint> violatedIfThenElseConstraintBuilder = new OrBuilder()
            .withAndConstraint(new AndBuilder()
                .withOfLengthConstraint(staticField, 5)
                .withMatchesRegexConstraint(staticField, Pattern.compile("abcde")).negate().wrapAtomicWithViolate()
            )
            .withAndConstraint(new AndBuilder()
                .withOfLengthConstraint(staticField, 5).negate().wrapAtomicWithViolate()
                .withContainsRegexConstraint(staticField, Pattern.compile("z")).negate().wrapAtomicWithViolate()
            );

        return Stream.of(
            Arguments.of(notConstraintBuilder, violatedNotConstraintBuilder),
            Arguments.of(andConstraintBuilder, violatedAndConstraintBuilder),
            Arguments.of(orConstraintBuilder, violatedOrConstraintBuilder),
            Arguments.of(ifThenConstraintBuilder, violatedIfThenConstraintBuilder),
            Arguments.of(ifThenElseConstraintBuilder, violatedIfThenElseConstraintBuilder)
        );
    }

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing()
            .when(mockManifestWriter)
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));

        constraintsToNotViolate = new ArrayList<>();
        IndividualConstraintRuleViolator ruleViolator = new IndividualConstraintRuleViolator(constraintsToNotViolate);
        profileViolator = new IndividualRuleProfileViolator(mockManifestWriter, mockPath, ruleViolator);
        field1 = new Field("field1");
        field2 = new Field("field2");
        field3 = new Field("field3");
        field4 = new Field("field4");
        field5 = new Field("field5");


        A = new SingleConstraintBuilder().withEqualToConstraint(field1, "A");
        B = new SingleConstraintBuilder().withGreaterThanConstraint(field2, 100);
        C = new SingleConstraintBuilder().withOfLengthConstraint(field3, 10);
        D = new SingleConstraintBuilder().withOfTypeConstraint(field4, IsOfTypeConstraint.Types.NUMERIC);
        E = new SingleConstraintBuilder().withLessThanConstraint(field5, 200);
    }

    @Test
    public void violate_withNoConstraints_producesViolatedEmptyProfile() throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Empty rule")
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Empty rule profile",
            Collections.singletonList(field1),
            Collections.singletonList(new Pair<>(rule, rule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    @ParameterizedTest
    @MethodSource("allAtomicConstraints")
    public void violate_withLinearProfileSingleRuleSingleConstraint_producesViolatedProfile(
        Class<? extends AtomicConstraint> atomicConstraint, Object value
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Input Rule")
            .withAtomicConstraint(field1, atomicConstraint, value)
            .build();

        Rule violatedRule = new RuleBuilder("Input Rule")
            .withAtomicConstraint(field1, atomicConstraint, value).negate().wrapAtomicWithViolate()
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Input Profile",
            Collections.singletonList(field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    @Test
    public void violate_withFilteredConstraintType_producesViolatedProfile() throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Input Rule")
            .withGreaterThanConstraint(field1, 100)
            .build();

        constraintsToNotViolate.add(new ConstraintTypeViolationFilter(IsGreaterThanConstantConstraint.class));

        TestProfiles testProfiles = createTestProfiles(
            "Input Profile",
            Collections.singletonList(field1),
            Collections.singletonList(new Pair<>(rule, rule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that the violator can take a profile with a single rule with two different types of constraints for two
     * fields and return the correct violated profile.
     */
    @Test
    public void violate_withLinearProfileSingleRule_producesViolatedProfile() throws IOException {
        //Arrange
        Rule rule1 = new RuleBuilder("Rule 1")
            .withLessThanConstraint(field1, 100)
            .withGreaterThanConstraint(field2, 200)
            .build();

         Rule violatedRule1 = new RuleBuilder("Rule 1")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100).negate().wrapAtomicWithViolate()
                    .withGreaterThanConstraint(field2, 200)
                )
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100)
                    .withGreaterThanConstraint(field2, 200).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Profile 1",
            Arrays.asList(field1, field2),
            Collections.singletonList(new Pair<>(rule1, violatedRule1))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that the violator can take a profile with a single rule with two different types of constraints including a
     * not for two fields and return the correct violated profile.
     */
    @Test
    public void violate_withLinearProfileSingleRuleIncludingNot_producesViolatedProfile() throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Rule 1")
            .withLessThanConstraint(field1, 100).negate()
            .withGreaterThanConstraint(field2, 200)
            .build();

        Rule violatedRule = new RuleBuilder("Rule 1")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100).wrapAtomicWithViolate()
                    .withGreaterThanConstraint(field2, 200)
                )
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100).negate()
                    .withGreaterThanConstraint(field2, 200).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested if then else profile",
            Arrays.asList(field1, field2),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that the violator can take a profile of two simple rules and return two violated profiles with the correct
     * negation combinations.
     * Input: Profile with 2 fields foo and bar, 2 single atomic constraint rules affecting foo and bar
     * Output: 2 Profiles, one with rule 1 negated and rule 2 unaffected, one with rule 1 unaffected and rule 2 negated
     */
    @Test
    public void violate_withTwoSimpleRuleProfile_producesTwoViolatedProfiles() throws IOException {
        //Arrange
        Rule rule1 = new RuleBuilder("Rule 1")
            .withLessThanConstraint(field1, 100)
            .withLessThanConstraint(field1, 200)
            .build();

        Rule rule2 = new RuleBuilder("Rule 2")
            .withGreaterThanConstraint(field1, 10)
            .withGreaterThanConstraint(field1, 15)
            .build();

        Rule violatedRule1 = new RuleBuilder("Rule 1")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100).negate().wrapAtomicWithViolate()
                    .withLessThanConstraint(field1, 200)
                )
                .withAndConstraint(new AndBuilder()
                    .withLessThanConstraint(field1, 100)
                    .withLessThanConstraint(field1, 200).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        Rule violatedRule2 = new RuleBuilder("Rule 2")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .withGreaterThanConstraint(field1, 10).negate().wrapAtomicWithViolate()
                    .withGreaterThanConstraint(field1, 15)
                )
                .withAndConstraint(new AndBuilder()
                    .withGreaterThanConstraint(field1, 10)
                    .withGreaterThanConstraint(field1, 15).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Profile 1",
            Collections.singletonList(field1),
            Arrays.asList(new Pair<>(rule1, violatedRule1), new Pair<>(rule2, violatedRule2))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that an if-then constraint nested inside the conditional of another if-then constraint violates as
     * expected.
     * In shorthand where A,B,C are atomic constraints: VIOLATE(IF(IF A THEN B) THEN C) -> (IF A THEN B) AND VIOLATE(C)
     *
     * @throws IOException if the manifest writer fails to write.
     */
    @Test
    public void violate_nestedIfThenInsideIfOfIfThen_producesViolatedProfiles() throws IOException {
        //Arrange
        String ruleName = "Nested if rule";

        Rule rule = new RuleBuilder(ruleName)
            .withIfConstraint(new IfBuilder()
                .withIf(new IfBuilder()
                    .withIf(A)
                    .withThen(B)
                )
                .withThen(C)
            )
            .build();

        Rule violatedRule = new RuleBuilder(ruleName)
            .withAndConstraint(new AndBuilder()
                .withIfConstraint(new IfBuilder()
                    .withIf(A)
                    .withThen(B)
                )
                .appendBuilder(C.negate().wrapAtomicWithViolate())
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested if profile",
            Arrays.asList(field1, field2, field3),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert

        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that an if-then-else constraint nested inside the conditional of another if-then-else constraint violates
     * as expected.
     * In shorthand where A,B,C,D,E are atomic constraints:
     * VIOLATE(IF(IF A THEN B ELSE C) THEN D ELSE E)
     * ->   ((IF A THEN B ELSE C) AND VIOLATE(D)) OR (VIOLATE(IF A THEN B ELSE C) AND VIOLATE(E))
     * ->   ((IF A THEN B ELSE C) AND VIOLATE(D)) OR (((A AND VIOLATE(B)) OR (VIOLATE(A) AND VIOLATE(C))) AND VIOLATE(E))
     *
     * @throws IOException if the manifest writer fails to write.
     */
    @Test
    public void violate_nestedIfThenElseInsideIf_producesViolatedProfiles() throws IOException {
        //Arrange
        String ruleName = "Nested if rule";

        Rule rule = new RuleBuilder(ruleName)
            .withIfConstraint(new IfBuilder()
                .withIf(new IfBuilder()
                    .withIf(A)
                    .withThen(B)
                    .withElse(C)
                )
                .withThen(D)
                .withElse(E)
            )
            .build();

        //((IF A THEN B ELSE C) AND VIOLATE(D)) OR (((A AND VIOLATE(B)) OR (VIOLATE(A) AND VIOLATE(C))) AND VIOLATE(E))
        Rule violatedRule = new RuleBuilder(ruleName)
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .withIfConstraint(new IfBuilder()
                        .withIf(A)
                        .withThen(B)
                        .withElse(C)
                    )
                    .appendBuilder(D.negate().wrapAtomicWithViolate())
                )
                .withAndConstraint(new AndBuilder()
                    .withOrConstraint(new OrBuilder()
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(A)
                            .appendBuilder(B.negate().wrapAtomicWithViolate())
                        )
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(A.negate().wrapAtomicWithViolate())
                            .appendBuilder(C.negate().wrapAtomicWithViolate())
                        )
                    )
                    .appendBuilder(E.negate().wrapAtomicWithViolate())
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested if then else profile",
            Arrays.asList(field1, field2, field3, field4, field5),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert

        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that an if-then-else constraint nested inside the then clause of another if-then-else constraint violates
     * as expected.
     * In shorthand where A,B,C,D,E are atomic constraints:
     * VIOLATE(IF A THEN (IF B THEN C ELSE D) ELSE E)
     * ->   (A AND VIOLATE(IF B THEN C ELSE D)) OR (VIOLATE(A) AND VIOLATE(E))
     * ->   (A AND ((B AND VIOLATE(C)) OR (VIOLATE(B) AND VIOLATE(D))) OR (VIOLATE(A) AND VIOLATE(E))
     *
     * @throws IOException if the manifest writer fails to write.
     */
    @Test
    public void violate_nestedIfThenElseInsideThen_producesViolatedProfiles() throws IOException {
        //Arrange
        String ruleName = "Nested if rule";

        Rule rule = new RuleBuilder(ruleName)
            .withIfConstraint(new IfBuilder()
                .withIf(A)
                .withThen(new IfBuilder()
                    .withIf(B)
                    .withThen(C)
                    .withElse(D)
                )
                .withElse(E)
            )
            .build();

        //(A AND ((B AND VIOLATE(C)) OR (VIOLATE(B) AND VIOLATE(D))) OR (VIOLATE(A) AND VIOLATE(E))
        Rule violatedRule = new RuleBuilder(ruleName)
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A)
                    .withOrConstraint(new OrBuilder()
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(B)
                            .appendBuilder(C.negate().wrapAtomicWithViolate())
                        )
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(B.negate().wrapAtomicWithViolate())
                            .appendBuilder(D.negate().wrapAtomicWithViolate())
                        )
                    )
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A.negate().wrapAtomicWithViolate())
                    .appendBuilder(E.negate().wrapAtomicWithViolate())
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested if then else profile",
            Arrays.asList(field1, field2, field3, field4, field5),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        //Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        //Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that an if-then-else constraint nested inside the else clause of another if-then-else constraint violates
     * as expected.
     * In shorthand where A,B,C,D,E are atomic constraints:
     * VIOLATE(IF A THEN B ELSE (IF C THEN D ELSE E))
     * ->   (A AND VIOLATE(B)) OR (VIOLATE(A) AND VIOLATE(IF C THEN D ELSE E))
     * ->   (A AND VIOLATE(B)) OR (VIOLATE(A) AND ((C AND VIOLATE(D)) OR (VIOLATE(C) AND VIOLATE(E))))
     *
     * @throws IOException if the manifest writer fails to write.
     */
    @Test
    public void violate_nestedIfThenElseInsideElse_producesViolatedProfiles() throws IOException {
        //Arrange
        String ruleName = "Nested if rule";
        Rule rule = new RuleBuilder(ruleName)
            .withIfConstraint(new IfBuilder()
                .withIf(A)
                .withThen(B)
                .withElse(new IfBuilder()
                    .withIf(C)
                    .withThen(D)
                    .withElse(E)
                )
            )
            .build();

        //(A AND VIOLATE(B)) OR (VIOLATE(A) AND ((C AND VIOLATE(D)) OR (VIOLATE(C) AND VIOLATE(E))))
        Rule violatedRule = new RuleBuilder(ruleName)
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A)
                    .appendBuilder(B.negate().wrapAtomicWithViolate())
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A.negate().wrapAtomicWithViolate())
                    .withOrConstraint(new OrBuilder()
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(C)
                            .appendBuilder(D.negate().wrapAtomicWithViolate())
                        )
                        .withAndConstraint(new AndBuilder()
                            .appendBuilder(C.negate().wrapAtomicWithViolate())
                            .appendBuilder(E.negate().wrapAtomicWithViolate())
                        )
                    )
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested if then else profile",
            Arrays.asList(field1, field2, field3, field4, field5),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that an if-then constraint nested within inside an if statement of a not-if-then constraint violates
     * as expected.
     * In shorthand where A,B,C are atomic constraints: VIOLATE(IF(NOT(IF A THEN B)) THEN C) -> NOT(IF A THEN B) AND VIOLATE(C)
     */
    @Test
    public void violate_nestedIfThenInsideIfOfIfNotThen_producesViolatedProfiles() throws IOException {
        // Arrange
        Rule rule = new RuleBuilder("rule name")
            .withIfConstraint(new IfBuilder()
                .withIf(new IfBuilder()
                    .withIf(A)
                    .withThen(B)).negate()
                .withThen(C))
            .build();

        Rule violatedRule = new RuleBuilder("rule name")
            .withAndConstraint(new AndBuilder()
                .withIfConstraint(new IfBuilder()
                    .withIf(A)
                    .withThen(B)
                ).negate()
                .appendBuilder(C).negate().wrapAtomicWithViolate()
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Input Profile",
            Arrays.asList(field1, field2, field3),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    //Two-Layer Nested Constraints Violation

    /**
     * Tests that a profile violates correctly with nested NOTs.
     * Where X is any constraint which can contain another constraint:
     *      VIOLATE(NOT(X)) -> X
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedNotConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Not Rule")
            .appendBuilder(nestingConstraint).negate()
            .build();

        Rule violatedRule = new RuleBuilder("Nested Not Rule")
            .appendBuilder(nestingConstraint).wrapAtomicWithViolate()
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Not Profile",
            Collections.singletonList(staticField),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested AND.
     * Where X is any constraint which can contain another constraint and A is an atomic constraint:
     *      VIOLATE(X AND A)) -> (VIOLATE(X) AND A) OR (X AND VIOLATE(A))
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedAndConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested And Rule")
            .withAndConstraint(new AndBuilder()
                .appendBuilder(nestingConstraint)
                .appendBuilder(A)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested And Rule")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(violatedNestingConstraint)
                    .appendBuilder(A)
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(nestingConstraint)
                    .appendBuilder(A).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested And Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested OR.
     * Where X is any constraint which can contain another constraint and A is an atomic constraint:
     *      VIOLATE(X OR A)) -> VIOLATE(X) AND VIOLATE(A)
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedOrConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Or Rule")
            .withOrConstraint(new OrBuilder()
                .appendBuilder(nestingConstraint)
                .appendBuilder(A)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Or Rule")
            .withAndConstraint(new AndBuilder()
                .appendBuilder(violatedNestingConstraint)
                .appendBuilder(A).negate().wrapAtomicWithViolate()
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Or Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested IF.
     * Where X is any constraint which can contain another constraint and A is an atomic constraint:
     *      VIOLATE(IF X THEN A) -> X AND VIOLATE(A)
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedInsideIfOfIfThenConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Inside If of If-Then Rule")
            .withIfConstraint(new IfBuilder()
                .withIf(nestingConstraint)
                .withThen(A)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Inside If of If-Then Rule")
            .withAndConstraint(new AndBuilder()
                .appendBuilder(nestingConstraint)
                .appendBuilder(A).negate().wrapAtomicWithViolate()
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Inside If of If-Then Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested IF.
     * Where X is any constraint which can contain another constraint and A is an atomic constraint:
     *      VIOLATE(IF A THEN X) -> A AND VIOLATE(X)
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedInsideThenOfIfThenConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Inside Then of If-Then Rule")
            .withIfConstraint(new IfBuilder()
                .withIf(A)
                .withThen(nestingConstraint)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Inside Then of If-Then Rule")
            .withAndConstraint(new AndBuilder()
                .appendBuilder(A)
                .appendBuilder(violatedNestingConstraint)
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Inside Then of If-Then Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested IF.
     * Where X is any constraint which can contain another constraint and A,B are atomic constraints:
     *      VIOLATE(IF X THEN A ELSE B) -> (X AND VIOLATE(A)) OR (VIOLATE(X) AND VIOLATE(B))
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedInsideIfOfIfThenElseConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Inside If of If-Then-Else Rule")
            .withIfConstraint(new IfBuilder()
                .withIf(nestingConstraint)
                .withThen(A)
                .withElse(B)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Inside If of If-Then-Else Rule")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(nestingConstraint)
                    .appendBuilder(A).negate().wrapAtomicWithViolate()
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(violatedNestingConstraint)
                    .appendBuilder(B).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Inside If of If-Then-Else Profile",
            Arrays.asList(staticField, field1, field2),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested IF.
     * Where X is any constraint which can contain another constraint and A,B are atomic constraints:
     *      VIOLATE(IF A THEN X ELSE B) -> (A AND VIOLATE(X)) OR (VIOLATE(A) AND VIOLATE(B))
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedInsideThenOfIfThenElseConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Inside Then of If-Then-Else Rule")
            .withIfConstraint(new IfBuilder()
                .withIf(A)
                .withThen(nestingConstraint)
                .withElse(B)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Inside Then of If-Then-Else Rule")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A)
                    .appendBuilder(violatedNestingConstraint)
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A).negate().wrapAtomicWithViolate()
                    .appendBuilder(B).negate().wrapAtomicWithViolate()
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Inside Then of If-Then-Else Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    /**
     * Tests that a profile violates correctly with nested IF.
     * Where X is any constraint which can contain another constraint and A,B are atomic constraints:
     *      VIOLATE(IF A THEN B ELSE X) -> (A AND VIOLATE(B)) OR (VIOLATE(A) AND VIOLATE(X))
     * @param nestingConstraint A builder of a constraint which can contain at least one other constraint.
     * @param violatedNestingConstraint The violated form of the previous nesting constraint.
     */
    @ParameterizedTest
    @MethodSource("nestingConstraints")
    public void violate_withNestedInsideElseOfIfThenElseConstraint_returnsCorrectViolatedProfile(
        BaseConstraintBuilder<? extends Constraint> nestingConstraint,
        BaseConstraintBuilder<? extends Constraint> violatedNestingConstraint
    ) throws IOException {
        //Arrange
        Rule rule = new RuleBuilder("Nested Inside Else of If-Then-Else Rule")
            .withIfConstraint(new IfBuilder()
                .withIf(A)
                .withThen(B)
                .withElse(nestingConstraint)
            )
            .build();

        Rule violatedRule = new RuleBuilder("Nested Inside Else of If-Then-Else Rule")
            .withOrConstraint(new OrBuilder()
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A)
                    .appendBuilder(B).negate().wrapAtomicWithViolate()
                )
                .withAndConstraint(new AndBuilder()
                    .appendBuilder(A).negate().wrapAtomicWithViolate()
                    .appendBuilder(violatedNestingConstraint)
                )
            )
            .build();

        TestProfiles testProfiles = createTestProfiles(
            "Nested Inside Else of If-Then Profile",
            Arrays.asList(staticField, field1),
            Collections.singletonList(new Pair<>(rule, violatedRule))
        );

        // Act
        List<Profile> violatedProfiles = profileViolator.violate(testProfiles.inputProfile);

        // Assert
        assertProfileListsAreEquivalent(violatedProfiles, testProfiles.expectedViolatedProfiles);
        verify(mockManifestWriter, times(1))
            .writeManifest(anyListOf(ViolatedProfile.class), same(mockPath));
    }

    private TestProfiles createTestProfiles(String description, List<Field> fields, List<Pair<Rule, Rule>> ruleViolatedRulePairs) {
        List<Rule> rules = ruleViolatedRulePairs
            .stream()
            .map(Pair::getKey)
            .collect(Collectors.toList());

        Profile inputProfile = new Profile(fields, rules, description);
        List<Profile> violatedProfiles = new ArrayList<>();

        for (int i = 0; i < ruleViolatedRulePairs.size(); i++) {
            Pair<Rule,Rule> rulePair = ruleViolatedRulePairs.get(i);
            List<Rule> newRuleList = new ArrayList<>(rules);
            newRuleList.set(i, rulePair.getValue());
            violatedProfiles.add(
                new ViolatedProfile(
                    rulePair.getKey(),
                    new ProfileFields(fields),
                    newRuleList,
                    description + " -- Violating: " + rulePair.getKey().ruleInformation.getDescription()
                )
            );
        }

        return new TestProfiles(inputProfile, violatedProfiles);
    }

    private class TestProfiles {
        Profile inputProfile;
        List<Profile> expectedViolatedProfiles;

        TestProfiles(Profile inputProfile, List<Profile> expectedViolatedProfiles) {
            this.inputProfile = inputProfile;
            this.expectedViolatedProfiles = expectedViolatedProfiles;
        }
    }
}
