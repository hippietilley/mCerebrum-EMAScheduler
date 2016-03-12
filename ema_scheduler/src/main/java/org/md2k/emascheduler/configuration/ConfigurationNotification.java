package org.md2k.emascheduler.configuration;

import org.md2k.datakitapi.source.datasource.DataSource;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigurationNotification {
    String id;
    String name;
    String type;
    DataSource data_source;
    String format;
    String repeat;
    String interval;
    String[] message;
    ConfigurationResponseOption response_option;
}
