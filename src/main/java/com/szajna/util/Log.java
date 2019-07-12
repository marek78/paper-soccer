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

package com.szajna.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log
{
    public static final int LOG_LEVEL_ERROR = 1;
    public static final int LOG_LEVEL_WARN = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_DEBUG = 4;
    public static final int LOG_LEVEL_VERBOSE = 5;

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static int logLevel = LOG_LEVEL_DEBUG;

    public static void setLogLevel(int level)
    {
        logLevel = level;
    }

    public static void v(String tag, String msg)
    {
        if (logLevel >= LOG_LEVEL_VERBOSE)
        {
            printLog("V: ", tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if (logLevel >= LOG_LEVEL_DEBUG)
        {
            printLog("D: ", tag, msg);
        }
    }

    public static void i(String tag, String msg)
    {
        if (logLevel >= LOG_LEVEL_INFO)
        {
            printLog("I: ", tag, msg);
        }
    }

    public static void w(String tag, String msg)
    {
        if (logLevel >= LOG_LEVEL_WARN)
        {
            printLog("W: ", tag, msg);
        }
    }

    public static void e(String tag, String msg)
    {
        if (logLevel >= LOG_LEVEL_ERROR)
        {
            printLog("E: ", tag, msg);
        }
    }

    private static void printLog(String level, String tag, String msg)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date()));
        sb.append(" ");
        sb.append(Thread.currentThread().getName());
        sb.append(" [");
        sb.append(tag);
        sb.append("] ");
        sb.append(level);
        sb.append(msg);
        System.out.println(sb.toString());
    }
}
