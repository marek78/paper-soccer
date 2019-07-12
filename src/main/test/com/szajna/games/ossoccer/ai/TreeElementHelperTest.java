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

import com.szajna.games.ossoccer.ai.EstimationTree.TreeElementHelper;
import com.szajna.games.ossoccer.field.Move;

public class TreeElementHelperTest
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
    public void testGetParentIndex()
    {
        for (int testValue = -1; testValue <= 1000000; ++testValue)
        {
            long treeElement = TreeElementHelper.valueOf(testValue, (short) 0, (byte) 0, (byte) 0, false, false);
            assertEquals(testValue, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }

        int testValues[] = { 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000 };
        for (int i = 0; i < testValues.length; ++i)
        {
            long treeElement = TreeElementHelper.valueOf(testValues[i], (short) 0, (byte) 0, (byte) 0, false, false);
            assertEquals(testValues[i], TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testGetEstimation()
    {
        for (int testValue = Byte.MIN_VALUE; testValue <= Byte.MAX_VALUE; ++testValue)
        {
            long treeElement = TreeElementHelper.valueOf(0, (short) testValue, (byte) 0, (byte) 0, false, false);
            assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(testValue, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testGetMoveDepth()
    {
        for (int testValue = Byte.MIN_VALUE; testValue <= Byte.MAX_VALUE; ++testValue)
        {
            long treeElement = TreeElementHelper.valueOf(0, (short) 0, (byte) testValue, (byte) 0, false, false);
            assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(testValue, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testGetMoveDirection()
    {
        for (int testValue = Move.DIRECTION_0; testValue <= Move.DIRECTION_7; ++testValue)
        {
            long treeElement = TreeElementHelper.valueOf(0, (short) 0, (byte) 0, (byte) testValue, false, false);
            assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(testValue, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testIsAnalysisComplete()
    {
        boolean testValues[] = { true, false };
        for (int i = 0; i < testValues.length; ++i)
        {
            long treeElement = TreeElementHelper.valueOf(0, (short) 0, (byte) 0, (byte) 0, testValues[i], false);
            assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(testValues[i], TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(false, TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testIsPathEnd()
    {
        boolean testValues[] = { true, false };
        for (int i = 0; i < testValues.length; ++i)
        {
            long treeElement = TreeElementHelper.valueOf(0, (short) 0, (byte) 0, (byte) 0, false, testValues[i]);
            assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
            assertEquals(0, TreeElementHelper.getEstimation(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
            assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
            assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
            assertEquals(testValues[i], TreeElementHelper.isPathEnd(treeElement));
        }
    }

    @Test
    public void testSetAnalysisComplete()
    {
        long treeElement = TreeElementHelper.valueOf(0, (short) 0, (byte) 0, (byte) 0, false, true);
        assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
        assertEquals(0, TreeElementHelper.getEstimation(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
        assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
        assertEquals(true, TreeElementHelper.isPathEnd(treeElement));

        treeElement = TreeElementHelper.setAnalysisComplete(treeElement, true);
        assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
        assertEquals(0, TreeElementHelper.getEstimation(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
        assertEquals(true, TreeElementHelper.isAnalysisComplete(treeElement));
        assertEquals(true, TreeElementHelper.isPathEnd(treeElement));

        treeElement = TreeElementHelper.setAnalysisComplete(treeElement, false);
        assertEquals(0, TreeElementHelper.getParentIndex(treeElement));
        assertEquals(0, TreeElementHelper.getEstimation(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDepth(treeElement));
        assertEquals(0, TreeElementHelper.getMoveDirection(treeElement));
        assertEquals(false, TreeElementHelper.isAnalysisComplete(treeElement));
        assertEquals(true, TreeElementHelper.isPathEnd(treeElement));
    }
}
