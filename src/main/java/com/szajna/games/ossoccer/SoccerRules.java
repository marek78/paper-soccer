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

package com.szajna.games.ossoccer;

import com.szajna.games.ossoccer.field.Edge;
import com.szajna.games.ossoccer.field.Field;
import com.szajna.games.ossoccer.field.Move;
import com.szajna.games.ossoccer.field.Node;

public class SoccerRules
{
    /**
     * Checks if the move in the direction is allowed at node.
     * 
     * @param node
     * @param direction
     * @return true if move is allowed, otherwise false.
     */
    public static boolean isMoveAllowed(Node node, int direction)
    {
        final Edge edge = node.getEdge(direction);
        if (edge == null || edge.isBlocked())
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Checks if any move allowed at node.
     * 
     * @param node
     * @return true if any move is allowed, otherwise false.
     */
    public static boolean isAnyMoveAllowed(Node node)
    {
        for (int dir = Move.DIRECTION_0; dir <= Move.DIRECTION_7; ++dir)
        {
            if (isMoveAllowed(node, dir))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any move allowed at node skipping exceptDirection.
     * 
     * @param node
     * @param exceptDirection
     * @return true if any move is allowed, otherwise false.
     */
    public static boolean isAnyMoveAllowed(Node node, int exceptDirection)
    {
        for (int dir = Move.DIRECTION_0; dir <= Move.DIRECTION_7; ++dir)
        {
            if (dir != exceptDirection && isMoveAllowed(node, dir))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if player has to re-bounce at node opposite to the Node node in the direction dir.
     * 
     * @param node
     * @param dir
     * @return true if player has to re-bounce at node opposite to the Node node in the direction dir.
     */
    public static boolean hasToRebounce(Node node, int dir)
    {

        final Edge edge = node.getEdge(dir);

        if (null == edge)
        {
            throw new IllegalArgumentException("Edge doesn't exists.");
        }
        if (edge.isBlocked())
        {
            throw new IllegalArgumentException("Edge already visited.");
        }

        final Node other = edge.getOpposite(node);
        return other.isMarked();
    }

    /**
     * Checks if goal has been scored.
     * 
     * @return true if goal has been scored, otherwise false.
     */
    public static boolean isGoalScored(final Field field)
    {
        return (field.getCurrent().getY() == 0) || (field.getCurrent().getY() == field.getHeight() - 1);
    }
}
