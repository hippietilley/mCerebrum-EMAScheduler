package org.md2k.emascheduler.configuration;

import java.util.HashMap;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigurationApplication {
    String package_name;
    String file_name;
    HashMap<String, String> params;

    public String getPackage_name() {
        return package_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public HashMap<String, String> getParams() {
        return params;
    }
}
