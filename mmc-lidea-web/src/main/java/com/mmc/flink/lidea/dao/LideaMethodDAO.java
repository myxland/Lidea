/*
 * Copyright (c) 2010-2020 Founder Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Founder. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Founder.
 *
 */
package com.mmc.flink.lidea.dao;

import com.mmc.flink.lidea.bo.LideaAppBO;
import com.mmc.flink.lidea.bo.LideaMethodBO;
import com.mmc.flink.lidea.context.Const;
import com.mmc.flink.lidea.dto.LideaMethodReq;
import com.mmc.flink.lidea.mapper.LideaAppResultsExtractor;
import com.mmc.flink.lidea.mapper.LideaMethodResultsExtractor;
import com.mmc.lidea.util.BytesUtils;
import com.mmc.lidea.util.MD5Util;
import com.mmc.lidea.util.StringUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Joey
 * @date 2019/8/6 18:29
 */
@Service("lideaMethodDAO")
public class LideaMethodDAO {

    @Resource
    private HbaseTemplate hbaseTemplate;

    public LideaMethodBO put(LideaMethodBO bo) {

        Put put = new Put(makeRowKey(bo));
        put.addColumn(Const.LIDEA_LOG_FEMILY, BytesUtils.toBytes("time"), BytesUtils.toBytes(bo.time));
        put.addColumn(Const.LIDEA_LOG_FEMILY, BytesUtils.toBytes("appName"), BytesUtils.toBytes(bo.appName));
        put.addColumn(Const.LIDEA_LOG_FEMILY, BytesUtils.toBytes("serviceName"), BytesUtils.toBytes(bo.serviceName));
        put.addColumn(Const.LIDEA_LOG_FEMILY, BytesUtils.toBytes("methodName"), BytesUtils.toBytes(bo.methodName));

        return hbaseTemplate.execute(Const.LIDEA_METHOD_TABLE, (htable) -> {

            htable.put(put);
            return bo;

        });

    }

    private byte[] makeRowKey(LideaMethodBO bo) {
        String base = StringUtil.format("{}{}", bo.appName, MD5Util.encrypt(bo.serviceName + bo.methodName));
        return BytesUtils.toBytes(base);
    }

    public List<LideaMethodBO> scan(LideaMethodReq req) {


        Scan scan = new Scan();
        scan.setCaching(50);
        scan.addFamily(Const.LIDEA_LOG_FEMILY);
        scan.setFilter(new PrefixFilter(req.getAppName().getBytes()));

        return hbaseTemplate.find(Const.LIDEA_METHOD_TABLE, scan, new LideaMethodResultsExtractor());
    }
}