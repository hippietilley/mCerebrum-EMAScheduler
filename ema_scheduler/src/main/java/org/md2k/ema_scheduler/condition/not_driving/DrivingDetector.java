package org.md2k.ema_scheduler.condition.not_driving;

import org.md2k.datakitapi.time.DateTime;

/**
 * Created by hsarker on 10/1/2015.
 */
public class DrivingDetector {

    public enum DrivingStatus {
        DRIVING,
        DRIVING_STOP_SIGN,
        NOT_DRIVING,
        UNKNOWN
    }

    /**
     * Bohannon, R. Comfortable and maximum walking speed of adults aged 20-79 years: Reference values and determinants. Age and Ageing 26, 1 (1997), 15–19.
     */
    public static final double maxGaitSpeed = 2.533; // meter/second
    public static final double maxStopSignTime = 5*60*1000; // 5 minute
    public static final double maxGpsMissingTime = 30*1000; // 30 second

    double lastSpeed = -1;
    long lastReceivedSampleGPS=-1;
    long lastDrivingSampleGPS=-1;

    public void setSpeed(double speed) {
        long currentTimestamp = DateTime.getDateTime();
        this.lastSpeed = speed;
        this.lastReceivedSampleGPS = currentTimestamp;
        if(speed>maxGaitSpeed) {
            this.lastDrivingSampleGPS = currentTimestamp;
        }
    }

    public DrivingStatus getDrivingStatus() {
        long currentTimestamp = DateTime.getDateTime();
        if(this.lastReceivedSampleGPS==-1 || currentTimestamp-this.lastReceivedSampleGPS > maxGpsMissingTime) {
            return DrivingStatus.UNKNOWN;
        }
        if(this.lastDrivingSampleGPS==-1) {
            return DrivingStatus.NOT_DRIVING;
        }
        if(currentTimestamp-this.lastDrivingSampleGPS < maxStopSignTime) {
            if(this.lastSpeed>maxGaitSpeed) {
                return DrivingStatus.DRIVING;
            } else {
                return DrivingStatus.DRIVING_STOP_SIGN;
            }
        }
        return DrivingStatus.NOT_DRIVING;
    }
}
