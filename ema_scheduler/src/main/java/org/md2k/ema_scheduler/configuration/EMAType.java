package org.md2k.ema_scheduler.configuration;

import java.io.Serializable;

/**
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
public class EMAType implements Serializable {
    public static final String TYPE_EMA = "EMA";
    public static final String TYPE_EMI = "EMI";
    public static final String ID_RANDOM_EMA="RANDOM_EMA";
    public static final String ID_SMOKING_EMA ="SMOKING_EMA";
    public static final String ID_STRESS_EMA ="STRESS_EMA";
    public static final String ID_END_OF_DAY_EMA="END_OF_DAY_EMA";
    public static final String ID_EMI="EMI";
    String id;
    String type;
    String trigger_type;
    String name;
    boolean enable;
    Application application;
    Block[] blocks;
    SchedulerRule[] scheduler_rules;
    Notification[] notifications;
    IncentiveRule[] incentive_rules;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Application getApplication() {
        return application;
    }

    public String getTrigger_type() {
        return trigger_type;
    }

    public Notification[] getNotifications() {
        return notifications;
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public SchedulerRule[] getScheduler_rules() {
        return scheduler_rules;
    }

    public boolean isEnable() {
        return enable;
    }

    public IncentiveRule[] getIncentive_rules() {
        return incentive_rules;
    }
}
