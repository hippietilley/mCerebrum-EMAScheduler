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

package org.md2k.ema_scheduler.condition.not_driving;

/**
 * Determines if the device is in a moving vehicle.
 */
class DrivingDetector {

    public static final double maxStopSignTime = 5 * 60 * 1000; // 5 minute
    /**
     * Bohannon, R. Comfortable and maximum walking speed of adults aged 20-79 years: Reference values and determinants. Age and Ageing 26, 1 (1997), 15–19.
     */
    private static final double maxGaitSpeed = 2.533; // meters per second
    private static final double maxGpsMissingTime = 30 * 1000; // 30 second

    /**
     * Returns the driving status.
     * @param lastTimestamp Last recorded timestamp.
     * @param currentTimestamp Current timestamp.
     * @param speed Current speed.
     * @return The driving status.
     */
    DrivingStatus getDrivingStatus(long lastTimestamp, long currentTimestamp, double speed) {
        if(lastTimestamp == -1 || currentTimestamp - lastTimestamp > maxGpsMissingTime) {
            return DrivingStatus.UNKNOWN;
        }
        if(speed > maxGaitSpeed)
            return DrivingStatus.DRIVING;
        else return DrivingStatus.NOT_DRIVING;
    }

    /**
     * Enumeration of possible driving statuses:
     * <ul>
     *     <li>Driving</li>
     *     <li>Driving, but stopped at a stop sign</li>
     *     <li>Not driving</li>
     *     <li>Unknown</li>
     * </ul>
     */
    enum DrivingStatus {
        DRIVING,
        DRIVING_STOP_SIGN,
        NOT_DRIVING,
        UNKNOWN
    }
}
