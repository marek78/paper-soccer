/**
 * MIT License
 * 
 * Copyright (c) 2019 Marek Szajna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.szajna.games.ossoccer.ai;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.szajna.games.ossoccer.ai.PoolArrayLong;

public class PoolArrayLongTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testCreate()
    {
        PoolArrayLong array = new PoolArrayLong();
        assertTrue(array.size() == 0);
    }

    @Test
    public void testAddSimple()
    {
        PoolArrayLong array = new PoolArrayLong();

        long testValue = 133;
        array.add(testValue);

        assertEquals(array.size(), 1);
        assertEquals(array.get(0), testValue);

        array.clear();
        assertEquals(array.size(), 0);
    }

    @Test
    public void testAdd()
    {
        PoolArrayLong array = new PoolArrayLong();

        int testValuesCount = 100000;
        for (int i = 0; i < testValuesCount; ++i)
        {
            array.add(i);
        }
        assertEquals(array.size(), testValuesCount);
        for (int i = 0; i < testValuesCount; ++i)
        {
            assertEquals(array.get(i), i);
        }
    }
}
