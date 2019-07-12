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

package com.szajna.games.ossoccer.toobox;

import com.szajna.games.ossoccer.field.Move;

public class Tools
{
    public static class MoveShift
    {
        public MoveShift(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;
    }

    private static MoveShift MOVE_SHIFT_BY_DIRECTION[] = { new MoveShift(0, -1), new MoveShift(1, -1),
            new MoveShift(1, 0), new MoveShift(1, 1), new MoveShift(0, 1), new MoveShift(-1, 1), new MoveShift(-1, 0),
            new MoveShift(-1, -1) };

    public static MoveShift getMoveShiftByDirection(int moveDirection)
    {
        assert (Tools.isInRange(moveDirection, Move.DIRECTION_0, Move.DIRECTION_7));
        return MOVE_SHIFT_BY_DIRECTION[moveDirection];
    }

    public static int getOppositeMoveDirection(int moveDirection)
    {
        return (moveDirection < 4 ? moveDirection + 4 : moveDirection - 4);
    }

    public static int cutToRange(int value, int min, int max)
    {
        return value < min ? min : value > max ? max : value;
    }

    public static float cutToRange(float value, float min, float max)
    {
        return value < min ? min : value > max ? max : value;
    }

    public static boolean isInRange(float value, float min, float max)
    {
        return value < min ? false : value > max ? false : true;
    }

    public static boolean isInRange(int value, int min, int max)
    {
        return value < min ? false : value > max ? false : true;
    }

    public static float normalizeAngle(float angle)
    {
        while (angle > 360)
        {
            angle -= 360;
        }
        return angle;
    }

    /**
     * No instance allowed.
     */
    private Tools()
    {
    }
}
