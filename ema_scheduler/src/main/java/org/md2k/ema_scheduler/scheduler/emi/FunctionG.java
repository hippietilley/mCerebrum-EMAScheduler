package org.md2k.ema_scheduler.scheduler.emi;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by monowar on 3/29/16.
 */
public class FunctionG {
    public static double getG(Context context, boolean isPreQuit, boolean isStress, int remainingTime) {
        BufferedReader reader;
        String filename;
        double value=-1;
        try {
            if (isPreQuit)
                filename = "prelapse_g";
            else filename = "postlapse_g";
            InputStream file = context.getResources().openRawResource(
                    context.getResources().getIdentifier(filename,
                            "raw", context.getPackageName()));
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
                if(items.size()!=3) continue;
                if(Integer.parseInt(items.get(0))==remainingTime){
                    if(!isStress){
                        value=Double.parseDouble(items.get(1));
                    }
                    else value=Double.parseDouble(items.get(2));
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return value;
    }
}
