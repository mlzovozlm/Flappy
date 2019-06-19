/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import gui.screen;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Bao Anh Luu
 */
public class control {

    private final int fallHeight = 2;
    private Thread birdThread;
    private Thread chimneyThread;
    private screen screen;
    //---------------------------------
    private int floorY;
    private int ceilY;
    private JButton ceil;
    private JButton floor;
    //---------------------------------
    private final int moveLeft = 1;
    private final int chimneyDist = 200;
    private final int chimneyGap = 100;
    private final int chimneyWidth = 50;
    private final int GAME_END = 0;
    private final int GAME_PLAY = 1;
    private final int GAME_PAUSE = 2;
    private int GAME_STATE;
    private LinkedList<JButton> chimneys;
    private int score;
    private boolean flag;
    private JLabel bird;
    private int centerY;
    private int relativeBird;

    public void addPauseAction() {
        screen.getBtnPause().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (GAME_STATE) {
                    case GAME_PLAY:
                        GAME_STATE = GAME_PAUSE;
                        flag = false;
                        screen.getBtnPause().setText("Resume");
                        break;
                    case GAME_PAUSE:
                        GAME_STATE = GAME_PLAY;
                        flag = true;
                        screen.getBtnPause().setText("Pause");
                        screen.getGamePanel().requestFocus(true);
                        break;
                    case GAME_END:
                        GAME_STATE = GAME_PLAY;
                        score = 0;
                        screen.getLblScore().setText("0");
                        initButton();
                        screen.getGamePanel().requestFocus(true);

                        screen.getBtnPause().setText("Pause");
                        flag = true;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void addKeyAction() {
        screen.getGamePanel().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    bird.setLocation(bird.getX(), bird.getY() - screen.jumpHeight);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    //---------------------------------here----------------------------------------
    public void addScreenResize() {
        screen.getGamePanel().addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerY = screen.getGamePanel().getHeight() / 2;
                bird.setLocation(100, centerY - relativeBird);
                ceilY = centerY - 150;
                floorY = centerY + 150;
                ceil.setSize(screen.getGamePanel().getWidth(), ceilY);
                ceil.setLocation(0, 0);
                floor.setSize(screen.getGamePanel().getWidth(), screen.getGamePanel().getHeight() - floorY);
                floor.setLocation(0, floorY);
                for (int i = 0; i < chimneys.size(); i++) {
                    JButton btn = chimneys.get(i);
                    if (i % 2 == 0) {
                        btn.setLocation(btn.getX(), ceilY);
                    } else {
                        btn.setLocation(btn.getX(), floorY - btn.getHeight());
                    }
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
    //-----------------------------------------------------------------------------

    public control() {
        GAME_STATE = GAME_END;
        screen = new screen();
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screen.setVisible(true);
        chimneys = new LinkedList<>();
        centerY = screen.getGamePanel().getHeight() / 2;
        score = 0;
        flag = false;
        bird = new JLabel();
        bird.setSize(40, 40);
        screen.getGamePanel().add(bird);
        bird.setLocation(100, centerY);
        bird.setVisible(true);
        bird.setBorder(new LineBorder(Color.BLACK, 1));
        //--------------------------------all--------------------------------------
        screen.getGamePanel().setMinimumSize(new Dimension(500, 320));
        screen.setMinimumSize(new Dimension(500, 500));
        floorY = centerY + 150;
        floor = new JButton();
        floor.setSize(screen.getGamePanel().getWidth(), screen.getGamePanel().getHeight() - floorY);
        floor.setLocation(0, ceilY);
        ceilY = centerY - 150;
        ceil = new JButton();
        ceil.setSize(screen.getGamePanel().getWidth(), ceilY);
        ceil.setLocation(0, 0);
        screen.getGamePanel().add(floor);
        screen.getGamePanel().add(ceil);
        addScreenResize();
        //-------------------------------------------------------------------------
        screen.repaint();
        addKeyAction();
        addPauseAction();
        addSaveAction();
        initThread();
        birdThread.start();
        chimneyThread.start();
    }

    public void BirdUpdate(int fallHeight) {
        int posX = bird.getX();
        int posY = bird.getY();
        bird.setLocation(posX, posY + fallHeight);
        relativeBird = centerY - bird.getY();
    }

    public void gameEnd() {
        flag = false;
        GAME_STATE = GAME_END;
        screen.getBtnPause().setText("Start");
        for (Iterator i = chimneys.iterator(); i.hasNext();) {
            JButton btn = (JButton) i.next();
            screen.getGamePanel().remove(btn);
            i.remove();
        }
        centerY = screen.getGamePanel().getHeight() / 2;
        bird.setLocation(100, centerY);
        screen.getGamePanel().repaint();
    }

    boolean isCollide() {
        JLabel lbl = bird;
        for (Iterator i = chimneys.iterator(); i.hasNext();) {
            JButton btn = (JButton) i.next();
            if (btn.getX() < lbl.getX() + lbl.getWidth()
                    && btn.getX() + btn.getWidth() > lbl.getX()
                    && btn.getY() < lbl.getY() + lbl.getHeight()
                    && btn.getHeight() + btn.getY() > lbl.getY()) {
                return true;
            }
        }
        //------------------------------------------------------------------
        if (lbl.getY() <= ceilY //here
                || lbl.getY() + lbl.getHeight() >= floorY) { //here
            return true;
        }
        //------------------------------------------------------------------
        return false;
    }

    public void increaseScore() {
        score++;
        screen.getLblScore().setText(Integer.toString(score));
    }

    public void ChimneyUpdate() {
        //delete passed chimney + update score
        for (Iterator i = chimneys.iterator(); i.hasNext();) {
            JButton btn = (JButton) i.next();
            btn.setLocation(btn.getX() - moveLeft, btn.getY());
            if (btn.getX() <= 0) {
                increaseScore();
                screen.getGamePanel().remove(btn);
                i.remove();
            }
        }
        //add new chimney
        if (chimneys.getLast().getX() + chimneyDist < screen.getGamePanel().getWidth() - chimneyWidth) {
            Random random = new Random();
            //---------------------------------------------------------------------
            int topHeight = random.nextInt(10) * 16 + 10;
            int posX = chimneys.getLast().getX() + chimneyDist;
            JButton top = new JButton();
            top.setLocation(posX, ceilY); //here
            top.setSize(chimneyWidth, topHeight);
            chimneys.add(top);
            screen.getGamePanel().add(top);
            JButton bot = new JButton();
            bot.setLocation(posX, topHeight + ceilY + chimneyGap); //here
            bot.setSize(chimneyWidth, floorY - topHeight - chimneyGap - ceilY); //here
            chimneys.add(bot);
            screen.getGamePanel().add(bot);
            //---------------------------------------------------------------------
        }
    }

    public void initButton() {
        int i = 0;
        while (i < 5) {
            Random random = new Random();
            int topHeight = random.nextInt(10) * 16 + 10;
            int posX = 300 + i * chimneyDist; //here
            if (posX < screen.getWidth() - chimneyWidth) {
                JButton top = new JButton();
                //-----------------------------------------------------------------
                top.setLocation(posX, ceilY); //here
                top.setSize(chimneyWidth, topHeight);
                chimneys.add(top);
                screen.getGamePanel().add(top);
                JButton bot = new JButton();
                bot.setLocation(posX, topHeight + chimneyGap + ceilY); //here
                bot.setSize(chimneyWidth, floorY - topHeight - chimneyGap - ceilY); //here
                chimneys.add(bot);
                screen.getGamePanel().add(bot);
                //-----------------------------------------------------------------
            }
            i++;
        }
    }

    public void initThread() {
        birdThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (flag) {
                        screen.getGamePanel().repaint();
                        BirdUpdate(fallHeight);
                    }
                    try {
                        sleep(50);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(control.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        chimneyThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (flag) {
                        ChimneyUpdate();
                        if (isCollide()) {
                            gameEnd();
                        }
                    }
                    try {
                        sleep(25);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(control.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

    }

    public void addSaveAction() {
        screen.getBtnSave().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (screen.getFile() == null) {
                    saveAsToFile(screen);
                } else {
                    writeToFile(screen);
                }
            }
        });
    }

    private void saveAsToFile(screen screen) {
        File checkFile = null;
        JFileChooser chooser = new JFileChooser();
        setupFileChooser(chooser);
        while (true) {
            chooser.showSaveDialog(screen);
            checkFile = chooser.getSelectedFile();
            if (!checkFile.exists()) {
                break;
            }
            int option = JOptionPane.showConfirmDialog(screen, "Do you want to replace it?", "Save As", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                break;
            } else {
                return;
            }
        }
        screen.setFile(checkFile);
        writeToFile(screen);
    }

    private void writeToFile(screen screen) {
        FileWriter fout = null;
        try {
            if (screen.getFile() == null) {
                saveAsToFile(screen);
                return;
            }
            fout = new FileWriter(screen.getFile());
            fout.write(screen.getLblScore().getText());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void setupFileChooser(JFileChooser chooser) {

        // allow user choose file .txt
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return f.getName().endsWith(".txt");
                }
            }

            @Override
            public String getDescription() {
                return "Text Files(*.txt)";
            }
        });

        // set current directory
        chooser.setCurrentDirectory(new File("."));

    }

    public static void main(String[] args) {
        new control();
    }
}
