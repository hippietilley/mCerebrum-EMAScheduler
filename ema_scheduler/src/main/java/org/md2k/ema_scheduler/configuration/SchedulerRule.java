package org.md2k.ema_scheduler.configuration;

import org.md2k.datakitapi.source.datasource.DataSource;

/**
 * Created by monowar on 3/14/16.
 */
public class SchedulerRule {
    public static final String TYPE_RANDOM="RANDOM";
    public static final String TYPE_IMMEDIATE ="IMMEDIATE";
    public static final String TIME_BLOCK_START ="BLOCK_START";
    public static final String TIME_BLOCK_END ="BLOCK_END";
    public static final String TIME_LAST_SCHEDULE="LAST_SCHEDULE";
    String type;
    String start_time;
    String end_time;
    int divide;
    DataSource data_source;
    String[] parameters;
    String[] conditions;

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
