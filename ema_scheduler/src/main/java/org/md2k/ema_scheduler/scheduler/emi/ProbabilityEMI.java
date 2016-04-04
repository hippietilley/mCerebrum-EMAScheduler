package org.md2k.ema_scheduler.scheduler.emi;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.logger.EMIInfo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by monowar on 3/29/16.
 */
public class ProbabilityEMI {
    Context context;
    EMIHistoryManager emiHistoryManager;
    public static double LAMBDA = 0.3;
    EMIInfo emiInfo;
    long dayStartTime;

    public ProbabilityEMI(Context context, long dayStartTime, boolean isPreQuit, boolean isStress, String type, String id) {
        emiInfo=new EMIInfo();
        emiInfo.isPreQuit=isPreQuit;
        emiInfo.isStress=isStress;
        this.context = context;
        this.dayStartTime = dayStartTime;
        emiHistoryManager = new EMIHistoryManager(context, type, id);
    }

    public void getProbability() {
        emiInfo.remainingTimeInMinute = getRemainingTimeInMinute(dayStartTime);
        emiInfo.G = FunctionG.getG(context, emiInfo.isPreQuit, emiInfo.isStress, emiInfo.remainingTimeInMinute);
        emiInfo.N = getN();
        emiInfo.sumLambda = getSumLambda();
        emiInfo.probability = (emiInfo.N - emiInfo.sumLambda) / (1 + emiInfo.G);
        emiInfo.probability = truncateIfRequired(emiInfo.probability, emiInfo.isStress);
    }

    double getSumLambda() {
        double sumLambda=0;
        long curTime=DateTime.getDateTime();
        ArrayList<EMIInfo> emiHistoryArrayList=emiHistoryManager.getEmiHistories(emiInfo.isStress, dayStartTime, curTime);
        for(int i=0;i<emiHistoryArrayList.size();i++) {
            double lambdaT=getLambdaT(curTime-emiHistoryArrayList.get(i).timestamp);
            if(emiHistoryArrayList.get(i).isTriggered) {
                sumLambda = sumLambda + lambdaT+(1-lambdaT)*emiHistoryArrayList.get(i).probability;
            }else{
                sumLambda = sumLambda  +(1-lambdaT)*emiHistoryArrayList.get(i).probability;
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
        }else{
            if (probability < 0.0) probability = 0.0;
            if (probability > 1.0) probability = 1.0;
        }
        return probability;
    }

    private double getN() {
        if (emiInfo.isStress) return 3.0f;
        if (emiInfo.isPreQuit) return 1.75f;
        return 1.65f;
    }

    private int getRemainingTimeInMinute(long getStartTime) {
        return (int) (12*60-((DateTime.getDateTime() - getStartTime) / (1000 * 60)));

    }

    public boolean isTrigger() {
        getProbability();
        Random generator = new Random();
        double number = generator.nextDouble();
        if (number <= emiInfo.probability) emiInfo.isTriggered= true;
        else emiInfo.isTriggered= false;
        emiHistoryManager.insert(emiInfo);
        return emiInfo.isTriggered;
    }
}
