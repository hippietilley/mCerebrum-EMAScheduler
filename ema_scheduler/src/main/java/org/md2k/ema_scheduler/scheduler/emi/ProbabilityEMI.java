package org.md2k.ema_scheduler.scheduler.emi;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.logger.EMIInfo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class ProbabilityEMI {
    public static double LAMBDA = 0.4;
    Context context;
    EMIHistoryManager emiHistoryManager;
    EMIInfo emiInfo;
    long dayStartTime;

    public ProbabilityEMI(Context context, long dayStartTime, boolean isPreLapse, boolean isStress, String type, String id) {
        emiInfo = new EMIInfo();
        emiInfo.isPreLapse = isPreLapse;
        emiInfo.isStress = isStress;
        this.context = context;
        this.dayStartTime = dayStartTime;
        emiHistoryManager = new EMIHistoryManager(context, type, id);
    }

    public void getProbability() {
        emiInfo.remainingTimeInMinute = getRemainingTimeInMinute(dayStartTime);
        emiInfo.G = FunctionG.getG(context, emiInfo.isPreLapse, emiInfo.isStress, emiInfo.remainingTimeInMinute);
        emiInfo.N = getN();
        emiInfo.sumLambda = getSumLambda();
        emiInfo.probability = (emiInfo.N - emiInfo.sumLambda) / (1 + emiInfo.G);
        emiInfo.probability = truncateIfRequired(emiInfo.probability, emiInfo.isStress);
    }

    double getSumLambda() {
        double sumLambda = 0;
        long curTime = DateTime.getDateTime();
        ArrayList<EMIInfo> emiHistoryArrayList = emiHistoryManager.getEmiHistories(emiInfo.isStress, dayStartTime, curTime);
        for (int i = 0; i < emiHistoryArrayList.size(); i++) {
            double lambdaT = getLambdaT(curTime - emiHistoryArrayList.get(i).timestamp);
            if (emiHistoryArrayList.get(i).isTriggered) {
                sumLambda = sumLambda + lambdaT + (1 - lambdaT) * emiHistoryArrayList.get(i).probability;
            } else {
                sumLambda = sumLambda + (1 - lambdaT) * emiHistoryArrayList.get(i).probability;
            }
        }
        return sumLambda;
    }

    double getLambdaT(long diff) {
        double timeInHour = ((double) (diff)) / (1000.0f * 60.0f * 60.0f);
        return Math.pow(LAMBDA, timeInHour);
    }

    private double truncateIfRequired(double probability, boolean isStress) {
        if (isStress) {
            if (probability < 0.05) probability = 0.05;
            if (probability > 0.95) probability = 0.95;
        } else {
            if (probability < 0.0) probability = 0.0;
            if (probability > 1.0) probability = 1.0;
        }
        return probability;
    }

    private double getN() {
        if (emiInfo.isStress == true && emiInfo.isPreLapse == true) return 2.25;
        if (emiInfo.isStress == true && emiInfo.isPreLapse == false) return 3.0;
        if (emiInfo.isStress == false && emiInfo.isPreLapse == true) return 1.60;
        if (emiInfo.isStress == false && emiInfo.isPreLapse == false) return 1.65;
        return 2.25;
    }

    private int getRemainingTimeInMinute(long getStartTime) {
        return (int) (12 * 60 - ((DateTime.getDateTime() - getStartTime) / (1000 * 60)));

    }

    public boolean isTrigger() throws DataKitException {
        getProbability();
        Random generator = new Random();
        double number = generator.nextDouble();
        emiInfo.random = number;
        if (number >= emiInfo.probability) emiInfo.isTriggered = true;
        else emiInfo.isTriggered = false;
        emiHistoryManager.insert(emiInfo);
        return emiInfo.isTriggered;
    }
}
