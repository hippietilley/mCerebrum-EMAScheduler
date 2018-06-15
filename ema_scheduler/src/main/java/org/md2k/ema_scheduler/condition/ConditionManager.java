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

package org.md2k.ema_scheduler.condition;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.EMA_limit.EMALimitManager;
import org.md2k.ema_scheduler.condition.battery_level.BatteryLevelManager;
import org.md2k.ema_scheduler.condition.bonus_incentive.BonusIncentive;
import org.md2k.ema_scheduler.condition.data_quality.DataQualityManager;
import org.md2k.ema_scheduler.condition.ema_answer.EmaAnswerManager;
import org.md2k.ema_scheduler.condition.last_ema_emi.LastEmaEmiManager;
import org.md2k.ema_scheduler.condition.no_self_report.NoSelfReportManager;
import org.md2k.ema_scheduler.condition.not_active.NotActiveManager;
import org.md2k.ema_scheduler.condition.not_driving.DrivingDetectorManager;
import org.md2k.ema_scheduler.condition.privacy.PrivacyManager;
import org.md2k.ema_scheduler.condition.valid_block.ValidBlockManager;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

import java.util.HashMap;

/**
 * Manages the conditions under which an EMA is administered.
 */
public class ConditionManager {
    public static final String TYPE_DATA_QUALITY = "DATA_QUALITY";
    private static final String TYPE_PHONE_BATTERY = "PHONE_BATTERY";
    private static final String TYPE_VALID_BLOCK = "VALID_BLOCK";
    private static final String TYPE_LAST_EMA_EMI = "LAST_EMA_EMI";
    private static final String TYPE_NOT_ACTIVE = "NOT_ACTIVE";
    private static final String TYPE_NOT_DRIVING = "NOT_DRIVING";
    private static final String TYPE_PRIVACY = "PRIVACY";
    private static final String TYPE_EMA_ANSWER = "EMA_ANSWER";
    private static final String TYPE_NO_SELF_REPORT = "NO_SELF_REPORT";
    private static final String TYPE_EMA_LIMIT = "EMA_LIMIT";
    private static final String TYPE_BONUS_INCENTIVE = "BONUS_INCENTIVE";

    private static final String TAG = ConditionManager.class.getSimpleName();
    private static ConditionManager instance = null;
    private HashMap<String, Condition> conditionHashMap;
    private Configuration configuration;
    private Context context;

    /**
     * Constructor
     * @param context Android context
     */
    private ConditionManager(Context context){
        this.context = context;
        conditionHashMap = new HashMap<>();
        configuration = Configuration.getInstance();
        ConfigCondition[] configConditions = configuration.getConditions();
        for (ConfigCondition configCondition : configConditions) {
            conditionHashMap.put(configCondition.getId(), createCondition(context, configCondition.getType()));
        }
    }

    /**
     * Returns an instance of <code>ConditionManager</code>.
     * @param context Android context.
     * @return An instance of <code>ConditionManager</code>.
     */
    public static ConditionManager getInstance(Context context){
        if(instance == null)
            instance = new ConditionManager(context);
        return instance;
    }

    /**
     * Clears this instance of <code>ConditionManager</code>.
     */
    public static void clear(){
        instance = null;
    }

    /**
     * Creates a condition manager of the given type.
     * @param context Android context.
     * @param type Type of condition manager to make.
     * @return A condition manager of the given type.
     */
    private Condition createCondition(Context context, String type){
        switch(type){
            case TYPE_PHONE_BATTERY:
                return new BatteryLevelManager(context);
            case TYPE_DATA_QUALITY:
                return new DataQualityManager(context);
            case TYPE_NOT_DRIVING:
                return new DrivingDetectorManager(context);
            case TYPE_LAST_EMA_EMI:
                return new LastEmaEmiManager(context);
            case TYPE_NOT_ACTIVE:
                return new NotActiveManager(context);
            case TYPE_NO_SELF_REPORT:
                return new NoSelfReportManager(context);
            case TYPE_VALID_BLOCK:
                return new ValidBlockManager(context);
            case TYPE_PRIVACY:
                return new PrivacyManager(context);
            case TYPE_EMA_ANSWER:
                return new EmaAnswerManager(context);
            case TYPE_EMA_LIMIT:
                return new EMALimitManager(context);
            case TYPE_BONUS_INCENTIVE:
                return new BonusIncentive(context);
        }
        return null;
    }

    /**
     * Returns whether all conditions are valid or not.
     * @param conditions Array of conditions.
     * @param type Type of condition.
     * @param id Id for the log.
     * @return Whether all conditions are valid or not.
     * @throws DataKitException
     */
    public boolean isValid(String[] conditions, String type, String id) throws DataKitException {
        if(conditions == null)
            return true;
        boolean isTrue = true;
        for (String condition : conditions) {
            Log.d(TAG, "condition=" + condition);
            ConfigCondition configCondition = configuration.getConditions(condition);
            if (!conditionHashMap.get(condition).isValid(configCondition)) {
                Log.d(TAG, "condition=" + condition + " false");
                isTrue = false;
            }else{
                Log.d(TAG, "condition=" + condition + " true");
            }
        }
        if(isTrue) {
            log(type, id, "true: all conditions okay");
            return true;
        }else{
            log(type, id, "false: some conditions are failed");
            return false;
        }
    }

    /**
     * Logs the given message for the given condition.
     * @param type Type of condition.
     * @param id Id of the log.
     * @param message Message to log.
     * @throws DataKitException
     */
    private void log(String type, String id, String message) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setOperation(LogInfo.OP_CONDITION);
        logInfo.setId(id);
        logInfo.setType(type);
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage(message);
        LoggerManager.getInstance(context).insert(logInfo);
    }
}
