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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.szajna.games.ossoccer.field.Move;
import com.szajna.games.ossoccer.field.Path;
import com.szajna.games.ossoccer.field.Path.PathElement;
import com.szajna.util.Log;

import java.util.Random;
import java.util.Set;

/**
 * EstimationTree Used to find the best path by AI.
 * 
 */
public class EstimationTree
{
    private static final String LOG_TAG = "EstimationTree";

    /**
     * <b>TreeElementHelper methods</b><br>
     * <b>Rationale: speed / memory optimization</b><br>
     * 
     * <b>TreeElement (long) structure - starting from the lowest bit:</b><br>
     * 32 bits: parent index<br>
     * 16 bits: estimation<br>
     * 8 bits: move depth, values: 1 - max move depth - see {@link SimpleAIPlayer.Config}<br>
     * 3 bits: move direction<br>
     * 1 bit : analysisComplete<br>
     * 1 bit : pathEnd<br>
     * 3 bits: free - not used<br>
     */
    public static class TreeElementHelper
    {
        private static final long MASK_PARENT_INDEX = 0x00000000FFFFFFFFL;
        private static final long MASK_ESTIMATION = 0x0000FFFF00000000L;
        private static final long MASK_MOVE_DEPTH = 0x00FF000000000000L;
        private static final long MASK_MOVE_DIRECTION = 0x0700000000000000L;
        private static final long MASK_ANALYSIS_COMPLETE = 0x0800000000000000L;
        private static final long MASK_PATH_END = 0x1000000000000000L;

        /**
         * Use valueOf method to create TreeElement.
         */
        private TreeElementHelper()
        {
        };

        public static long valueOf(int parentIndex, short estimation, byte moveDepth, byte moveDirection,
                boolean analysisComplete, boolean pathEnd)
        {
            assert (moveDirection >= Move.DIRECTION_0 && moveDirection <= Move.DIRECTION_7);

            long packedData = ((long) parentIndex & MASK_PARENT_INDEX);
            packedData |= ((long) estimation << 32) & MASK_ESTIMATION;
            packedData |= ((long) moveDepth << 48) & MASK_MOVE_DEPTH;
            packedData |= ((long) moveDirection << 56) & MASK_MOVE_DIRECTION;

            if (analysisComplete)
                packedData |= MASK_ANALYSIS_COMPLETE;

            if (pathEnd)
                packedData |= MASK_PATH_END;

            return packedData;
        }

        public static int getParentIndex(long treeElement)
        {
            return (int) (treeElement);
        }

        public static short getEstimation(long treeElement)
        {
            return (short) (treeElement >>> 32);
        }

        public static byte getMoveDepth(long treeElement)
        {
            return (byte) (treeElement >>> 48);
        }

        public static byte getMoveDirection(long treeElement)
        {
            return (byte) ((treeElement & MASK_MOVE_DIRECTION) >>> 56);
        }

        public static boolean isAnalysisComplete(long treeElement)
        {
            return (treeElement & MASK_ANALYSIS_COMPLETE) != 0;
        }

        public static boolean isPathEnd(long treeElement)
        {
            return (treeElement & MASK_PATH_END) != 0;
        }

        public static boolean isOpponentAnalysisPossible(long treeElement)
        {
            short estimation = getEstimation(treeElement);
            return isPathEnd(treeElement) && estimation != EST_GAME_LOST && estimation != EST_GOAL_LOST
                    && estimation != EST_GOAL_SCORED;
        }

        public static long setAnalysisComplete(long treeElement, boolean analysisComplete)
        {
            if (analysisComplete)
                treeElement |= MASK_ANALYSIS_COMPLETE;
            else
                treeElement &= (~MASK_ANALYSIS_COMPLETE);

            return treeElement;
        }
    }

    /** TreeElementComparator. */
    private static final Comparator<Long> TreeElementComparator = new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2)
        {
            short est1 = TreeElementHelper.getEstimation(o1);
            short est2 = TreeElementHelper.getEstimation(o2);

            if (est1 > est2)
            {
                return -1;
            }
            else if (est1 < est2)
            {
                return 1;
            }
            else
            {
                int parentIndex1 = TreeElementHelper.getParentIndex(o1);
                int parentIndex2 = TreeElementHelper.getParentIndex(o2);

                if (parentIndex1 < parentIndex2)
                    return -1;
                else if (parentIndex1 > parentIndex2)
                    return 1;
                else
                    return 0;
            }
        }
    };

    // private static final short EST_WORST = Short.MIN_VALUE;
    private static final short EST_BEST = Short.MAX_VALUE;

    public static final short EST_GAME_LOST = Short.MIN_VALUE + 1;
    public static final short EST_GOAL_LOST = -10000;
    public static final short EST_GOAL_SCORED = 10000;

    private final Random randomizer;
    private final PoolArrayLong tree;
    private final int playerId;
    private boolean atLeastOnePathEndInTree;

    /**
     * Constructs EstimationTree.
     * 
     * @param playerId
     */
    public EstimationTree(final int playerId)
    {
        randomizer = new Random();
        tree = new PoolArrayLong();
        this.playerId = playerId;
        atLeastOnePathEndInTree = false;
    }

    /**
     * Clears the tree.
     */
    public void clear()
    {
        tree.clear();
        atLeastOnePathEndInTree = false;
    }

    /**
     * Check if there is at least one path end (complete move) element in the tree.
     * 
     * @return true if yes, false if no.
     */
    public boolean isAtLeastOnePathEndInTree()
    {
        return atLeastOnePathEndInTree;
    }

    /**
     * Adds element to tree.
     * 
     * @param treeElement
     */
    public void addElement(final long treeElement)
    {
        tree.add(treeElement);
        if (!atLeastOnePathEndInTree && TreeElementHelper.isPathEnd(treeElement))
        {
            atLeastOnePathEndInTree = true;
        }
    }

    /**
     * Sets element at index.
     * 
     * @param treeElement
     * @param index
     */
    public void setElement(final long treeElement, final int index)
    {
        tree.set(treeElement, index);
    }

    /**
     * Gets element at index.
     * 
     * @param index
     * @return TreeElement.
     */
    public long getElement(final int index)
    {
        return tree.get(index);
    }

    /**
     * Gets path to element at index.
     * 
     * @param index
     * @return Path.
     */
    public Path getPathToElement(final int index)
    {
        Path path = new Path();
        if (index < 0)
            return path;

        long treeElement = tree.get(index);
        byte moveDirection = TreeElementHelper.getMoveDirection(treeElement);
        path.addMove(PathElement.valueOf(moveDirection, (byte) playerId));
        int parentIndex = TreeElementHelper.getParentIndex(treeElement);

        while (parentIndex >= 0)
        {
            treeElement = tree.get(parentIndex);
            moveDirection = TreeElementHelper.getMoveDirection(treeElement);
            path.addMove(PathElement.valueOf(moveDirection, (byte) playerId));
            parentIndex = TreeElementHelper.getParentIndex(treeElement);
        }
        // reverse the path
        Collections.reverse(path.getMoves());
        return path;
    }

    /**
     * Gets path (moves) to element at index. NOTE: method rationale: speed optimization.
     * 
     * @param index
     * @param pathMoves - will be filled by the method. It has to be
     * @return pathMoves size (NOTE: differs from pathMoves.length)
     */
    public int getPathToElement(final int index, byte[] pathMoves)
    {
        int pathMovesSize = 0;
        long treeElement = tree.get(index);
        pathMoves[pathMovesSize++] = TreeElementHelper.getMoveDirection(treeElement);

        int parentIndex = TreeElementHelper.getParentIndex(treeElement);
        while (parentIndex >= 0)
        {
            treeElement = tree.get(parentIndex);
            pathMoves[pathMovesSize++] = TreeElementHelper.getMoveDirection(treeElement);
            parentIndex = TreeElementHelper.getParentIndex(treeElement);
        }
        // reverse array
        for (int i = 0; i < pathMovesSize / 2; i++)
        {
            byte tmp = pathMoves[pathMovesSize - i - 1];
            pathMoves[pathMovesSize - i - 1] = pathMoves[i];
            pathMoves[i] = tmp;
        }
        return pathMovesSize;
    }

    /**
     * Gets tree elements count.
     * 
     * @return elements count.
     */
    public int getElementsCount()
    {
        return tree.size();
    }

    /**
     * Checks if tree has elements for analysis starting at startIndex.
     * 
     * @param startIndex - start index; use 0 to check the whole tree.
     * @return true if tree has elements for analysis, otherwise false.
     */
    public boolean hasElementsForAnalysis(final int startIndex)
    {
        for (int i = startIndex; i < tree.size(); ++i)
        {
            if (!TreeElementHelper.isAnalysisComplete(tree.get(i)))
                return true;
        }
        return false;
    }

    /**
     * Gets the best path based on opponent move analysis.
     * 
     * @return Path
     */
    public Path getBestPath(final int opponentMoveStartIndex)
    {
        Log.d(LOG_TAG, "End points count [AI, Opponent]: " + getEndPointsCount(opponentMoveStartIndex, true) + ", "
                + getEndPointsCount(opponentMoveStartIndex, false));

        // check if goal scored
        for (int i = 0; i < opponentMoveStartIndex; ++i)
        {
            long treeElement = tree.get(i);
            if (TreeElementHelper.isPathEnd(treeElement)
                    && TreeElementHelper.getEstimation(treeElement) == EST_GOAL_SCORED)
            {
                return getPathToElement(i);
            }
        }

        // AI move indexes with opponent best estimation
        Map<Integer, Integer> aiMoveIndexes = new HashMap<Integer, Integer>();
        for (int i = opponentMoveStartIndex; i < tree.size(); ++i)
        {

            long treeElement = tree.get(i);
            if (TreeElementHelper.isPathEnd(treeElement))
            {
                int opponentMoveEstimation = TreeElementHelper.getEstimation(treeElement);
                while (TreeElementHelper.getParentIndex(treeElement) >= opponentMoveStartIndex)
                {
                    treeElement = tree.get(TreeElementHelper.getParentIndex(treeElement));
                }

                int parentIndex = TreeElementHelper.getParentIndex(treeElement);
                Integer opponentBestEstimation = aiMoveIndexes.get(parentIndex);

                if (opponentBestEstimation == null || opponentMoveEstimation > opponentBestEstimation)
                {
                    aiMoveIndexes.put(parentIndex, opponentMoveEstimation);
                }
            }
        }

        if (aiMoveIndexes.size() > 0)
        {
            // get the worst from opponents best replies
            int bestAiMoveIndex = -1;
            int bestAiMoveCount = 0;
            int worstOpponentBestReply = EST_BEST;

            Iterator<Entry<Integer, Integer>> it = aiMoveIndexes.entrySet().iterator();
            while (it.hasNext())
            {
                Entry<Integer, Integer> pair = it.next();
                if (bestAiMoveIndex == -1 || pair.getValue() < worstOpponentBestReply)
                {
                    bestAiMoveCount = 1;
                    bestAiMoveIndex = pair.getKey();
                    worstOpponentBestReply = pair.getValue();
                }
                else if (pair.getValue() == worstOpponentBestReply)
                {
                    ++bestAiMoveCount;
                }
            }

            // take random best move if more than one move with the same estimation
            if (bestAiMoveCount > 1)
            {
                int whichMove = randomizer.nextInt(bestAiMoveCount);
                int currentMove = 0;

                it = aiMoveIndexes.entrySet().iterator();
                while (it.hasNext())
                {
                    Entry<Integer, Integer> pair = it.next();
                    if (pair.getValue() == worstOpponentBestReply)
                    {
                        if (currentMove == whichMove)
                        {
                            bestAiMoveIndex = pair.getKey();
                        }
                        ++currentMove;
                    }
                }
            }
            return getPathToElement(bestAiMoveIndex);
        }
        else
        {
            Log.w(LOG_TAG, "aiMoveIndexes.size(): " + aiMoveIndexes.size()
                    + " This should not have happend! Please check the tree build limits!");
            return getBestPathAIAnalysis(opponentMoveStartIndex);
        }
    }

    /**
     * Gets the best path based on AI move analysis.
     * 
     * @return Path
     */
    public Path getBestPathAIAnalysis(final int opponentMoveStartIndex)
    {
        // finding opponent moves which are no good for us :)
        Set<Integer> noGoodMoveIndexSet = new HashSet<Integer>();

        for (int i = opponentMoveStartIndex; i < tree.size(); ++i)
        {
            long treeElement = tree.get(i);
            if (TreeElementHelper.isPathEnd(treeElement)
                    && TreeElementHelper.getEstimation(treeElement) == EST_GOAL_SCORED)
            {
                while (TreeElementHelper.getParentIndex(treeElement) >= opponentMoveStartIndex)
                {
                    treeElement = tree.get(TreeElementHelper.getParentIndex(treeElement));
                }
                noGoodMoveIndexSet.add(TreeElementHelper.getParentIndex(treeElement));
            }
        }
        // end of finding opponent moves which are no good for us

        ArrayList<Long> bestTreeElements = new ArrayList<Long>();
        for (int i = 0; i < opponentMoveStartIndex; ++i)
        {
            long treeElement = tree.get(i);
            if (TreeElementHelper.isPathEnd(treeElement) && !noGoodMoveIndexSet.contains(i))
            {
                bestTreeElements.add(treeElement);
            }
        }
        if (bestTreeElements.size() < 1)
        {
            for (int i = 0; i < opponentMoveStartIndex; ++i)
            {
                long treeElement = tree.get(i);
                if (TreeElementHelper.isPathEnd(treeElement))
                {
                    bestTreeElements.add(treeElement);
                }
            }
        }
        Collections.sort(bestTreeElements, TreeElementComparator);

        // get best move (random if there are more than one best moves)
        long bestTreeElement = bestTreeElements.get(0);

        short bestElementEstimation = TreeElementHelper.getEstimation(bestTreeElement);
        int maxParentIndex = TreeElementHelper.getParentIndex(bestTreeElement);
        int bestElementCount = bestTreeElements.size();

        for (int i = 1; i < bestTreeElements.size(); ++i)
        {
            long treeElement = bestTreeElements.get(i);
            if (TreeElementHelper.getEstimation(treeElement) < bestElementEstimation
                    || TreeElementHelper.getParentIndex(treeElement) > maxParentIndex)
            {
                bestElementCount = i;
                break;
            }
        }

        int bestIndex = bestElementCount > 1 ? randomizer.nextInt(bestElementCount) : 0;
        bestTreeElement = bestTreeElements.get(bestIndex);

        Path bestPath = getPathToElement(TreeElementHelper.getParentIndex(bestTreeElement));
        bestPath.addMove(PathElement.valueOf(TreeElementHelper.getMoveDirection(bestTreeElement), (byte) playerId));

        return bestPath;
    }

    /**
     * Gets estimated tree size. NOTE: use only for debug purposes.
     * 
     * @param withObjectHeader
     * @return estimated tree size in bytes.
     */
    public int getEstimatedByteSize(final boolean withObjectHeader)
    {
        return tree.size() * (withObjectHeader ? 8 : 8);
    }

    /**
     * Get end points count.
     * 
     * @param opponentMoveStartIndex
     * @param forAI                  - for AI moves when true, for Opponent moves when false.
     */
    private int getEndPointsCount(final int opponentMoveStartIndex, final boolean forAI)
    {
        int endPointsCount = 0;
        int startIndex = forAI ? 0 : opponentMoveStartIndex;
        int afterEndIndex = forAI ? opponentMoveStartIndex : tree.size();

        for (int i = startIndex; i < afterEndIndex; ++i)
        {
            long treeElement = tree.get(i);
            if (TreeElementHelper.isPathEnd(treeElement))
            {
                ++endPointsCount;
            }
        }
        return endPointsCount;
    }
}
