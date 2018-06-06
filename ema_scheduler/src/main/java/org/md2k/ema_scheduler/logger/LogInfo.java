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

package org.md2k.ema_scheduler.logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides status constants and log metadata.
 */
public class LogInfo {
    public static final String OP_SCHEDULE = "SCHEDULE";
    public static final String OP_DELIVER = "DELIVER";
    public static final String OP_NOTIFICATION = "NOTIFICATION";
    public static final String OP_NOTIFICATION_RESPONSE = "NOTIFICATION_RESPONSE";
    public static final String OP_RUN = "RUN";
    public static final String OP_CONDITION = "CONDITION";
    public static final String OP_EMI_INFO = "EMI_INFO";
    public static final String STATUS_SCHEDULER_ALREADY_DELIVERED = "ALREADY_DELIVERED";
    public static final String STATUS_SCHEDULER_SCHEDULED = "SCHEDULED";
    public static final String STATUS_SCHEDULER_NO_VALID_BLOCK = "NO_VALID_BLOCK";
    public static final String STATUS_SCHEDULER_NO_TIME_LEFT = "NO_TIME_LEFT";
    public static final String STATUS_DELIVER_ALREADY_RUNNING = "ALREADY_RUNNING";
    public static final String STATUS_DELIVER_SUCCESS = "DELIVERED";
    public static final String STATUS_NOTIFICATION_NOTIFYING = "NOTIFY";
    public static final String STATUS_NOTIFICATION_RESPONSE_DELAY = "DELAY";
    public static final String STATUS_RUN_MISSED = "MISSED";
    public static final String STATUS_RUN_COMPLETED = "COMPLETED";
    public static final String STATUS_RUN_ABANDONED_BY_TIMEOUT ="ABANDONED_BY_TIMEOUT";
    public static final String STATUS_RUN_ABANDONED_BY_USER = "ABANDONED_BY_USER";
    public static final String STATUS_RUN_START = "START";

    private String operation;
    private String type;
    private String id;
    private String message;
    private long timestamp;
    private LogSchedule logSchedule;
    private EMIInfo emiInfo;
    private String status;
    private String current_time;

    /**
     * Returns the type.
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the message.
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the operation.
     * @return The operation.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation.
     * @param operation Operation to set.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Returns the timestamp.
     * @return The timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     * @param timestamp The timestamp.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.current_time = formatTime(timestamp);
    }

    /**
     * Returns the given timestamp in "yyy/MM/dd hh:mm:ss a" format.
     * @param timestamp Timestamp to format.
     * @return The given timestamp in "yyy/MM/dd hh:mm:ss a" format.
     */
    private String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {}
        return "";
    }

    /**
     * Returns the id.
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param id Id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the log schedule.
     * @return The log schedule.
     */
    public LogSchedule getLogSchedule() {
        return logSchedule;
    }

    /**
     * Sets the log schedule.
     * @param logSchedule Schedule for the log.
     */
    public void setLogSchedule(LogSchedule logSchedule) {
        this.logSchedule = logSchedule;
    }

    /**
     * Returns the EMI info.
     * @return The EMI info.
     */
    public EMIInfo getEmiInfo() {
        return emiInfo;
    }

    /**
     * Sets the EMI info.
     * @param emiInfo EMI info to set.
     */
    public void setEmiInfo(EMIInfo emiInfo) {
        this.emiInfo = emiInfo;
    }

    /**
     * Returns the status.
     * @return The status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status The status.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
