package View;

import Model.Coord;
import Model.Figure;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Window extends JFrame implements Runnable {

    private EndGame endGame;
    private FallingFigure activeFigure;
    private FallingFigure nextFigure;
    private Figure next;
    private Box[][] boxes;
    private Box[][] hBoxes;
    private ScoreText scoreText;
    private Timer timer;
    private int delay = 300;
    public Window(){
        boxes = new Box[Config.WIDTH][Config.HEIGHT];
        hBoxes = new Box[Config.WIDTH_HELP][Config.HEIGHT_HELP];
        next = Figure.getRandom();
        scoreText = new ScoreText();
        initForm();
        initBoxes();
        helpImage();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.NANOSECONDS);
        addKeyListener(new KeyAdapter());
        TimeAdapter t = new TimeAdapter();
        timer = new Timer(delay, t);
        timer.start();
        addFigure(next); // это старт программы
        next = initHelpBox();
    }

    private void initForm() {
        setSize(Config.WIDTH * Config.SIZE_CELL*2 + Config.SIZE_CELL, Config.HEIGHT * Config.SIZE_CELL + 40);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(new Text());
        add(scoreText);
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Tetris The Game");
        setLayout(null);
        setVisible(true);
    }
    private void initBoxes(){
        for (int x = 0; x < Config.WIDTH; x++)
            for (int y = 0; y < Config.HEIGHT; y++) {
                boxes[x][y] = new Box(x, y);
                add(boxes[x][y]);
            }
    }

    public int getScore() {
        return scoreText.getScore();
    }

    private void helpImage() {
        for (int x = 0; x < Config.WIDTH_HELP; x++)
            for (int y = 0; y < Config.HEIGHT_HELP; y++) {
                hBoxes[x][y] = new Box(x+13, y+2);
                add(hBoxes[x][y]);
            }
    }
    private Figure initHelpBox() {
        nextFigure = new FallingFigure(this, Config.X_HELP_BOX, Config.Y_HELP_BOX);
        showFigure(Config.NEXT);
        return nextFigure.getFallingFigure();
    }

    private void hideFigure(boolean isNext) {
        int a = 1;
        if (isNext) {
            Figure figure = nextFigure.getFallingFigure().show();
            if (figure == Figure.T3 || figure == Figure.J2) a++;
            for (Coord block: figure.getCells())
                setBoxColor(block.x+1, block.y+a, 0, true);
        } else {
            for (Coord block : activeFigure.getFallingFigure().getCells())
                setBoxColor(activeFigure.getPosition().x + block.x, activeFigure.getPosition().y + block.y,
                        0, false);
        }
    }

    private void showFigure(boolean isNext) {
        int a = 1;
        if (isNext) {
            Figure figure = nextFigure.getFallingFigure().show();
            if (figure == Figure.T3 || figure == Figure.J2) a++;
            for (Coord block: figure.getCells())
                setBoxColor(block.x+1, block.y+a, figure.getColorFigure(), true);
        } else {
            for (Coord block : activeFigure.getFallingFigure().getCells())
                setBoxColor(activeFigure.getPosition().x + block.x, activeFigure.getPosition().y + block.y,
                        activeFigure.getFallingFigure().getColorFigure(), false);
        }
    }


    private void moveActiveFigure(int sx, int sy) {
        hideFigure(Config.ACTIVE);
        activeFigure.moveFigure(sx, sy);
        showFigure(Config.ACTIVE);
    }

    private void turnActiveFigure() {
        hideFigure(Config.ACTIVE);
        activeFigure.turnFigure();
        showFigure(Config.ACTIVE);
    }

    private void setBoxColor(int x, int y, int color, boolean isNext) {
        if (y < 0 || y >= Config.HEIGHT) return;
        if (isNext)
            hBoxes[x][y].setColor(color);
        else
            boxes[x][y].setColor(color);
    }

    public int getBoxColor(int x, int y) {
        if (y < 0 || y >= Config.HEIGHT) return -1;
        return boxes[x][y].getColor();
    }

    private void addFigure(Figure figure) {
        activeFigure = new FallingFigure(this, Config.START_X, Config.START_Y, figure);
        if (activeFigure.canPlaceFigure())
            showFigure(Config.ACTIVE);
        else {
            endGame = new EndGame(this);
            endGame.setVisible(true);
            restartTheGame();
        }
    }

    private void restartTheGame() {
        timer.stop();
        dispose();
        Window game = new Window();
    }

    private void cleanLine() {
        int bonus = 0;
        for (int y = Config.HEIGHT - 1; y >= 0; y--)
            while (isFullLine(y)) {
                bonus++;
                dropLine(y);
            }
        if (scoreText.plusScore(bonus)) {
            delay /= 2;
            timer.setDelay(delay);
        }
    }

    private void dropActiveFigure() {
        hideFigure(Config.ACTIVE);
        activeFigure.fastDrop();
        showFigure(Config.ACTIVE);
    }

    private void dropLine(int y) {
        for (int my = y - 1; my >= 0; my--)
            for (int x = 0; x < Config.WIDTH; x++)
                setBoxColor(x, my + 1, getBoxColor(x, my), false);
        for (int x = 0; x < Config.WIDTH; x++)
            setBoxColor(x, 0, 0, false);
    }
    private boolean isFullLine(int y) {
        for (int x = 0; x < Config.WIDTH; x++)
            if (getBoxColor(x, y) == 0) return false;
        return true;
    }

    @Override
    public void run() {
        repaint();
    }
    private class KeyAdapter implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: moveActiveFigure(-1, 0);break;
                case KeyEvent.VK_RIGHT: moveActiveFigure(+1, 0); break;
                case KeyEvent.VK_UP: turnActiveFigure(); break;
                case KeyEvent.VK_SPACE: restartTheGame(); break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_DOWN) dropActiveFigure();
        }
    }

    private class TimeAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            moveActiveFigure(0, 1);
            if (activeFigure.dropped()) {
                cleanLine();
                addFigure(next);
                hideFigure(Config.NEXT);
                next = initHelpBox();
            }
        }
    }

}
