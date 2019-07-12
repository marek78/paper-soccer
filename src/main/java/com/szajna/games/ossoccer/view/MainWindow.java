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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.szajna.games.ossoccer.AppConfig;
import com.szajna.games.ossoccer.SoccerEngine;
import com.szajna.games.ossoccer.controller.MenuActionCommand;
import com.szajna.games.ossoccer.controller.SoccerController;

public class MainWindow extends JFrame implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private SoccerController soccerController = null;

    private JMenuItem menuItemGameDiffEasy;
    private JMenuItem menuItemGameDiffMedium;
    private JMenuItem menuItemGameDiffHard;

    private JMenuItem menuItemGameRulesFirstGoalWins;

    private JMenuItem menuItemUiAnimFast;
    private JMenuItem menuItemUiAnimMedium;
    private JMenuItem menuItemUiAnimSlow;
    private Object mutex = new Object();

    private enum MenuItemType
    {
        TYPE_SIMPLE, TYPE_CHECKBOX,
    };

    public MainWindow(String appName)
    {
        createUi(appName);
    }

    private void createUi(String appName)
    {
        this.setTitle(appName);
        URL iconUrl = MainWindow.class.getResource("/icon.png");
        if (iconUrl != null)
        {
            ImageIcon icon = new ImageIcon(iconUrl);
            this.setIconImage(icon.getImage());
        }

        synchronized (mutex)
        {
            // read application configuration
            AppConfig.getInstance().readFromFile();
        }

        final SoccerEngine soccerEngine = new SoccerEngine();
        soccerEngine.resetGame();

        final Court courtView = new Court(soccerEngine.getField(), soccerEngine.getDifficultyLevel());
        soccerController = new SoccerController(soccerEngine, courtView);
        courtView.setPlayers(soccerController.getPlayers());

        // add listeners / observers
        soccerEngine.setAnalysisObserver(soccerController);
        courtView.addMouseListener(soccerController);
        courtView.addMouseMotionListener(soccerController);
        courtView.addMouseWheelListener(soccerController);
        courtView.addComponentListener(soccerController);
        courtView.addKeyListener(soccerController);

        courtView.setFocusable(true);
        courtView.requestFocusInWindow();

        this.getContentPane().add(courtView);
        this.setJMenuBar(createMenu());

        // initial configuration of menu items
        setMenuDifficultySelected(soccerEngine.getDifficultyLevel());
        setMenuGameRulesFirstGoalWins(soccerController.isFirstGoalWins());
        setMenuUiAnimSelected(soccerController.getUiAnimSpeed());

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocation(100, 100);
        this.setMinimumSize(new Dimension(400, 400));
        // center on screen
        this.setLocationRelativeTo(null);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                synchronized (mutex)
                {
                    // write application configuration
                    AppConfig.getInstance().writeToFile();
                }
            }
        });
    }

    private JMenuBar createMenu()
    {
        JMenuBar menuBar = new JMenuBar();

        // menu Game
        JMenu menuGame = new JMenu(" Game ");
        menuGame.setMnemonic(KeyEvent.VK_G);

        JMenuItem menuItemGameNew = createMenuItem(MenuItemType.TYPE_SIMPLE, "New", "control N",
                MenuActionCommand.GAME_NEW, this, null);
        menuGame.add(menuItemGameNew);

        menuGame.addSeparator();

        menuItemGameDiffEasy = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Difficulty easy", null,
                MenuActionCommand.GAME_DIFF_EASY, this, null);

        menuItemGameDiffMedium = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Difficulty medium", null,
                MenuActionCommand.GAME_DIFF_MEDIUM, this, null);

        menuItemGameDiffHard = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Difficulty hard", null,
                MenuActionCommand.GAME_DIFF_HARD, this, null);

        menuGame.add(menuItemGameDiffEasy);
        menuGame.add(menuItemGameDiffMedium);
        menuGame.add(menuItemGameDiffHard);
        menuGame.addSeparator();

        JMenuItem menuItemGameQuit = createMenuItem(MenuItemType.TYPE_SIMPLE, "Quit", null, MenuActionCommand.GAME_QUIT,
                this, null);
        menuGame.add(menuItemGameQuit);

        // menu Game Rules
        JMenu menuGameRules = new JMenu(" Rules ");
        menuGameRules.setMnemonic(KeyEvent.VK_R);

        menuItemGameRulesFirstGoalWins = createMenuItem(MenuItemType.TYPE_CHECKBOX, "First goal wins", null,
                MenuActionCommand.GAME_RULES_FIRST_GOAL_WINS, this, null);
        menuGameRules.add(menuItemGameRulesFirstGoalWins);

        // menu Ui
        JMenu menuUi = new JMenu(" User Interface ");
        menuUi.setMnemonic(KeyEvent.VK_U);

        menuItemUiAnimFast = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Animation fast", null,
                MenuActionCommand.UI_ANIM_FAST, this, null);

        menuItemUiAnimMedium = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Animation medium", null,
                MenuActionCommand.UI_ANIM_MEDIUM, this, null);

        menuItemUiAnimSlow = createMenuItem(MenuItemType.TYPE_CHECKBOX, "Animation slow", null,
                MenuActionCommand.UI_ANIM_SLOW, this, null);

        menuUi.add(menuItemUiAnimFast);
        menuUi.add(menuItemUiAnimMedium);
        menuUi.add(menuItemUiAnimSlow);

        menuBar.add(menuGame);
        menuBar.add(menuGameRules);
        menuBar.add(menuUi);

        return menuBar;
    }

    private static JMenuItem createMenuItem(MenuItemType type, String text, String acceleratorKeyStroke,
            String actionCommand, ActionListener actionListener, ItemListener itemListener)
    {
        JMenuItem menuItem;

        switch (type)
        {
        case TYPE_CHECKBOX:
            menuItem = new JCheckBoxMenuItem(text);
            break;
        case TYPE_SIMPLE:
        default:
            menuItem = new JMenuItem(text);
            break;
        }

        menuItem.setActionCommand(actionCommand);

        if (acceleratorKeyStroke != null && acceleratorKeyStroke.length() > 0)
        {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(acceleratorKeyStroke);
            if (keyStroke != null)
                menuItem.setAccelerator(keyStroke);
        }

        if (actionListener != null)
            menuItem.addActionListener(actionListener);
        if (itemListener != null)
            menuItem.addItemListener(itemListener);

        return menuItem;
    }

    /**
     * ActionListener interface implementation.
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getActionCommand() == MenuActionCommand.GAME_QUIT)
        {
            MainWindow.this.dispose();
            // application will quit
            System.exit(0);
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_EASY)
        {
            setMenuDifficultySelected(0);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_DIFFICULTY_LEVEL,
                    String.valueOf(0));
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_MEDIUM)
        {
            setMenuDifficultySelected(1);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_DIFFICULTY_LEVEL,
                    String.valueOf(1));
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_DIFF_HARD)
        {
            setMenuDifficultySelected(2);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_DIFFICULTY_LEVEL,
                    String.valueOf(2));
        }
        else if (event.getActionCommand() == MenuActionCommand.GAME_RULES_FIRST_GOAL_WINS)
        {
            boolean firstGoalWins = menuItemGameRulesFirstGoalWins.isSelected();
            soccerController.setFirstGoalWins(firstGoalWins);

            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_FIRST_GOAL_WINS,
                    String.valueOf(firstGoalWins ? 1 : 0));
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_SLOW)
        {
            setMenuUiAnimSelected(0);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_UI_ANIM_SPEED, String.valueOf(0));
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_MEDIUM)
        {
            setMenuUiAnimSelected(1);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_UI_ANIM_SPEED, String.valueOf(1));
        }
        else if (event.getActionCommand() == MenuActionCommand.UI_ANIM_FAST)
        {
            setMenuUiAnimSelected(2);
            AppConfig.getInstance().getAppProperties().setProperty(AppConfig.PROP_KEY_UI_ANIM_SPEED, String.valueOf(2));
        }

        // dispatch also to soccerController
        if (soccerController != null)
        {
            soccerController.actionPerformed(event);
        }
    }

    private void setMenuDifficultySelected(int difficulty)
    {
        menuItemGameDiffEasy.setSelected(false);
        menuItemGameDiffMedium.setSelected(false);
        menuItemGameDiffHard.setSelected(false);

        if (difficulty == 0)
        {
            menuItemGameDiffEasy.setSelected(true);
        }
        else if (difficulty == 1)
        {
            menuItemGameDiffMedium.setSelected(true);
        }
        else
        {
            menuItemGameDiffHard.setSelected(true);
        }
    }

    private void setMenuGameRulesFirstGoalWins(boolean firstGoalWins)
    {
        menuItemGameRulesFirstGoalWins.setSelected(firstGoalWins);
    }

    private void setMenuUiAnimSelected(int speed)
    {
        menuItemUiAnimSlow.setSelected(false);
        menuItemUiAnimMedium.setSelected(false);
        menuItemUiAnimFast.setSelected(false);

        if (speed == 0)
        {
            menuItemUiAnimSlow.setSelected(true);
        }
        else if (speed == 1)
        {
            menuItemUiAnimMedium.setSelected(true);
        }
        else
        {
            menuItemUiAnimFast.setSelected(true);
        }
    }
}
