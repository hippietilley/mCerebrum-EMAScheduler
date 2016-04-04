package org.md2k.ema_scheduler.condition.not_driving;

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


    public DrivingStatus getDrivingStatus(long lastTimestamp, long currentTimestamp, double speed) {
        if(lastTimestamp==-1 || currentTimestamp-lastTimestamp > maxGpsMissingTime) {
            return DrivingStatus.UNKNOWN;
        }
        if(speed>maxGaitSpeed) return DrivingStatus.DRIVING;
        else return DrivingStatus.NOT_DRIVING;
    }
}
