package com.scottlogic.deg.generator.outputs.datasetwriters;

import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.outputs.GeneratedObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * This class contains the functionality to write the output to HBase.
 */
public class HBaseDataSetWriter implements DataSetWriter<HBaseDataSetWriter.HBaseWriter> {
//    private final Properties hbaseProperties = new Properties();
//    private Configuration config = HBaseConfiguration.create();
    private Connection connection;
    private TableName table1 = TableName.valueOf("MrSpacemanTestTable");
    private String family1 = "MrSpacemanFamily1";
    private String family2 = "MrSpacemanFamily2";
    private String family3 = "MrSpacemanFamily3";
    private String family4 = "MrSpacemanFamily4";

    @Override
    public HBaseWriter openWriter(Path directory, String fileName, ProfileFields profileFields) throws IOException {
//        String propertiesPath = "generator/dataHelix.properties";
//        hbaseProperties.load(new FileInputStream(propertiesPath));

//        URL path = this.getClass()
//            .getClassLoader()
//            .getResource("hbase-site.xml");
//        config.addResource(path);

        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("hbase.zookeeper.quorum", "ec2-99-81-98-128.eu-west-1.compute.amazonaws.com");
//        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        connection = ConnectionFactory.createConnection(conf);

//        connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin();
        if (!admin.tableExists(table1)) {
            HTableDescriptor htdesc = new HTableDescriptor(table1);
            htdesc.addFamily(new HColumnDescriptor(family1));
            htdesc.addFamily(new HColumnDescriptor(family2));
            htdesc.addFamily(new HColumnDescriptor(family3));
            htdesc.addFamily(new HColumnDescriptor(family4));
            admin.createTable(htdesc);
        }

        return new HBaseWriter();
    }

    @Override
    public void writeRow(HBaseWriter hbaseWriter, GeneratedObject row) {
        try {
            Table table = connection.getTable(table1);

            LocalDateTime ldt = LocalDateTime.now();
            String rowId = ldt.toString() + UUID.randomUUID();
            Put p = new Put(Bytes.toBytes(rowId));

            for (int i = 0; i < row.values.size(); i++) {
                String field = row.source.columns.get(i).field.toString();
                String value = Optional
                    .ofNullable(row.values.get(i).getFormattedValue()).orElse("null")
                    .toString();

                p.addColumn(Bytes.toBytes(family1), Bytes.toBytes(field), Bytes.toBytes(value));
            }
            table.put(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFileName(String fileNameWithoutExtension) {
        return null;
    }

    public class HBaseWriter implements Closeable {
        public HBaseWriter() {
        }

        @Override
        public void close() throws IOException {
        }
    }
}
