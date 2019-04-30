package com.scottlogic.deg.generator.outputs.datasetwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.outputs.GeneratedObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * This class contains the functionality to connect to a kafka broker.
 * openWriter creates a kafka producer with the properties of the kafka cluster
 * writeRow sends a record to the kafka broker with the formatted data
 */
public class KafkaDataSetWriter implements DataSetWriter<KafkaDataSetWriter.KafkaWriter> {

    @Override
    public KafkaWriter openWriter(Path directory, String fileName, ProfileFields profileFields) throws IOException {
        return new KafkaWriter(profileFields);
    }

    @Override
    public void writeRow(KafkaWriter kafkaWriter, GeneratedObject row) {
        ObjectNode rowNode = kafkaWriter.jsonObjectMapper.createObjectNode();

        JsonDataSetWriter.writeRowToJsonNode(row, kafkaWriter.profileFields, rowNode);

//        try {
//            System.out.println(kafkaWriter.jsonObjectMapper.writeValueAsString(rowNode));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        ProducerRecord<String, JsonNode> rec = new ProducerRecord<String, JsonNode>(kafkaWriter.kafkaProperties.getProperty("topic"),rowNode);
        kafkaWriter.kafkaProducer.send(rec);
    }

    @Override
    public String getFileName(String fileNameWithoutExtension) {
        return null;
    }

    public class KafkaWriter implements Closeable {
        private final ObjectMapper jsonObjectMapper;
        private final ProfileFields profileFields;
        private final KafkaProducer<String, JsonNode> kafkaProducer;
        private final Properties kafkaProperties;


        public KafkaWriter(ProfileFields profileFields) throws IOException {
            this.profileFields = profileFields;
            this.jsonObjectMapper = new ObjectMapper();
            this.kafkaProperties = getKafkaProperties();
            this.kafkaProducer = new KafkaProducer<String, JsonNode>(this.kafkaProperties);
        }

        private Properties getKafkaProperties() throws IOException {
            Properties kafkaProperties = new Properties();
            String propertiesPath = "generator/dataHelix.properties";
            kafkaProperties.load(new FileInputStream(propertiesPath));
           // kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
           // kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

            return kafkaProperties;
        }

        @Override
        public void close(){
            this.kafkaProducer.close();
        }


    }
}
