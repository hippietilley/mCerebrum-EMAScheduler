package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/10/16.
 */
public class EMAType {
    public static final String TYPE_RANDOM = "RANDOM";
    public static final String TYPE_EVENT = "EVENT";
    public static final String ID_SMOKING_EMA="SMOKING_EMA";
    public static final String ID_END_OF_DAY_EMA="END_OF_DAY_EMA";
    public static final String ID_RANDOM_EMA="RANDOM_EMA";
    public static final String ID_EMI="EMI";
    String id;
    String type;
    String category;
    String name;
    Application application;
    Window[] windows;
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

    public String getCategory() {
        return category;
    }

    public Notification[] getNotifications() {
        return notifications;
    }

    public Window[] getWindows() {
        return windows;
    }

    public SchedulerRule[] getScheduler_rules() {
        return scheduler_rules;
    }
}
