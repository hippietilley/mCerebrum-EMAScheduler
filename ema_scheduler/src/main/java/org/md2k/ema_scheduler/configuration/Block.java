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
 * Defines a <code>Block</code> object.
 */
public class Block implements Serializable {
    public static final String BASE_DAY_START = "DAY_START";
    private int total;
    private String base;
    private long start_offset;
    private long end_offset;

    /**
     * Returns the total.
     * @return The total.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns the base.
     * @return The base.
     */
    public String getBase() {
        return base;
    }

    /**
     * Returns the starting offset.
     * @return The starting offset.
     */
    public long getStart_offset() {
        return start_offset;
    }

    /**
     * Returns the ending offset.
     * @return The ending offset.
     */
    public long getEnd_offset() {
        return end_offset;
    }
}
