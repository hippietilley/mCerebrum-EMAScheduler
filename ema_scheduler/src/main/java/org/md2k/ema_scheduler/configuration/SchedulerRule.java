package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/14/16.
 */
public class SchedulerRule {
    public static final String TYPE_RANDOM="RANDOM";
    public static final String TYPE_WHEN_POSSIBLE="WHEN_POSSIBLE";
    public static final String TYPE_NOW="NOW";
    public static final String TIME_WINDOW_START="WINDOW_START";
    public static final String TIME_WINDOW_END="WINDOW_END";
    public static final String TIME_LAST_SCHEDULE="LAST_SCHEDULE";
    String type;
    String start_time;
    String end_time;
    int divide;

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
}
