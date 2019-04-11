package com.scottlogic.deg.generator.generation.rows;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.ValueGenerator;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.*;

class StandardRowSpecRowCombinerFactoryTests {

    private static final Field field = new Field("Field 1");
    private static final ProfileFields fields = new ProfileFields(Collections.singletonList(field));

    @Test
    void shouldCreateMultiplexingRowSourceForRowSpec() {
        FieldSpec fieldSpec = FieldSpec.Empty;
        Map<Field, FieldSpec> map = new HashMap<Field, FieldSpec>() {{ put(field, fieldSpec); }};
        RowSpec rowSpec = new RowSpec(fields, map);
        ValueGenerator valueGenerator = mock(ValueGenerator.class);
        when(valueGenerator.generate(any(), any())).thenReturn(Stream.of(new Value(field, "value")));

        RowCombinerFactory factory = new RowCombinerFactory(valueGenerator);

        RowCombiner result = factory.createRowCombiner(rowSpec);

        Assert.assertThat(result, instanceOf(FieldCombiningRowCombiner.class));
    }

    @Test
    void shouldCreateValuesForEachFieldSpecInRowSpec() {
        FieldSpec fieldSpec = FieldSpec.Empty;
        Map<Field, FieldSpec> map = new HashMap<Field, FieldSpec>() {{ put(field, fieldSpec); }};
        RowSpec rowSpec = new RowSpec(fields, map);
        ValueGenerator valueGenerator = mock(ValueGenerator.class);
        when(valueGenerator.generate(any(), any())).thenReturn(Stream.of(new Value(field, "value")));

        RowCombinerFactory factory = new RowCombinerFactory(valueGenerator);

        factory.createRowCombiner(rowSpec);

        verify(valueGenerator, times(1)).generate(field, fieldSpec);
    }
}