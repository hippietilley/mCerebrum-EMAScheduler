package org.md2k.emascheduler.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigurationEMAType {
    public static final String TYPE_USER="USER";
    public static final String TYPE_SYSTEM="SYSTEM";
    String id;
    String name;
    String type;
    ConfigurationApplication application;
    ArrayList<ConfigurationEMA> ema;
    ConfigurationCondition condition;

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
