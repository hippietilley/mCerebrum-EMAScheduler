package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/14/16.
 */
public class Block {
    public static final String BASE_DAY_START ="DAY_START";
    int total;
    String base;
    long start_offset;
    long end_offset;

    public int getTotal() {
        return total;
    }

    public String getBase() {
        return base;
    }

    public long getStart_offset() {
        return start_offset;
    }

    public long getEnd_offset() {
        return end_offset;
    }
}
