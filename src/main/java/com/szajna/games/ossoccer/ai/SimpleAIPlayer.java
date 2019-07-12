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

import static com.szajna.games.ossoccer.ai.EstimationTree.EST_GAME_LOST;
import static com.szajna.games.ossoccer.ai.EstimationTree.EST_GOAL_LOST;
import static com.szajna.games.ossoccer.ai.EstimationTree.EST_GOAL_SCORED;

import com.szajna.games.ossoccer.SoccerRules;
import com.szajna.games.ossoccer.ai.EstimationTree.TreeElementHelper;
import com.szajna.games.ossoccer.field.Edge;
import com.szajna.games.ossoccer.field.Field;
import com.szajna.games.ossoccer.field.Move;
import com.szajna.games.ossoccer.field.Node;
import com.szajna.games.ossoccer.field.Path;
import com.szajna.games.ossoccer.field.PlayerId;
import com.szajna.util.Log;

public class SimpleAIPlayer implements AIPlayer
{
    private static final String LOG_TAG = "SimpleAIPlayer ";

    private final int playerId;
    private final Field field;
    private int level;

    private int goalAtBottomY;
    private int goalAtTopY;
    private int fieldHalfHeight;
    @SuppressWarnings("unused")
    private int fieldHalfWidth;

    private static final int ANALYSIS_PATH_MAX_MOVES = 100;
    private byte pathMoves[];
    private byte analysisPathMoves[];
    private int analysisPathMovesSize;

    /**
     * AI configuration class.
     */
    private static class Config
    {
        private final int treeElementMaxCount;
        private final int aiMoveMaxDepth;
        private final int opponentMoveMaxDepth;

        /**
         * Creates AI configuration object.
         * 
         * @param treeElementMaxCount
         * @param aiMoveMaxDepth
         * @param opponentMoveMaxDepth
         */
        Config(final int treeElementMaxCount, final int aiMoveMaxDepth, final int opponentMoveMaxDepth)
        {
            this.treeElementMaxCount = treeElementMaxCount;
            this.aiMoveMaxDepth = aiMoveMaxDepth;
            this.opponentMoveMaxDepth = opponentMoveMaxDepth;
        }
    }

    private final Config cfgEasy = new Config(50000, 4, 8); // RAM max. 400 kiB
    private final Config cfgMedium = new Config(500000, 16, 32); // RAM max. 4 MiB
    private final Config cfgHard = new Config(5000000, 32, 64); // RAM max. 40 MiB

    /** Current AI configuration */
    private Config config;

    /**
     * Creates 'simple' AI Player.
     * 
     * @param level
     * @param pid
     * @param field
     */
    public SimpleAIPlayer(final int level, final int pid, final Field field)
    {
        this.playerId = pid;
        this.field = field;
        this.setDifficultyLevel(level);
    }

    @Override
    public Path makeMove()
    {
        goalAtBottomY = 0;
        goalAtTopY = field.getHeight() - 1;
        fieldHalfHeight = field.getHeight() / 2;
        fieldHalfWidth = field.getWidth() / 2;

        pathMoves = new byte[ANALYSIS_PATH_MAX_MOVES];
        analysisPathMoves = new byte[ANALYSIS_PATH_MAX_MOVES];
        analysisPathMovesSize = 0;

        return doAnalysis();
    }

    @Override
    public int getDifficultyLevel()
    {
        return level;
    }

    @Override
    public void setDifficultyLevel(int level)
    {
        this.level = level;
        switch (level)
        {
        case 0:
            config = cfgEasy;
            break;
        case 1:
            config = cfgMedium;
            break;
        case 2:
        default:
            config = cfgHard;
            break;
        }
    }

    /**
     * Does the analysis.
     * 
     * @return calculated best path.
     */
    private Path doAnalysis()
    {
        if (!SoccerRules.isAnyMoveAllowed(field.getCurrent()) || field.getCurrent().isGoal())
            return null;

        Log.i(LOG_TAG, "Analysis started, level: " + level);

        // Debug.startMethodTracing("calc");
        long starttime = System.currentTimeMillis();
        EstimationTree tree = new EstimationTree(playerId);

        // AI move analysis
        boolean isAIMove = true;
        boolean estimationTopPositive = isEstimationTopPositive(isAIMove);
        boolean aiScoreDetected = false;

        // do the initial analysis
        aiScoreDetected = analyseMovesAtCurrentPosition(tree, -1, 0, estimationTopPositive);
        if (!aiScoreDetected)
        {
            // do the tree analysis
            aiScoreDetected = analyseTreeElements(tree, 0, estimationTopPositive, isAIMove);
        }

        int opponentMoveStartIndex = tree.getElementsCount();
        if (!aiScoreDetected)
        {
            // opponent move analysis
            isAIMove = false;
            estimationTopPositive = isEstimationTopPositive(isAIMove);

            // NOTE: do not move tree.getElementsCount() to the for loop (tree is modified there!)
            int treeElementsCount = tree.getElementsCount();

            for (int i = 0; i < treeElementsCount; ++i)
            {
                // stop if too many moves analyzed
                if (tree.getElementsCount() > config.treeElementMaxCount)
                    break;

                long treeElement = tree.getElement(i);
                if (TreeElementHelper.isOpponentAnalysisPossible(treeElement))
                {
                    prepareFieldForAnalysis(tree, i);
                    analyseMovesAtCurrentPosition(tree, i, TreeElementHelper.getMoveDepth(treeElement),
                            estimationTopPositive);
                }
            }
            // take back all moves from analysis path to leave filed in initial state
            for (int i = analysisPathMovesSize - 1; i >= 0; --i)
            {
                takeBackMove(analysisPathMoves[i]);
            }
            analysisPathMovesSize = 0;

            // do the tree analysis
            analyseTreeElements(tree, opponentMoveStartIndex, estimationTopPositive, isAIMove);
        }

        // get the best path
        Path bestPath = level < 1 ? tree.getBestPathAIAnalysis(opponentMoveStartIndex) : // difficulty: easy
                tree.getBestPath(opponentMoveStartIndex); // difficulty: medium, hard

        Log.i(LOG_TAG, "BEST PATH");
        Log.i(LOG_TAG, bestPath.toString());
        Log.i(LOG_TAG, "Analysis time: " + (System.currentTimeMillis() - starttime) + " ms");

        // Debug.stopMethodTracing();
        return bestPath;
    }

    /**
     * Makes move.
     * 
     * @param moveDirection
     */
    private void makeMove(int moveDirection)
    {
        final Node current = field.getCurrent();
        final Edge edge = current.getEdge(moveDirection);

        if (null == edge)
            throw new IllegalArgumentException("Edge doesn't exists.");
        if (edge.isBlocked())
            throw new IllegalArgumentException("Edge already visited.");

        edge.setVisitedBy(playerId);

        final Node other = edge.getOpposite(current);
        other.setVisited(true);
        field.setCurrent(other.getX(), other.getY());
    }

    /**
     * Takes back move.
     * 
     * @param moveDirection
     */
    private void takeBackMove(int moveDirection)
    {
        int oppositeMoveDir = Move.getOppositeDirection(moveDirection);

        final Node current = field.getCurrent();
        final Edge edge = current.getEdge(oppositeMoveDir);

        if (null == edge)
            throw new IllegalArgumentException("Edge doesn't exists.");
        if (edge.isBorder())
            throw new IllegalArgumentException("Edge is border.");
        if (!edge.isVisited())
            throw new IllegalArgumentException("Edge not visited.");

        edge.setVisitedBy(PlayerId.None);
        boolean currentNodeVisited = false;

        for (int dir = Move.DIRECTION_0; dir <= Move.DIRECTION_7; ++dir)
        {
            final Edge e = current.getEdge(dir);
            if (e != null && !e.isBorder() && e.isVisited())
            {
                currentNodeVisited = true;
                break;
            }
        }
        current.setVisited(currentNodeVisited);

        final Node other = edge.getOpposite(current);
        field.setCurrent(other.getX(), other.getY());
    }

    /**
     * Analyzes all possible moves at current position.
     * 
     * @param tree
     * @param parentIndex
     * @param moveDepth
     * @param estimationTopPositive
     * @return true if score possibility detected (for AI or Opponent), otherwise false.
     */
    private boolean analyseMovesAtCurrentPosition(final EstimationTree tree, final int parentIndex, final int moveDepth,
            final boolean estimationTopPositive)
    {
        boolean scorePosibilityDetected = false;
        Node baseNode = field.getCurrent();

        boolean hasToRebounce;
        boolean analysisComplete;
        boolean pathEnd;
        short estimation;

        for (int dir = Move.DIRECTION_0; dir <= Move.DIRECTION_7; ++dir)
        {
            if (SoccerRules.isMoveAllowed(baseNode, dir))
            {

                Node nextNode = baseNode.getNode(dir);
                hasToRebounce = nextNode.isMarked();

                if (nextNode.isGoal())
                {
                    if (nextNode.getY() == goalAtBottomY)
                    {
                        // goal at bottom
                        estimation = estimationTopPositive ? EST_GOAL_LOST : EST_GOAL_SCORED;
                    }
                    else if (nextNode.getY() == goalAtTopY)
                    {
                        // goal at top
                        estimation = estimationTopPositive ? EST_GOAL_SCORED : EST_GOAL_LOST;
                    }
                    else
                    {
                        estimation = 0;
                    }
                    analysisComplete = true;
                    pathEnd = true;

                    if (estimation == EST_GOAL_SCORED)
                        scorePosibilityDetected = true;

                }
                else if (!SoccerRules.isAnyMoveAllowed(nextNode, Move.getOppositeDirection(dir)))
                {
                    // block - game lost
                    estimation = EST_GAME_LOST;
                    analysisComplete = true;
                    pathEnd = true;
                }
                else
                {
                    estimation = (short) (estimationTopPositive ? (nextNode.getY() - fieldHalfHeight)
                            : (-nextNode.getY() + fieldHalfHeight));

                    analysisComplete = hasToRebounce ? false : true;
                    pathEnd = hasToRebounce ? false : true;
                }

                long treeElement = TreeElementHelper.valueOf(parentIndex, estimation, (byte) (moveDepth + 1),
                        (byte) dir, analysisComplete, pathEnd);
                tree.addElement(treeElement);

                // Log.v(LOG_TAG, "Adding element, parentIndex: " + parentIndex +
                // ", dir: " + dir + ", est: " + estimation);
            }
        }
        return scorePosibilityDetected;
    }

    /**
     * Returns if estimation is top positive or the opposite (bottom positive).
     * 
     * @param isAIMove
     * @return true if estimation is top positive, otherwise false.
     */
    private boolean isEstimationTopPositive(boolean isAIMove)
    {
        if (playerId == PlayerId.Player1)
        {
            return isAIMove ? true : false;

        }
        else if (playerId == PlayerId.Player2)
        {
            return isAIMove ? false : true;

        }
        else
            throw new IllegalArgumentException();
    }

    /**
     * Analyzes tree elements starting from analysisStartIndex.
     * 
     * @param tree
     * @param estimationTopPositive
     * @param analysisStartIndex
     * @param isAIMove
     * @return true if AI score possibility detected (only for AI), otherwise false.
     */
    private boolean analyseTreeElements(final EstimationTree tree, final int analysisStartIndex,
            final boolean estimationTopPositive, final boolean isAIMove)
    {
        final int treeMaxMoveDepth = isAIMove ? config.aiMoveMaxDepth : config.opponentMoveMaxDepth;

        int moveDepth;
        boolean keepAnalyzing = true;
        boolean aiScorePosibilityDetected = false;
        int startIndex = analysisStartIndex;

        while (tree.hasElementsForAnalysis(startIndex) && keepAnalyzing)
        {
            int treeElementsCount = tree.getElementsCount();
            for (int i = startIndex; i < treeElementsCount; ++i)
            {
                // stop if too many moves analyzed
                if (tree.getElementsCount() > config.treeElementMaxCount || keepAnalyzing == false)
                {
                    keepAnalyzing = false;
                    break;
                }

                long treeElement = tree.getElement(i);
                if (!TreeElementHelper.isAnalysisComplete(treeElement))
                {
                    moveDepth = TreeElementHelper.getMoveDepth(treeElement);
                    if (moveDepth < treeMaxMoveDepth || !tree.isAtLeastOnePathEndInTree())
                    {
                        // do the further analysis
                        prepareFieldForAnalysis(tree, i);

                        if (analyseMovesAtCurrentPosition(tree, i, moveDepth, estimationTopPositive) && isAIMove)
                        {
                            // stop analysis early if AI score possibility detected
                            keepAnalyzing = false;
                            aiScorePosibilityDetected = true;
                        }
                    }

                    // set analysis complete for treeElement
                    long element = TreeElementHelper.setAnalysisComplete(treeElement, true);
                    tree.setElement(element, i);
                }
            }
            startIndex = treeElementsCount;
        }

        // take back all moves from analysis path to leave filed in initial state
        for (int i = analysisPathMovesSize - 1; i >= 0; --i)
        {
            takeBackMove(analysisPathMoves[i]);
        }
        analysisPathMovesSize = 0;

        Log.d(LOG_TAG, "Tree size: " + tree.getElementsCount() + ", " + tree.getEstimatedByteSize(true) + " bytes");

        return aiScorePosibilityDetected;
    }

    /**
     * Prepares field for tree analysis at index.
     * 
     * @param tree
     * @param index
     */
    private void prepareFieldForAnalysis(final EstimationTree tree, final int index)
    {
        int pathMovesCount = tree.getPathToElement(index, pathMoves);

        int moveIndex = 0;
        while (moveIndex < analysisPathMovesSize && moveIndex < pathMovesCount
                && analysisPathMoves[moveIndex] == pathMoves[moveIndex])
        {
            ++moveIndex;
        }
        int diffStartIndex = moveIndex;

        // take back moves
        for (int i = analysisPathMovesSize - 1; i >= diffStartIndex; --i)
        {
            takeBackMove(analysisPathMoves[i]);
            analysisPathMovesSize--;
        }
        // make moves
        byte moveDirection;
        for (int i = diffStartIndex; i < pathMovesCount; ++i)
        {
            moveDirection = pathMoves[i];
            makeMove(moveDirection);
            analysisPathMoves[analysisPathMovesSize] = moveDirection;
            analysisPathMovesSize++;
        }

        if (analysisPathMovesSize > ANALYSIS_PATH_MAX_MOVES)
        {
            throw new ArrayIndexOutOfBoundsException(
                    "" + analysisPathMovesSize + " exceeds ANALYSIS_PATH_MAX_MOVES [" + ANALYSIS_PATH_MAX_MOVES + "]");
        }
    }
}
