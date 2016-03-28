package org.md2k.ema_scheduler.configuration;

import org.md2k.datakitapi.source.datasource.DataSource;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigCondition {
    String id;
    String type;
    String name;
    ArrayList<String> values;
    DataSource data_source;
    Source source;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getValues() {
        return values;
    }


    public Source getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public DataSource getData_source() {
        return data_source;
    }

    public class Source{
        String id;
        String type;

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }
}
