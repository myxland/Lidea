/*
 * Copyright (c) 2010-2020 Founder Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Founder. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Founder.
 *
 */
package com.mmc.lidea.stream.flink;

import com.mmc.lidea.stream.model.LogContent;
import com.mmc.lidea.stream.util.LogContentUtil;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author Joey
 * @date 2019/8/1 19:31
 */
public class LogContentSplitter implements MapFunction<String, LogContent> {

    private static final long serialVersionUID = -5055197964573569120L;


    /**
     * The mapping method. Takes an element from the input data set and transforms
     * it into exactly one element.
     *
     * @param element The input value.
     * @return The transformed value
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
     *                   to fail and may trigger recovery.
     */
    @Override
    public LogContent map(String element) throws Exception {

        return LogContentUtil.valueOf(element);
    }


}
