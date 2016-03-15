package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/14/16.
 */
public class Window {
    public static final String TYPE_DAY_START="DAY_START";
    int count;
    String type;
    long start_offset;
    long end_offset;

    public int getCount() {
        return count;
    }

    public String getType() {
        return type;
    }

    public long getStart_offset() {
        return start_offset;
    }

    public long getEnd_offset() {
        return end_offset;
    }
}
