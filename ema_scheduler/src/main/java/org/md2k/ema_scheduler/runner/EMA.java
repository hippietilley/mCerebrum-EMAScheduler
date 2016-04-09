package org.md2k.ema_scheduler.runner;

import com.google.gson.JsonArray;

/**
 * Created by monowar on 3/22/16.
 */
public class EMA {
    String id;
    String name;
    String trigger_type;
    long start_timestamp;
    long end_timestamp;
    String status;
    JsonArray question_answers;

}
