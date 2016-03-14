package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/10/16.
 */
public class EMAType {
    String id;
    String type;
    String category;
    String name;
    Application application;
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
}
