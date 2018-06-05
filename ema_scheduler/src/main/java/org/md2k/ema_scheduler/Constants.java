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

package org.md2k.ema_scheduler;

import android.os.Environment;

/**
 * This class provides common constant string values used across the application.
 */
public class Constants {
    /**
     * Directory of the configuration files.
     */
    public static final String CONFIG_DIRECTORY= Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/mCerebrum/org.md2k.ema_scheduler/";
    /**
     * Name of the configuration file. Default is <code>"config.json"</code>.
     */
    public static final String CONFIG_FILENAME = "config.json";
    /**
     * Name of the notification file. Default is <code>"notification.json"</code>.
     */
    public static final String NOTIFICATION_FILENAME = "notification.json";
    /**
     * Name of the condition file. Default is <code>"condition.json"</code>.
     */
    public static final String CONDITION_FILENAME = "condition.json";
    /**
     * Debug mode is set to false by default.
     */
    public static final boolean DEBUG = false;
    /**
     * Name of the log file. Default is <code>"log.json"</code>.
     */
    public static final String LOG_FILENAME = "log.json";
    /**
     * Default is <code>"INTENT_USER"</code>.
     */
    public static final String INTENT_USER = "INTENT_USER";
}
