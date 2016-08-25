package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Block;

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
