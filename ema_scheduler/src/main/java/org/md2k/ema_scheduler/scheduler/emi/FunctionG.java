package org.md2k.ema_scheduler.scheduler.emi;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/*
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
class FunctionG {
    public static double getG(Context context, boolean isPreLapse, boolean isStress, int remainingTime) {
        BufferedReader reader;
        String filename;
        double value=-1;
        try {
            if (isPreLapse)
                filename = "prelapse_g";
            else filename = "postlapse_g";
            InputStream file = context.getResources().openRawResource(
                    context.getResources().getIdentifier(filename,
                            "raw", context.getPackageName()));
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
                if(items.size()!=3) continue;
                if(Integer.parseInt(items.get(0))==remainingTime){
                    if(!isStress){
                        value=Double.parseDouble(items.get(1));
                    }
                    else value=Double.parseDouble(items.get(2));
                    break;
                }
            }
            reader.close();
            file.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return value;
    }
}
