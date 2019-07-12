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

package com.szajna.games.ossoccer.controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.szajna.games.ossoccer.AppConfig;
import com.szajna.games.ossoccer.IAnalysisObserver;
import com.szajna.games.ossoccer.Player;
import com.szajna.games.ossoccer.SoccerEngine;
import com.szajna.games.ossoccer.SoccerRules;
import com.szajna.games.ossoccer.field.Field;
import com.szajna.games.ossoccer.field.Path;
import com.szajna.games.ossoccer.field.Path.PathElement;
import com.szajna.games.ossoccer.field.PlayerId;
import com.szajna.games.ossoccer.view.Court;
import com.szajna.util.Log;

public class SoccerController implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener,
        KeyListener, IAnalysisObserver, ActionListener
{
    private static final String LOG_TAG = SoccerController.class.getSimpleName();

    private SoccerEngine soccerEngine;
    private final Court courtView;

    private Player[] players;
    private Player currentPlayer;

    private boolean moveAcceptMode;
    private Path bestPath;
    private javax.swing.Timer uiTimer;

    private final static int FPS = 30;
    private long uiRepaintTimestamp = -1;

    /** 0 - slow, 1 - medium, 2 - fast */
    private int uiAnimSpeed;

    /** Configurable game rules */
    private boolean firstGoalWins;

    private int pathDrawTimePerMove;
    private int pathTotalDrawTime = 0;
    private int pathDrawTime = 0;
    private volatile boolean pathDrawInProgress = false;

    private Player winner = null;
    private boolean gameOver = false;
    private boolean gameOverEvent = false;

    public SoccerController(SoccerEngine soccerEngine, final Court courtView)
    {
        this.soccerEngine = soccerEngine;
        this.courtView = courtView;
        createPlayers();

        moveAcceptMode = true;
        bestPath = new Path();

        // set UI animation speed
        Properties appProperties = AppConfig.getInstance().getAppProperties();
        String propUiAnimSpeed = appProperties.getProperty(AppConfig.PROP_KEY_UI_ANIM_SPEED);
        String propFirstGoalWins = appProperties.getProperty(AppConfig.PROP_KEY_FIRST_GOAL_WINS);
        try
        {
            uiAnimSpeed = Integer.parseInt(propUiAnimSpeed);
            firstGoalWins = Integer.parseInt(propFirstGoalWins) == 0 ? false : true;
        }
        catch (NumberFormatException e)
        {
            // set defaults
            uiAnimSpeed = AppConfig.DEFAULT_UI_ANIM_SPEED;
            firstGoalWins = (AppConfig.DEFAULT_FIRST_GOAL_WINS == 0) ? false : true;
        }

        if (uiAnimSpeed < 0 || uiAnimSpeed > 2)
        {
            uiAnimSpeed = AppConfig.DEFAULT_UI_ANIM_SPEED;
        }

        setUiAnimSpeed(uiAnimSpeed);

        uiTimer = new javax.swing.Timer(1000 / FPS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                long now = System.currentTimeMillis();
                long millis = now - uiRepaintTimestamp;

                pathDrawTime += (int) millis;
                if (pathDrawTime > pathTotalDrawTime)
                {
                    pathDrawTime = pathTotalDrawTime;
                    pathDrawInProgress = false;
                }

                int pathDrawPercent;
                if (pathTotalDrawTime > 0)
                {
                    pathDrawPercent = 100 * pathDrawTime / pathTotalDrawTime;
                }
                else
                {
                    pathDrawPercent = 100;
                }

                courtView.setLastMoveDrawPercent(pathDrawPercent);
                courtView.repaint();
                uiRepaintTimestamp = now;

                if (gameOverEvent && pathDrawInProgress == false)
                {
                    String text = winner != null ? ("The winner is: " + winner.getName() + "!") : "Game draw!";
                    JOptionPane.showMessageDialog(null, text, "Game over", JOptionPane.INFORMATION_MESSAGE);
                    gameOverEvent = false;
                }
            }
        });
        uiTimer.start();
    }

    public Player[] getPlayers()
    {
        return players;
    }

    public int getUiAnimSpeed()
    {
        return uiAnimSpeed;
    }

    private void setUiAnimSpeed(int speed)
    {
        if (speed < 0)
            speed = 0;
        else if (speed > 2)
            speed = 2;

        uiAnimSpeed = speed;
        switch (speed)
        {
        case 0:
            pathDrawTimePerMove = 500;
            break;
        case 1:
            pathDrawTimePerMove = 250;
            break;
        case 2:
        default:
            pathDrawTimePerMove = 100;
            break;
        }
    }

    public boolean isFirstGoalWins()
    {
        return firstGoalWins;
    }

    public void setFirstGoalWins(boolean firstGoalWins)
    {
        this.firstGoalWins = firstGoalWins;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        boolean lmb = SwingUtilities.isLeftMouseButton(e);
        if (!lmb || gameOver)
        {
            return;
        }

        if (currentPlayer.isCpuControlled() || pathDrawInProgress)
        {
            return;
        }

        Point p = e.getPoint();
        courtView.setFingerPos(p.x, p.y);
        courtView.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (pathDrawInProgress)
        {
            return;
        }

        boolean lmb = SwingUtilities.isLeftMouseButton(e);
        boolean rmb = SwingUtilities.isRightMouseButton(e);
        boolean gameOverProcessed = (gameOver && (!gameOverEvent));

        if (gameOverProcessed && (lmb || rmb))
        {
            setNewGame();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        boolean lmb = SwingUtilities.isLeftMouseButton(e);
        if (!lmb || gameOver)
        {
            return;
        }

        if (currentPlayer.isCpuControlled() || pathDrawInProgress)
        {
            return;
        }

        Point p = e.getPoint();
        courtView.setFingerPos(p.x, p.y);

        if (moveAcceptMode)
        {
            if (courtView.isMoveAccepted(p.x, p.y))
            {
                int moveDir = courtView.getMoveDirection();
                int playerId = currentPlayer.getId();

                bestPath.clear();
                bestPath.addMove(PathElement.valueOf((byte) moveDir, (byte) playerId));

                try
                {
                    onMoveMade();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }

                courtView.setFingerPos(-1, -1);
                courtView.repaint();
            }
        }
        else
        {
            courtView.setFingerPos(p.x, p.y);
            courtView.repaint();
            moveAcceptMode = true;
        }
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
    }

    @Override
    public void onAnalysisComplete()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run()
                {
                    bestPath = soccerEngine.getBestPath();
                    assert (bestPath != null && bestPath.getMoves().size() > 0);

                    pathDrawTime = 0;
                    pathTotalDrawTime = bestPath.getMoves().size() * pathDrawTimePerMove;
                    pathDrawInProgress = true;

                    courtView.setLastMoveSize(bestPath.getMoves().size());
                    courtView.setLastMoveDrawPercent(0);

                    try
                    {
                        onMoveMade();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    courtView.repaint();
                }
            });
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void createPlayers()
    {
        Player player1 = new Player(PlayerId.Player1, "Player", false);
        Player player2 = new Player(PlayerId.Player2, "CPU", true);
        Player[] players = { player1, player2 };

        this.players = players;
        setCurrentPlayer(players[0]);
    }

    private void setCurrentPlayer(Player player)
    {
        this.currentPlayer = player;
        courtView.setCurrentPlayer(player);
    }

    private void setNewGame()
    {
        bestPath.clear();
        soccerEngine.resetGame();

        setCurrentPlayer(players[0]);

        for (Player player : players)
        {
            player.setScore(0);
        }

        courtView.setFingerPos(-1, -1);

        pathDrawTime = pathTotalDrawTime;
        pathDrawInProgress = false;

        winner = null;
        gameOver = false;
        gameOverEvent = false;

        courtView.repaint();
    }

    private void onMoveMade()
    {
        Field field = soccerEngine.getField();
        boolean hasToRebounce = false;

        if (bestPath.getMoves().size() == 1)
        {
            int dir = bestPath.getMoves().get(0).getMoveDirection();
            hasToRebounce = SoccerRules.hasToRebounce(field.getCurrent(), dir);
        }

        field.applyPath(bestPath);
        bestPath.clear();

        if (SoccerRules.isGoalScored(field))
        {
            // goal scored
            Player scorer = getScorer(field);
            if (scorer != null)
            {
                scorer.setScore(scorer.getScore() + 1);
            }

            // set current node at center
            field.setCurrent(field.getWidth() / 2, field.getHeight() / 2);

            soccerEngine.setNextRound();
            Player opponent = getOpponent(scorer);
            setCurrentPlayer(opponent);

            if (firstGoalWins == false && SoccerRules.isAnyMoveAllowed(field.getCurrent()))
            {
                if (currentPlayer.isCpuControlled())
                {
                    soccerEngine.startAnalysis();
                }
            }
            else
            {
                // game over - get the Player who shot more goals
                winner = getWinner();
                gameOver = true;
                gameOverEvent = true;
            }
        }
        else if (!SoccerRules.isAnyMoveAllowed(field.getCurrent()))
        {
            // blocked player loses the game
            winner = getOpponent(currentPlayer);
            gameOver = true;
            gameOverEvent = true;
        }
        else
        {
            if (!hasToRebounce)
            {
                Player opponent = getOpponent(currentPlayer);
                setCurrentPlayer(opponent);

                if (currentPlayer.isCpuControlled())
                {
                    soccerEngine.startAnalysis();
                }
            }
        }
    }

    /**
     * Get opponent to Player player.
     * 
     * @param player
     * @return opponent to player.
     */
    private Player getOpponent(Player player)
    {
        Player otherPlayer = null;
        for (Player p : players)
        {
            if (p != player)
            {
                otherPlayer = p;
                break;
            }
        }
        assert (otherPlayer != null);
        return otherPlayer;
    }

    /**
     * Gets Player goal scorer. NOTE: currentPlayer is always goal scorer (@see getCurrentPlayer()), but
     * it can be an own goal!
     * 
     * @return Player for which goal has been scored or null if there is no goal scored.
     */
    public Player getScorer(Field field)
    {
        if (field.getCurrent().getY() == 0)
            return players[1];
        else if (field.getCurrent().getY() == field.getHeight() - 1)
            return players[0];
        else
            return null;
    }

    /**
     * Return winner Player or null if draw.
     */
    private Player getWinner()
    {
        assert (players.length == 2);
        Player playerWinner;

        if (players[0].getScore() > players[1].getScore())
        {
            playerWinner = players[0];
        }
        else if (players[0].getScore() < players[1].getScore())
        {
            playerWinner = players[1];
        }
        else
        {
            playerWinner = null;
        }

        return playerWinner;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_P)
        {
            // enable for debugging purposes
            // soccerEngine.getField().printFiled();
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * ActionListener interface implementation.
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        // Log.d(LOG_TAG, "actionPerformed: " + event);

        if (event.getActionCommand() == MenuActionCommand.GAME_NEW)
        {
            Log.d(LOG_TAG, "Game->New clicked");
            setNewGame();
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_EASY)
        {
            Log.d(LOG_TAG, "Game->Difficulty_Easy clicked");
            final int level = 0;
            soccerEngine.setDifficultyLevel(level);
            courtView.setDifficultyLevel(level);
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_MEDIUM)
        {
            Log.d(LOG_TAG, "Game->Difficulty_Medium clicked");
            final int level = 1;
            soccerEngine.setDifficultyLevel(level);
            courtView.setDifficultyLevel(level);
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_HARD)
        {
            Log.d(LOG_TAG, "Game->Difficulty_Hard clicked");
            final int level = 2;
            soccerEngine.setDifficultyLevel(level);
            courtView.setDifficultyLevel(level);
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_SLOW)
        {
            Log.d(LOG_TAG, "Ui->Anim_Slow clicked");
            setUiAnimSpeed(0);
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_MEDIUM)
        {
            Log.d(LOG_TAG, "Ui->Anim_Medium clicked");
            setUiAnimSpeed(1);
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_FAST)
        {
            Log.d(LOG_TAG, "Ui->Anim_Fast clicked");
            setUiAnimSpeed(2);
        }
    }
}
