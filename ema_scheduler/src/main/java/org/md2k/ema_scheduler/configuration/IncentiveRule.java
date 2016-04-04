package org.md2k.ema_scheduler.configuration;

/**
 * Created by monowar on 3/14/16.
 */
public class IncentiveRule {
    double incentive;
    String[] messages;
    String[] conditions;

    public double getIncentive() {
        return incentive;
    }

    public String[] getConditions() {
        return conditions;
    }

    public String[] getMessages() {
        return messages;
    }
}
