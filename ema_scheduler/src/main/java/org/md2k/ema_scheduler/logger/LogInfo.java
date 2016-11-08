package org.md2k.ema_scheduler.logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
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
public class LogInfo {
    public static final String OP_SCHEDULE="SCHEDULE";
    public static final String OP_DELIVER="DELIVER";
    public static final String OP_NOTIFICATION="NOTIFICATION";
    public static final String OP_NOTIFICATION_RESPONSE="NOTIFICATION_RESPONSE";
    public static final String OP_RUN="RUN";
    public static final String OP_CONDITION="CONDITION";
    public static final String OP_EMI_INFO="EMI_INFO";
    public static final String STATUS_SCHEDULER_ALREADY_DELIVERED ="ALREADY_DELIVERED";
    public static final String STATUS_SCHEDULER_SCHEDULED="SCHEDULED";
    public static final String STATUS_SCHEDULER_NO_VALID_BLOCK="NO_VALID_BLOCK";
    public static final String STATUS_SCHEDULER_NO_TIME_LEFT="NO_TIME_LEFT";
    public static final String STATUS_DELIVER_ALREADY_RUNNING="ALREADY_RUNNING";
    public static final String STATUS_DELIVER_SUCCESS="DELIVERED";
    public static final String STATUS_NOTIFICATION_NOTIFYING="NOTIFY";
    public static final String STATUS_NOTIFICATION_RESPONSE_DELAY="DELAY";

    public static final String STATUS_RUN_MISSED="MISSED";
    public static final String STATUS_RUN_COMPLETED="COMPLETED";
    public static final String STATUS_RUN_ABANDONED_BY_TIMEOUT ="ABANDONED_BY_TIMEOUT";
    public static final String STATUS_RUN_ABANDONED_BY_USER="ABANDONED_BY_USER";
    public static final String STATUS_RUN_START="START";


    private String operation;
    private String type;
    private String id;
    private String message;
    private long timestamp;
    private LogSchedule logSchedule;
    private EMIInfo emiInfo;
    private String status;
    private String current_time;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.current_time = formatTime(timestamp);
    }

    private String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LogSchedule getLogSchedule() {
        return logSchedule;
    }

    public void setLogSchedule(LogSchedule logSchedule) {
        this.logSchedule = logSchedule;
    }

    public EMIInfo getEmiInfo() {
        return emiInfo;
    }

    public void setEmiInfo(EMIInfo emiInfo) {
        this.emiInfo = emiInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
