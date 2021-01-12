/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.model;

import net.fortuna.ical4j.util.Numbers;

import java.io.Serializable;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * $Id$ [29-May-2004]
 *
 * Defines a list of numbers.
 * 
 * @author Ben Fortuna
 */
public class NumberList extends ArrayList<Integer> implements Serializable {
    
    private static final long serialVersionUID = -1667481795613729889L;

    private final ValueRange range;

    private final boolean allowsNegativeValues;
    
    /**
     * Default constructor.
     */
    public NumberList() {
    	this(Integer.MIN_VALUE, Integer.MAX_VALUE, true);
    }

    /**
     * Constructor with limits.
     * @param minValue the minimum allowable value
     * @param maxValue the maximum allowable value
     * @param allowsNegativeValues indicates whether negative values are allowed
     */
    public NumberList(int minValue, int maxValue, boolean allowsNegativeValues) {
        this(ValueRange.of(minValue, maxValue), allowsNegativeValues);
    }

    public NumberList(ValueRange range, boolean allowsNegativeValues) {
        this.range = range;
        this.allowsNegativeValues = allowsNegativeValues;
    }

    /**
     * Constructor.
     * @param aString a string representation of a number list
     */
    public NumberList(final String aString) {
    	this(aString, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
    }
    
    /**
     * @param aString a string representation of a number list
     * @param minValue the minimum allowable value
     * @param maxValue the maximum allowable value
     * @param allowsNegativeValues indicates whether negative values are allowed
     */
    public NumberList(final String aString, int minValue, int maxValue, boolean allowsNegativeValues) {
    	this(minValue, maxValue, allowsNegativeValues);
        addAll(Arrays.stream(aString.split(",")).parallel().map(Numbers::parseInt).collect(Collectors.toList()));
    }

    /**
     * @param aNumber a number to add to the list
     * @return true if the number was added, otherwise false
     */
    @Override
    public final boolean add(final Integer aNumber) {
        int abs = aNumber;
        if ((abs >> 31 | -abs >>> 31) < 0) {
            if (!allowsNegativeValues) {
                throw new IllegalArgumentException("Negative value not allowed: " + aNumber);
            }
            abs = Math.abs(abs);
        }
        if (!range.isValidIntValue(abs)) {
    		throw new IllegalArgumentException(
    		        "Value not in range [" + range.getMinimum() + ".." + range.getMaximum() + "]: " + aNumber);
    	}
        return super.add(aNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toString(this);
    }

    public static String toString(List<Integer> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static NumberList parse(String numberString) {
        NumberList retVal = new NumberList();
        retVal.addAll(Arrays.stream(numberString.split(",")).parallel().map(Numbers::parseInt).collect(Collectors.toList()));
        return retVal;
    }
}
