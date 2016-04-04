package org.md2k.ema_scheduler.logger;

/**
 * Created by monowar on 3/14/16.
 */
public class LogInfo {
    public static final String OP_SCHEDULE="SCHEDULE";
    public static final String OP_DELIVER="DELIVER";
    public static final String OP_NOTIFICATION="NOTIFICATION";
    public static final String OP_NOTIFICATION_RESPONSE="NOTIFICATION_RESPONSE";
    public static final String OP_RUN="RUN";
    public static final String OP_CONDITION="CONDITION";
    public static final String OP_EMI_INFO="EMI_INFO";
    public static final String STATUS_SCHEDULER_DELIVERED="ALREADY_DELIVERED";
    public static final String STATUS_SCHEDULER_RUNNING="APPLICATION_RUNNING";
    public static final String STATUS_SCHEDULER_SCHEDULED="SCHEDULED";
    public static final String STATUS_SCHEDULER_NO_VALID_BLOCK="NO_VALID_BLOCK";
    public static final String STATUS_DELIVER_ALREADY_RUNNING="ALREADY_RUNNING";
    public static final String STATUS_DELIVER_SUCCESS="DELIVERED";
    public static final String STATUS_NOTIFICATION_NOTIFYING="NOTIFY";
    public static final String STATUS_NOTIFICATION_RESPONSE_DELAY="DELAY";

    public static final String STATUS_RUN_MISSED="MISSED";
    public static final String STATUS_RUN_COMPLETED="COMPLETED";
    public static final String STATUS_RUN_ABANDONED_BY_TIMEOUT ="ABANDONED_BY_TIMEOUT";
    public static final String STATUS_RUN_ABANDONED_BY_USER="ABANDONED_BY_USER";
    public static final String STATUS_RUN_START="START";


    String operation;
    String type;
    String id;
    String message;
    long timestamp;
    LogSchedule logSchedule;
    EMIInfo emiInfo;
    String status;

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
