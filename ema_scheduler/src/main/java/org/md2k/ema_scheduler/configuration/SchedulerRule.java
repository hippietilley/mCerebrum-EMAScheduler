package org.md2k.ema_scheduler.configuration;

import org.md2k.datakitapi.source.datasource.DataSource;

import java.io.Serializable;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class SchedulerRule implements Serializable {
    public static final String TYPE_RANDOM="RANDOM";
    public static final String TYPE_IMMEDIATE ="IMMEDIATE";
    public static final String TIME_BLOCK_START ="BLOCK_START";
    public static final String TIME_BLOCK_END ="BLOCK_END";
    public static final String TIME_LAST_SCHEDULE="LAST_SCHEDULE";
    private String type;
    private String start_time;
    private String end_time;
    private int divide;
    private DataSource data_source;
    private String[] parameters;
    private String[] conditions;

    public String getType() {
        return type;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public int getDivide() {
        return divide;
    }

    public DataSource getData_source() {
        return data_source;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String[] getConditions() {
        return conditions;
    }
}
