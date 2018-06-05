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

package org.md2k.ema_scheduler.incentive;

/**
 * Defines an incentive object.
 */
public class Incentive {
    long timeStamp;
    String emaType;
    String emaId;
    double incentive;
    double totalIncentive;
    private int blockNumber;
    private double dataQuality;
    int incentiveRule;

    /**
     * Returns the <code>incentiveRule</code>.
     * @return The <code>incentiveRule</code>.
     */
    public int getIncentiveRule() {
        return incentiveRule;
    }

    /**
     * Sets the <code>incentiveRule</code>.
     * @param incentiveRule The <code>incentiveRule</code>.
     */
    public void setIncentiveRule(int incentiveRule) {
        this.incentiveRule = incentiveRule;
    }

    /**
     * Returns the timestamp.
     * @return The timestamp.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the timestamp.
     * @param timeStamp The timestamp.
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Returns the type of the EMA.
     * @return The type of the EMA.
     */
    public String getEmaType() {
        return emaType;
    }

    /**
     * Sets the type of the EMA.
     * @param emaType The type of the EMA.
     */
    public void setEmaType(String emaType) {
        this.emaType = emaType;
    }

    /**
     * Returns the EMA id.
     * @return The EMA id.
     */
    public String getEmaId() {
        return emaId;
    }

    /**
     * Sets the EMA id.
     * @param emaId The EMA id.
     */
    public void setEmaId(String emaId) {
        this.emaId = emaId;
    }

    /**
     * Returns the block number.
     * @return The block number.
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    /**
     * Sets the block number.
     * @param blockNumber The block number.
     */
    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Returns the data quality value.
     * @return The data quality value.
     */
    public double getDataQuality() {
        return dataQuality;
    }

    /**
     * Sets the data quality value.
     * @param dataQuality The data quality value.
     */
    public void setDataQuality(double dataQuality) {
        this.dataQuality = dataQuality;
    }

    /**
     * Returns the incentive.
     * @return The incentive.
     */
    public double getIncentive() {
        return incentive;
    }

    /**
     * Sets the incentive.
     * @param incentive The incentive.
     */
    public void setIncentive(double incentive) {
        this.incentive = incentive;
    }

    /**
     * Returns the total incentive.
     * @return The total incentive.
     */
    double getTotalIncentive() {
        return totalIncentive;
    }

    /**
     * Sets the total incentive.
     * @param totalIncentive The total incentive.
     */
    public void setTotalIncentive(double totalIncentive) {
        this.totalIncentive = totalIncentive;
    }
}
