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

import java.util.Properties;

import com.szajna.games.ossoccer.ai.AIFactory;
import com.szajna.games.ossoccer.ai.AIPlayer;
import com.szajna.games.ossoccer.field.Field;
import com.szajna.games.ossoccer.field.FieldFactory;
import com.szajna.games.ossoccer.field.Path;
import com.szajna.games.ossoccer.field.Path.PathElement;
import com.szajna.games.ossoccer.field.PlayerId;

public class SoccerEngine implements Runnable
{
    private IAnalysisObserver analysisObserver;
    private AIPlayer aiPlayer;
    private Field field;
    private Path bestPath;

    /*
     * ! SoccerEngine constructor.
     */
    public SoccerEngine()
    {
        Properties appProperties = AppConfig.getInstance().getAppProperties();
        String propDifficultyLevel = appProperties.getProperty(AppConfig.PROP_KEY_DIFFICULTY_LEVEL);

        int difficultyLevel;
        try
        {
            difficultyLevel = Integer.parseInt(propDifficultyLevel);
        }
        catch (NumberFormatException e)
        {
            // default - medium
            difficultyLevel = 1;
        }
        if (difficultyLevel < 0 || difficultyLevel > 2)
        {
            difficultyLevel = 1;
        }

        field = FieldFactory.createStandard();
        aiPlayer = AIFactory.createPlayer("simple", difficultyLevel, PlayerId.Player2, field);
        bestPath = null;
    }

    public void setAnalysisObserver(IAnalysisObserver observer)
    {
        this.analysisObserver = observer;
    }

    public Field getField()
    {
        return field;
    }

    public int getDifficultyLevel()
    {
        return aiPlayer.getDifficultyLevel();
    }

    public void setDifficultyLevel(int level)
    {
        aiPlayer.setDifficultyLevel(level);
    }

    public void resetGame()
    {
        // reset field
        field.reset();
    }

    public void setNextRound()
    {
        field.getPath().addMove(PathElement.PATH_SEPARATOR);
    }

    @Override
    public void run()
    {
        assert (analysisObserver != null);
        bestPath = null;
        bestPath = aiPlayer.makeMove();
        analysisObserver.onAnalysisComplete();
    }

    public void startAnalysis()
    {
        Thread worker = new Thread(this);
        worker.start();
    }

    public Path getBestPath()
    {
        return bestPath;
    }
}
