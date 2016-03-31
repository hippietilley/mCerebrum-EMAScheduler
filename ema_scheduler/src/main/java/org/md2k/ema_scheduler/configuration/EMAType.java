package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/10/16.
 */
public class EMAType {
    public static final String TYPE_EMA = "EMA";
    public static final String TYPE_EMI = "EMI";
    public static final String ID_RANDOM_EMA="RANDOM_EMA";
    public static final String ID_SMOKING_EMA ="SMOKING_EMA";
    public static final String ID_STRESS_EMA ="STRESS_EMA";
    public static final String ID_END_OF_DAY_EMA="END_OF_DAY_EMA";
    public static final String ID_EMI="EMI";
    String id;
    String type;
    String trigger_type;
    String name;
    boolean enable;
    Application application;
    Block[] blocks;
    SchedulerRule[] scheduler_rules;
    Notification[] notifications;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Application getApplication() {
        return application;
    }

    public String getTrigger_type() {
        return trigger_type;
    }

    public Notification[] getNotifications() {
        return notifications;
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public SchedulerRule[] getScheduler_rules() {
        return scheduler_rules;
    }

    public boolean isEnable() {
        return enable;
    }
}
