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

import java.io.Serializable;

/**
 * Provides methods for accessing attributes of <code>EMAType</code>.
 */
public class EMAType implements Serializable {
    public static final String TYPE_EMA = "EMA";
    public static final String TYPE_EMI = "EMI";
    public static final String ID_RANDOM_EMA="RANDOM_EMA";
    public static final String ID_SMOKING_EMA ="SMOKING_EMA";
    public static final String ID_STRESS_EMA ="STRESS_EMA";
    public static final String ID_END_OF_DAY_EMA="END_OF_DAY_EMA";
    public static final String ID_EMI="EMI";
    private String id;
    private String type;
    private String trigger_type;
    private String name;
    private boolean enable;
    private Application application;
    private Block[] blocks;
    private SchedulerRule[] scheduler_rules;
    private Notification[] notifications;
    private IncentiveRule[] incentive_rules;

    /**
     * Returns the id.
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type.
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the application.
     * @return The application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Returns the trigger type.
     * @return The trigger type.
     */
    public String getTrigger_type() {
        return trigger_type;
    }

    /**
     * Returns an array of notifications.
     * @return An array of notifications.
     */
    public Notification[] getNotifications() {
        return notifications;
    }

    /**
     * Returns an array of blocks.
     * @return An array of blocks.
     */
    public Block[] getBlocks() {
        return blocks;
    }

    /**
     * Returns an array of scheduler rules.
     * @return An array of scheduler rules.
     */
    public SchedulerRule[] getScheduler_rules() {
        return scheduler_rules;
    }

    /**
     * Returns whether this is enabled.
     * @return Whether this is enabled.
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Returns an array of incentive rules.
     * @return An array of incentive rules.
     */
    public IncentiveRule[] getIncentive_rules() {
        return incentive_rules;
    }
}
