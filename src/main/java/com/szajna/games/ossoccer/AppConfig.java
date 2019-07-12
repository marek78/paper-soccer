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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig
{
    public static final String PROP_KEY_DIFFICULTY_LEVEL = "difficultyLevel";
    public static final String PROP_KEY_FIRST_GOAL_WINS = "firstGoalWins";
    public static final String PROP_KEY_UI_ANIM_SPEED = "uiAnimSpeed";

    public static final int DEFAULT_FIRST_GOAL_WINS = 0;
    public static final int DEFAULT_UI_ANIM_SPEED = 1;

    private static final AppConfig instance = new AppConfig();
    private String configPath;

    private Properties defaultProperties = new Properties();
    private Properties appProperties;

    private AppConfig()
    {
        defaultProperties.setProperty(PROP_KEY_DIFFICULTY_LEVEL, "1");
        defaultProperties.setProperty(PROP_KEY_FIRST_GOAL_WINS, String.valueOf(DEFAULT_FIRST_GOAL_WINS));
        defaultProperties.setProperty(PROP_KEY_UI_ANIM_SPEED, String.valueOf(DEFAULT_UI_ANIM_SPEED));

        String fs = System.getProperty("file.separator");
        configPath = System.getProperty("user.home") + fs + ".ossoccer" + fs + "config.txt";

        // if file already exists will do nothing
        File configFile = new File(configPath);
        try
        {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static AppConfig getInstance()
    {
        return instance;
    }

    public Properties getAppProperties()
    {
        return appProperties;
    }

    public void readFromFile()
    {
        appProperties = new Properties(defaultProperties);
        FileInputStream in;

        try
        {
            in = new FileInputStream(configPath);
            appProperties.load(in);

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void writeToFile()
    {
        try
        {
            FileOutputStream out = new FileOutputStream(configPath);
            appProperties.store(out, "---App configuration---");
            out.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
