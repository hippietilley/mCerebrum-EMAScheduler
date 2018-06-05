/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
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

package org.md2k.ema_scheduler.configuration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.ema_scheduler.Constants;
import org.md2k.utilities.FileManager;
import org.md2k.utilities.data_format.notification.NotificationRequests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Provides methods for configuring the <code>EMAType</code>, <code>Notification</code>, and <code>Conditions</code>.
 */
public class Configuration{
    private static Configuration instance = null;
    private EMAType[] ema_types;
    private NotificationRequests notification_option;
    private ConfigCondition[] conditions;

    /**
     * Constructor
     * Reads the EMATypes, notifications, and conditions.
     */
    private Configuration() {
        readEMATypes();
        readNotifications();
        readConditions();
    }

    /**
     * Returns this instance of this class.
     * @return This instance of this class.
     */
    public static Configuration getInstance(){
        if(instance == null)
            instance = new Configuration();
        return instance;
    }

    /**
     * Sets this instance to null.
     */
    public static void clear(){
        instance = null;
    }

    /**
     * Reads the <code>EMAType</code>s from a json file.
     */
    private void readEMATypes(){
        BufferedReader br;
        String filepath = Constants.CONFIG_DIRECTORY + Constants.CONFIG_FILENAME;
        if(!FileManager.isExist(filepath))
            ema_types = null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<EMAType[]>() {
                }.getType();
                ema_types = gson.fromJson(br, collectionType);
                br.close();
            } catch (IOException e) {
                ema_types = null;
            }
        }
    }

    /**
     * Reads the notifications from a json file.
     */
    private void readNotifications(){
        BufferedReader br;
        String filepath = Constants.CONFIG_DIRECTORY+Constants.NOTIFICATION_FILENAME;
        if(!FileManager.isExist(filepath))
            notification_option = null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                notification_option = gson.fromJson(br, NotificationRequests.class);
                br.close();
            } catch (IOException e) {
                notification_option = null;
            }
        }
    }

    /**
     * Reads the conditions from a json file.
     */
    private void readConditions(){
        BufferedReader br;
        String filepath = Constants.CONFIG_DIRECTORY + Constants.CONDITION_FILENAME;
        if(!FileManager.isExist(filepath))
            conditions = null;
        else {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                Gson gson = new Gson();
                Type collectionType = new TypeToken<ConfigCondition[]>() {
                }.getType();
                conditions = gson.fromJson(br, collectionType);
                br.close();
            } catch (IOException e) {
                conditions = null;
            }
        }
    }

    /**
     * Returns an array of <code>EMAType</code>s.
     * @return An array of <code>EMAType</code>s.
     */
    public EMAType[] getEma_types() {
        return ema_types;
    }

    /**
     * Returns <code>EMAType</code>s of the given id and type.
     * @param id Id to match.
     * @param type Type to match.
     * @return <code>EMAType</code>s of the given id and type.
     */
    public EMAType getEma_types(String id, String type){
        for (EMAType ema_type : ema_types)
            if (ema_type.getId().equals(id) && ema_type.getType().equals(type))
                return ema_type;
        return null;
    }

    /**
     * Returns the notification options.
     * @return The notification options.
     */
    public NotificationRequests getNotification_option() {
        return notification_option;
    }

    /**
     * Returns the conditions.
     * @return The conditions.
     */
    public ConfigCondition[] getConditions() {
        return conditions;
    }

    /**
     * Returns the conditions matching the given id.
     * @param id Id of the desired conditions.
     * @return The conditions matching the given id.
     */
    public ConfigCondition getConditions(String id){
        for (ConfigCondition condition : conditions)
            if (condition.getId().equals(id))
                return condition;
        return null;
    }
}
