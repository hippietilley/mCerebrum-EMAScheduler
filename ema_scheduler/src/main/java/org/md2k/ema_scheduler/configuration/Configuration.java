package org.md2k.ema_scheduler.configuration;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.ema_scheduler.Constants;
import org.md2k.utilities.Files;
import org.md2k.utilities.data_format.NotificationRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Configuration{
    EMAType[] ema_types;
    NotificationRequest[] notificationRequests;
    ConfigCondition[] conditions;
    private static Configuration instance=null;
    public static Configuration getInstance(){
        if(instance==null)
            instance=new Configuration();
        return instance;
    }
    public static void clear(){
        instance=null;
    }
    private Configuration(){
        readEMATypes();
        readNotifications();
        readConditions();
    }
    private void readEMATypes(){
        BufferedReader br;
        String filepath= Constants.CONFIG_DIRECTORY+Constants.CONFIG_FILENAME;
        if(!Files.isExist(filepath))
            ema_types=null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<EMAType[]>() {
                }.getType();
                ema_types = gson.fromJson(br, collectionType);
            } catch (IOException e) {
                ema_types = null;
            }
        }

    }
    private void readNotifications(){
        BufferedReader br;
        String filepath= Constants.CONFIG_DIRECTORY+Constants.NOTIFICATION_FILENAME;
        if(!Files.isExist(filepath))
            notificationRequests =null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<NotificationRequest[]>() {
                }.getType();
                notificationRequests = gson.fromJson(br, collectionType);
            } catch (IOException e) {
                notificationRequests = null;
            }
        }

    }
    private void readConditions(){
        BufferedReader br;
        String filepath= Constants.CONFIG_DIRECTORY+Constants.CONDITION_FILENAME;
        if(!Files.isExist(filepath))
            conditions=null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<ConfigCondition[]>() {
                }.getType();
                conditions = gson.fromJson(br, collectionType);
            } catch (IOException e) {
                conditions = null;
            }
        }

    }

    public EMAType[] getEma_types() {
        return ema_types;
    }
    public EMAType getEma_types(String id, String type){
        for (EMAType ema_type : ema_types)
            if (ema_type.getId().equals(id) && ema_type.getType().equals(type)) return ema_type;
        return null;
    }

    public NotificationRequest[] getNotificationRequests() {
        return notificationRequests;
    }

    public ConfigCondition[] getConditions() {
        return conditions;
    }
    public ConfigCondition getConditions(String id){
        for (ConfigCondition condition : conditions)
            if (condition.getId().equals(id)) return condition;
        return null;
    }
}
