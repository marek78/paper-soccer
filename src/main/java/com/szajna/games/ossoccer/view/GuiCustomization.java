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

import java.awt.Color;

public class GuiCustomization
{
    public static final int X_MARGIN_PERCENT = 10;
    public static final int Y_MARGIN_PERCENT = 15;
    public static final int PREFERRED_WIDTH = 480;
    public static final int PREFERRED_HEIGHT = 800;

    public static final Color COURT_BOUNDARY_COLOR = new Color(20, 200, 20);
    public static final Color COURT_GRID = new Color(60, 60, 60);
    public static final Color COURT_CENTER = new Color(160, 160, 160, 180);

    public static final Color TEXT_COLOR = new Color(200, 200, 200);
    public static final Color BALL_COLOR = new Color(240, 20, 20);

    public static final Color PLAYER1_MOVE_COLOR = new Color(100, 40, 40, 180);
    public static final Color PLAYER2_MOVE_COLOR = new Color(40, 100, 40, 180);

    public static final Color PLAYER1_PATH_COLOR = new Color(220, 20, 20, 180);
    public static final Color PLAYER2_PATH_COLOR = new Color(20, 200, 20, 180);

    /**
     * No instance allowed.
     */
    private GuiCustomization()
    {
    }
}
