package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Block;

/**
 * Created by monowar on 3/14/16.
 */
public class BlockManager {
    Context context;
    Block[] blocks;
    public BlockManager(Context context, Block[] blocks){
        this.blocks = blocks;
        this.context=context;
    }

    int getBlockIndex(long dayStartTimestamp, long timestamp){
        for(int i=0;i< blocks.length;i++){
            if(blocks[i].getBase().equals(Block.BASE_DAY_START)){
                long startTime=dayStartTimestamp+ blocks[i].getStart_offset();
                long endTime=dayStartTimestamp+ blocks[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    return i;
            }
        }
        return -1;
    }
    long getBlockStartTime(long dayStartTimestamp, long timestamp){
        for(int i=0;i< blocks.length;i++){
            if(blocks[i].getBase().equals(Block.BASE_DAY_START)){
                long startTime=dayStartTimestamp+ blocks[i].getStart_offset();
                long endTime=dayStartTimestamp+ blocks[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<endTime)
                    return startTime;
            }
        }
        return -1;
    }
    long getNextBlockStartTime(long dayStartTimestamp, long timestamp){
        for(int i=0;i< blocks.length;i++){
            if(blocks[i].getBase().equals(Block.BASE_DAY_START)){
                long startTime=dayStartTimestamp+ blocks[i].getStart_offset();
                long endTime=dayStartTimestamp+ blocks[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    if(i< blocks.length-1)
                    return dayStartTimestamp+ blocks[i+1].getStart_offset();
            }
        }
        return -1;
    }

    long getBlockEndTime(long dayStartTimestamp, long timestamp){
        for(int i=0;i< blocks.length;i++){
            if(blocks[i].getBase().equals(Block.BASE_DAY_START)){
                long startTime=dayStartTimestamp+ blocks[i].getStart_offset();
                long endTime=dayStartTimestamp+ blocks[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    return endTime;
            }
        }
        return -1;
    }

    public Block[] getBlocks() {
        return blocks;
    }
}
