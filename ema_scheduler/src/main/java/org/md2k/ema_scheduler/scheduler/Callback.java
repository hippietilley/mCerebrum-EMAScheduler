package org.md2k.ema_scheduler.scheduler;

/**
 * Created by monowar on 3/14/16.
 */
public interface Callback {
    void onDayStartChanged();
    void onDayEndChanged();
}
