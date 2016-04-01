package org.md2k.ema_scheduler.logger;

/**
 * Created by monowar on 3/14/16.
 */
public class LogInfo {
    public static final String OP_MESSAGE="MESSAGE";
    public static final String OP_SCHEDULE="SCHEDULE";
    public static final String OP_DELIVER="DELIVER";
    public static final String OP_NOTIFY="NOTIFY";
    public static final String OP_NOTIFICATION_RESPONSE="NOTIFICATION_RESPONSE";
    public static final String OP_RUN="RUN";
    public static final String OP_SCHEDULER_RUN="SCHEDULER_RUN";
    public static final String OP_CONDITION="CONDITION";
    public static final String OP_EMI_INFO="EMI_INFO";
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
