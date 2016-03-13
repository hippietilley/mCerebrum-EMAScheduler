package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigurationEMAType {
    String id;
    String type;
    String category;
    String name;
    ConfigurationApplication application;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ConfigurationApplication getApplication() {
        return application;
    }

}
