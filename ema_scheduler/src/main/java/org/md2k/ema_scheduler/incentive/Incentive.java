package org.md2k.ema_scheduler.incentive;

/**
 * Created by monowar on 4/1/16.
 */
public class Incentive {
    long timeStamp;
    String emaType;
    String emaId;
    int blockNumber;
    double dataQuality;
    double incentive;
    double totalIncentive;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getEmaType() {
        return emaType;
    }

    public void setEmaType(String emaType) {
        this.emaType = emaType;
    }

    public String getEmaId() {
        return emaId;
    }

    public void setEmaId(String emaId) {
        this.emaId = emaId;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public double getDataQuality() {
        return dataQuality;
    }

    public void setDataQuality(double dataQuality) {
        this.dataQuality = dataQuality;
    }

    public double getIncentive() {
        return incentive;
    }

    public void setIncentive(double incentive) {
        this.incentive = incentive;
    }

    public double getTotalIncentive() {
        return totalIncentive;
    }

    public void setTotalIncentive(double totalIncentive) {
        this.totalIncentive = totalIncentive;
    }
}
