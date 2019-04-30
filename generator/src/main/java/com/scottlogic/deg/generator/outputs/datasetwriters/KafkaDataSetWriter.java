package com.scottlogic.deg.generator.outputs.datasetwriters;

import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.outputs.GeneratedObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

/**
 * This class contains the functionality to connect to a kafka broker.
 * openWriter creates a kafka producer with the properties of the kafka cluster
 * writeRow sends a record to the kafka broker with the formatted data
 */
public class KafkaDataSetWriter implements DataSetWriter<KafkaProducer> {
    private final Properties kafkaProperties = new Properties();

    @Override
    public KafkaProducer openWriter(Path directory, String fileName, ProfileFields profileFields) throws IOException {
        String propertiesPath = "generator/dataHelix.properties";
        kafkaProperties.load(new FileInputStream(propertiesPath));
        return new KafkaProducer<String, String>(kafkaProperties);
    }

    @Override
    public void writeRow(KafkaProducer kafkaProducer, GeneratedObject row) {
        String value = getValuesAsString(row);
        try {
            kafkaProducer.send(new ProducerRecord<>(kafkaProperties.getProperty("topic"), "dataHelix", value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFileName(String fileNameWithoutExtension) {
        return null;
    }

    private String getValuesAsString(GeneratedObject row) {
        ArrayList<String> strValue = new ArrayList<>();

        for (int i = 0; i < row.values.size(); i++) {
            String field = row.source.columns.get(i).field.toString();
            String value = Optional
                .ofNullable(row.values.get(i).getFormattedValue()).orElse("null")
                .toString();

            String desiredFormat = String.format("{%s: %s}", field, value);
            strValue.add(desiredFormat);
        }

        return strValue.toString();
    }
}
