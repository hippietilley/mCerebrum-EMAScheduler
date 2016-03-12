package org.md2k.emascheduler.configuration;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.emascheduler.Constants;
import org.md2k.utilities.Files;
import org.md2k.utilities.Report.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by monowar on 3/10/16.
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    Context context;
    Configuration configuration;
    public static ConfigurationManager getInstance(Context context){
        if(instance==null)
            instance=new ConfigurationManager(context);
        return instance;
    }
    private ConfigurationManager(Context context){
        this.context=context;
        read();
    }
    private void read() {
        BufferedReader br;
        String filepath= Constants.CONFIG_DIRECTORY+Constants.CONFIG_FILENAME;
        if(!Files.isExist(filepath))
            configuration=null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<List<Configuration>>() {
                }.getType();
                configuration = gson.fromJson(br, collectionType);
            } catch (IOException e) {
                configuration = null;
            }
        }
    }
    public Configuration getConfiguration(){
        return configuration;
    }
    public void clear(){
        instance=null;
    }
}
