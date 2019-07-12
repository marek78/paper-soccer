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

package com.szajna.games.ossoccer.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.szajna.games.ossoccer.Player;
import com.szajna.games.ossoccer.SoccerRules;
import com.szajna.games.ossoccer.field.Field;
import com.szajna.games.ossoccer.field.Path;
import com.szajna.games.ossoccer.field.Path.PathElement;
import com.szajna.games.ossoccer.field.PlayerId;
import com.szajna.games.ossoccer.toobox.Tools;

public class Court extends JPanel
{
    private static final long serialVersionUID = 1L;

    private Field field;
    private int level;
    private Player[] players = null;
    private Player currentPlayer = null;

    private int marginX;
    private int marginY;

    private int cellSize;
    private int cellCountX;
    private int cellCountY;

    private final int goalWidth = 2;
    private final int goalHeight = 1;

    private int fingerX;
    private int fingerY;

    private int moveDirection;

    private int lastMoveDrawPercent;
    private int lastMoveSize;

    private int ballPixelPosX;
    private int ballPixelPosY;

    private BufferedImage imageBall = null;
    private AffineTransform transform = new AffineTransform();

    private float ballRotationsPerSecond = 1.0f;
    private float ballAngle = 0;
    private long ballAngleTimestamp = 0;

    public Court(Field field, int level)
    {
        setPreferredSize(new Dimension(GuiCustomization.PREFERRED_WIDTH, GuiCustomization.PREFERRED_HEIGHT));
        this.field = field;
        this.level = level;

        cellCountX = field.getWidth() - 1;
        cellCountY = field.getHeight() - 1;

        fingerX = -1;
        fingerY = -1;
        moveDirection = -1;

        lastMoveSize = 0;
        lastMoveDrawPercent = 0;

        ballPixelPosX = -1;
        ballPixelPosY = -1;

        URL ballUrl = MainWindow.class.getResource("/ball.png");
        if (ballUrl != null)
        {
            try
            {
                imageBall = ImageIO.read(ballUrl);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setDifficultyLevel(int level)
    {
        this.level = level;
    }

    public void setPlayers(Player[] players)
    {
        assert (players.length == 2);
        this.players = players;
    }

    public void setCurrentPlayer(Player player)
    {
        this.currentPlayer = player;
    }

    public void setLastMoveSize(int size)
    {
        lastMoveSize = size;
    }

    public void setLastMoveDrawPercent(int percent)
    {
        lastMoveDrawPercent = percent;
    }

    private int getBallX()
    {
        return field.getCurrent().getX();
    }

    private int getBallY()
    {
        return cellCountY - field.getCurrent().getY();
    }

    public void setFingerPos(int x, int y)
    {
        fingerX = x;
        fingerY = y;

        int column = getBallX();
        int row = getBallY();

        int x1 = marginX + column * cellSize;
        int y1 = marginY + row * cellSize;

        int diffX = fingerX - x1;
        int diffY = fingerY - y1;
        int alpha = 0;

        if (diffX == 0)
        {
            alpha = diffY < 0 ? 0 : 180;
        }
        else
        {
            alpha = (int) (Math.atan(diffY / (double) diffX) * 180 / Math.PI);
            alpha += (diffX > 0 ? 90 : 270);
        }
        if (alpha == 360)
            alpha = 0;

        moveDirection = (int) (alpha / 45.0 + 0.5);
        if (moveDirection == 8)
            moveDirection = 0;
    }

    public int getMoveDirection()
    {
        return moveDirection;
    }

    public boolean isMoveAccepted(int fingerX, int fingerY)
    {
        if (!SoccerRules.isMoveAllowed(field.getCurrent(), moveDirection))
            return false;

        boolean accepted = false;

        int newBallColumn = getBallX() + Tools.getMoveShiftByDirection(moveDirection).x;
        int newBallRow = getBallY() + Tools.getMoveShiftByDirection(moveDirection).y;

        int x1 = marginX + newBallColumn * cellSize;
        int y1 = marginY + newBallRow * cellSize;

        Rectangle rect = new Rectangle(x1 - cellSize, y1 - cellSize, 2 * cellSize, 2 * cellSize);
        if (rect.contains(fingerX, fingerY))
            accepted = true;

        return accepted;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, getWidth(), getHeight());

        marginX = GuiCustomization.X_MARGIN_PERCENT * getWidth() / 100;
        marginY = GuiCustomization.Y_MARGIN_PERCENT * getHeight() / 100;

        int cellWidth = (getWidth() - 2 * marginX) / cellCountX;
        int cellHeight = (getHeight() - 2 * marginY) / cellCountY;

        cellSize = Math.min(cellWidth, cellHeight);

        // do the horizontal centering
        marginX = (getWidth() - cellSize * cellCountX) / 2;

        int ballColumn = getBallX();
        int ballRow = getBallY();

        drawCourt(g2d);
        drawPath(g2d);
        drawMove(g2d, ballColumn, ballRow);
        // drawing court center above path looks better
        drawCenter(g2d);

        // draw ball
        int ballSize = (int) (cellSize * 0.4f);
        boolean rotate = lastMoveDrawPercent < 100 ? true : false;
        drawBall(g2d, ballSize, ballPixelPosX - ballSize / 2, ballPixelPosY - ballSize / 2, rotate);
        drawPlayers(g2d);
    }

    /**
     * Draws player names.
     * 
     * @param g2d
     */
    private void drawPlayers(Graphics2D g2d)
    {
        Font scaledFont = new Font("Default", Font.BOLD, cellSize / 2);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(scaledFont);

        String diffLevelString;
        if (level == 0)
            diffLevelString = "easy";
        else if (level == 1)
            diffLevelString = "medium";
        else if (level == 2)
            diffLevelString = "hard";
        else
            diffLevelString = "";

        String textPlayer1 = players[0].getName();
        String textPlayer2 = players[1].getName();

        if (players[0].isCpuControlled())
            textPlayer1 += " (" + diffLevelString + ")";
        if (players[1].isCpuControlled())
            textPlayer2 += " (" + diffLevelString + ")";

        int fontBaseY = cellSize;

        g2d.setColor(GuiCustomization.PLAYER1_PATH_COLOR);
        g2d.drawString(textPlayer1 + ": " + players[0].getScore(), marginX + cellSize / 2, fontBaseY);

        g2d.setColor(GuiCustomization.PLAYER2_PATH_COLOR);
        g2d.drawString(textPlayer2 + ": " + players[1].getScore(), marginX + cellSize / 2, fontBaseY * 2);

        int ballSize = (int) (cellSize * 0.25f);

        if (currentPlayer == players[0])
        {
            drawBall(g2d, ballSize, marginX, fontBaseY - ballSize, false);
        }
        else if (currentPlayer == players[1])
        {
            drawBall(g2d, ballSize, marginX, fontBaseY * 2 - ballSize, false);
        }
    }

    /**
     * Draws path.
     * 
     * @param g2d
     */
    private void drawPath(Graphics2D g2d)
    {
        int pathThickness = (int) (cellSize * 0.20f);

        g2d.setStroke(new BasicStroke(pathThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path path = field.getPath();
        int column = cellCountX / 2;
        int row = cellCountY / 2;

        // initial ball position
        if (lastMoveDrawPercent >= 100 && path.getMoves().size() == 0)
        {
            ballPixelPosX = marginX + column * cellSize;
            ballPixelPosY = marginY + row * cellSize;
        }

        int movesToSkip = 0;
        int lastMovePercent = 100;

        if (lastMoveDrawPercent < 100)
        {
            movesToSkip = lastMoveSize - lastMoveSize * lastMoveDrawPercent / 100;
            lastMovePercent = lastMoveSize * lastMoveDrawPercent % 100;
        }

        ArrayList<PathElement> moves = path.getMoves();
        for (int i = 0; i < moves.size(); i++)
        {
            PathElement pathElement = moves.get(i);
            if (pathElement.isSeparator())
            {
                // set path start at center if PATH_SEPARATOR
                column = cellCountX / 2;
                row = cellCountY / 2;

                if (lastMoveDrawPercent >= 100 && i == moves.size() - 1)
                {
                    // ball position at center
                    ballPixelPosX = marginX + column * cellSize;
                    ballPixelPosY = marginY + row * cellSize;
                }
                continue;
            }

            if (i > moves.size() - movesToSkip)
            {
                continue;
            }

            byte moveDir = pathElement.getMoveDirection();
            g2d.setColor(pathElement.getPlayerId() == PlayerId.Player1 ? GuiCustomization.PLAYER1_PATH_COLOR
                    : GuiCustomization.PLAYER2_PATH_COLOR);

            int moveShiftX = Tools.getMoveShiftByDirection(moveDir).x * cellSize;
            int moveShiftY = Tools.getMoveShiftByDirection(moveDir).y * cellSize;

            if (i == moves.size() - movesToSkip)
            {
                moveShiftX = (moveShiftX * lastMovePercent) / 100;
                moveShiftY = (moveShiftY * lastMovePercent) / 100;
            }

            int x1 = marginX + column * cellSize;
            int y1 = marginY + row * cellSize;

            g2d.drawLine(x1, y1, x1 + moveShiftX, y1 + moveShiftY);
            ballPixelPosX = x1 + moveShiftX;
            ballPixelPosY = y1 + moveShiftY;

            column += Tools.getMoveShiftByDirection(moveDir).x;
            row += Tools.getMoveShiftByDirection(moveDir).y;
        }
    }

    /**
     * Draws court.
     * 
     * @param g2d
     */
    private void drawCourt(Graphics2D g2d)
    {
        g2d.setColor(GuiCustomization.COURT_GRID);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int x1;
        int y1;

        for (int i = 0; i <= cellCountX; ++i)
        {
            x1 = marginX + i * cellSize;
            y1 = marginY + cellSize;
            g2d.drawLine(x1, y1, x1, y1 + (cellCountY - 2 * goalHeight) * cellSize);
        }

        for (int i = 0; i <= cellCountY - 2 * goalHeight; ++i)
        {
            x1 = marginX;
            y1 = marginY + (i + 1) * cellSize;
            g2d.drawLine(x1, y1, x1 + cellCountX * cellSize, y1);
        }
        drawGoal(g2d, true);
        drawGoal(g2d, false);
        drawBoundary(g2d);
    }

    /**
     * Draws goal.
     * 
     * @param g2d
     * @param upper
     */
    private void drawGoal(Graphics2D g2d, boolean upper)
    {
        g2d.setColor(GuiCustomization.COURT_BOUNDARY_COLOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int gateOffset = (cellCountX - goalWidth) / 2;
        int x1 = marginX + gateOffset * cellSize;
        int y1;

        if (upper)
        {
            y1 = marginY;
            g2d.drawLine(x1, y1, x1 + goalWidth * cellSize, y1);
        }
        else
        {
            y1 = marginY + (cellCountY - goalHeight) * cellSize;
            g2d.drawLine(x1, y1 + cellSize, x1 + goalWidth * cellSize, y1 + cellSize);
        }
        g2d.drawLine(x1, y1, x1, y1 + cellSize);
        g2d.drawLine(x1 + goalWidth * cellSize, y1, x1 + goalWidth * cellSize, y1 + cellSize);
    }

    /**
     * Draws court boundary.
     * 
     * @param g2d
     */
    private void drawBoundary(Graphics2D g2d)
    {
        g2d.setColor(GuiCustomization.COURT_BOUNDARY_COLOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int gateOffset = (cellCountX - goalWidth) / 2;
        int x1 = marginX;
        int y1;

        for (int i = 0; i < 2; ++i)
        {
            y1 = (i == 0 ? marginY + cellSize : marginY + (cellCountY - goalHeight) * cellSize);
            g2d.drawLine(x1, y1, x1 + gateOffset * cellSize, y1);
            g2d.drawLine(x1 + (gateOffset + goalWidth) * cellSize, y1, x1 + cellCountX * cellSize, y1);
        }

        x1 = marginX;
        y1 = marginY + cellSize;
        g2d.drawLine(x1, y1, x1, y1 + (cellCountY - 2 * goalHeight) * cellSize);

        x1 = marginX + cellCountX * cellSize;
        y1 = marginY + cellSize;
        g2d.drawLine(x1, y1, x1, y1 + (cellCountY - 2 * goalHeight) * cellSize);
    }

    /**
     * Draws court center.
     * 
     * @param g2d
     */
    private void drawCenter(Graphics2D g2d)
    {
        g2d.setColor(GuiCustomization.COURT_CENTER);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int column = cellCountX / 2;
        int row = cellCountY / 2;

        int ballSize = (int) (cellSize * 0.4f);
        int x = marginX + column * cellSize - ballSize / 2;
        int y = marginY + row * cellSize - ballSize / 2;

        g2d.fillArc(x, y, ballSize, ballSize, 0, 360);
    }

    /**
     * Draws ball at position x, y in pixels.
     * 
     * @param g2d
     * @param x
     * @param y
     */
    private void drawBall(Graphics2D g2d, int ballSize, int x, int y, boolean rotate)
    {
        g2d.setColor(GuiCustomization.BALL_COLOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (imageBall != null)
        {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            Image scalledBall = imageBall.getScaledInstance(ballSize, ballSize, Image.SCALE_SMOOTH);

            if (rotate)
            {
                if (ballAngleTimestamp > 0)
                {
                    long timeDiffMillis = System.currentTimeMillis() - ballAngleTimestamp;
                    ballAngle += 360 * ballRotationsPerSecond * timeDiffMillis / 1000;
                    ballAngle = Tools.normalizeAngle(ballAngle);
                }

                transform.setToIdentity();
                transform.translate(x + ballSize / 2, y + ballSize / 2);
                transform.rotate(Math.toRadians((double) ballAngle));
                transform.translate(-ballSize / 2, -ballSize / 2);

                g2d.drawImage(scalledBall, transform, null);
                ballAngleTimestamp = System.currentTimeMillis();

            }
            else
            {
                g2d.drawImage(scalledBall, x, y, null);
            }
        }
        else
        {
            g2d.fillArc(x, y, ballSize, ballSize, 0, 360);
        }
    }

    /**
     * Draws move.
     * 
     * @param g2d
     * @param column
     * @param row
     */
    private void drawMove(Graphics2D g2d, int column, int row)
    {
        if (fingerX >= 0 && fingerY >= 0)
        {
            Color color = currentPlayer.getId() == PlayerId.Player1 ? GuiCustomization.PLAYER1_MOVE_COLOR
                    : GuiCustomization.PLAYER2_MOVE_COLOR;

            int pathThickness = (int) (cellSize * 0.20f);

            g2d.setColor(new Color(80, 80, 80, 80));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(pathThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

            int x1 = marginX + column * cellSize;
            int y1 = marginY + row * cellSize;

            if (SoccerRules.isMoveAllowed(field.getCurrent(), moveDirection))
            {
                int moveShiftX = Tools.getMoveShiftByDirection(moveDirection).x * cellSize;
                int moveShiftY = Tools.getMoveShiftByDirection(moveDirection).y * cellSize;

                g2d.setColor(color);
                g2d.drawLine(x1, y1, x1 + moveShiftX, y1 + moveShiftY);

                int circleSize = (int) (cellSize * 0.4f);
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke());
                g2d.drawArc(x1 + moveShiftX - circleSize / 2, y1 + moveShiftY - circleSize / 2, circleSize, circleSize,
                        0, 360);
            }
        }
    }
}
