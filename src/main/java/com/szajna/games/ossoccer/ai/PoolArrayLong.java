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

import java.util.ArrayList;

public class PoolArrayLong
{
    private static class Pool
    {
        /** 8192 * 8 = 64kB */
        private static final int POOL_ELEMENTS_COUNT = 8192;
        private long data[];
        private int size;

        Pool()
        {
            data = new long[POOL_ELEMENTS_COUNT];
            size = 0;
        }
    }

    private ArrayList<Pool> pools;
    private int size;
    private int reservedSize;

    public PoolArrayLong()
    {
        clear();
    }

    /**
     * Get container size.
     * 
     * @return container size.
     */
    public int size()
    {
        return size;
    }

    /**
     * Clears the container.
     */
    public void clear()
    {
        pools = new ArrayList<PoolArrayLong.Pool>();
        size = 0;
        reservedSize = 0;
    }

    /**
     * Adds element to the container.
     * 
     * @param element
     */
    public void add(long element)
    {
        if (size == reservedSize)
        {
            // another pool required
            pools.add(new Pool());
            reservedSize += Pool.POOL_ELEMENTS_COUNT;
        }

        Pool lastPool = pools.get(pools.size() - 1);
        lastPool.data[lastPool.size++] = element;
        size++;
    }

    /**
     * Sets element at index.
     * 
     * @param element
     * @param index
     */
    public void set(final long element, final int index)
    {
        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds: [0, " + (size - 1) + "]");
        }
        int poolIndex = index / Pool.POOL_ELEMENTS_COUNT;
        int elementIndex = index % Pool.POOL_ELEMENTS_COUNT;

        pools.get(poolIndex).data[elementIndex] = element;
    }

    /**
     * Gets element at index.
     * 
     * @param index
     * @return element at index
     */
    public long get(int index)
    {
        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds: [0, " + (size - 1) + "]");
        }

        int poolIndex = index / Pool.POOL_ELEMENTS_COUNT;
        int elementIndex = index % Pool.POOL_ELEMENTS_COUNT;

        return pools.get(poolIndex).data[elementIndex];
    }
}
