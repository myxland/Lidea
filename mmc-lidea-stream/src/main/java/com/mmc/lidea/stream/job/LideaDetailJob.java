/*
 * Copyright (c) 2010-2020 Founder Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Founder. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Founder.
 *
 */
package com.mmc.lidea.stream.job;

import com.mmc.lidea.common.context.KafkaConst;
import com.mmc.lidea.stream.Bootstrap;
import com.mmc.lidea.stream.flink.LideaDetailSinkFun;
import com.mmc.lidea.stream.flink.LogContentErrorFilter;
import com.mmc.lidea.stream.flink.LogContentSplitter;
import com.mmc.lidea.stream.flink.MessageWaterEmitter;
import com.mmc.lidea.stream.model.LogContent;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Joey
 * @date 2019/8/4 16:39
 */
public class LideaDetailJob {


    public static void main(String[] args) throws Exception {


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000); // 非常关键，一定要设置启动检查点！！
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        String confName = "lidea.properties";
        InputStream in = Bootstrap.class.getClassLoader().getResourceAsStream(confName);
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(in);
        env.getConfig().setGlobalJobParameters(parameterTool);

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", parameterTool.get("kafka.bootstrap.servers", "localhost:9092"));
        props.setProperty("group.id", "lidea-detail-group");

        FlinkKafkaConsumer010<String> consumer =
                new FlinkKafkaConsumer010<>(KafkaConst.TOPIC, new SimpleStringSchema(), props);
        consumer.assignTimestampsAndWatermarks(new MessageWaterEmitter());

        // 分离出日志格式
        DataStream<LogContent> mapStream = env.addSource(consumer)
                .filter(new LogContentErrorFilter())
                .map(new LogContentSplitter());


        // 写入故障数据
        addBaseJob(mapStream);
        // keyStream.print().setParallelism(1); // 打印调试

        env.execute("Record the exception detail.");

    }

    private static void addBaseJob(DataStream<LogContent> mapStream) {

        mapStream.keyBy("traceId")
                .timeWindow(Time.seconds(10))
                ;

        // mapStream.print().setParallelism(1); // 打印调试
        mapStream.addSink(new LideaDetailSinkFun());
    }

}
