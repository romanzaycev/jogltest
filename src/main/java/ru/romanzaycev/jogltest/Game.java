package ru.romanzaycev.jogltest;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.IntBuffer;
import java.util.Random;

class Board {
    private final int width;
    private final int height;
    private final int[] state;

    private final static int U_ALIVE = 1;

    private final static int U_0 = 1 << 1;
    private final static int U_1 = 1 << 2;
    private final static int U_2 = 1 << 3;
    private final static int U_3 = 1 << 4;
    private final static int U_4 = 1 << 5;
    private final static int U_5 = 1 << 6;
    private final static int U_6 = 1 << 7;
    private final static int U_7 = 1 << 8;

    private final static int[] xLine = {-1,  0,  1,  1,  1,  0, -1, -1};
    private final static int[] yLine = {-1, -1,  -1,  0,  1,  1,  1,  0};

    private final static int[] nPos    =  {  4,      5,      6,      7,      0,      1,      2,      3};
    private final static int[] nPosVal =  {U_4,    U_5,    U_6,    U_7,    U_0,    U_1,    U_2,    U_3};

    public Board(Board thatBoard) {
        this.width = thatBoard.width;
        this.height = thatBoard.height;
        state = new int[width * height];
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        state = new int[width * height];
    }

    public final void setAlive(int x, int y) {
        setAliveLinearized(getLinearizedXY(x, y));
    }

    public final void setAliveLinearized(int xy) {
        state[xy] |= U_ALIVE;
        int x = getX(xy);
        int y = getY(xy);

        for (int i = 0; i < 8; i++) {
            int xN = xLine[i] + x;
            int yN = yLine[i] + y;

            if (xN >= 0 && xN < width && yN >= 0 && yN < height) {
                state[getLinearizedXY(xN, yN)] |= nPosVal[nPos[i]];
            }
        }
    }

    public final void setDead(int x, int y) {
        setDeadLinearized(getLinearizedXY(x, y));
    }

    public final void setDeadLinearized(int xy) {
        state[xy] &= ~ U_ALIVE;
        int x = getX(xy);
        int y = getY(xy);

        for (int i = 0; i < 8; i++) {
            int xN = xLine[i] + x;
            int yN = yLine[i] + y;

            if (xN >= 0 && xN < width && yN >= 0 && yN < height) {
                state[getLinearizedXY(xN, yN)] &= ~ nPosVal[nPos[i]];
            }
        }
    }

    public boolean isAlive(int x, int y) {
        return isAliveLinearized(getLinearizedXY(x, y));
    }

    public boolean isAliveLinearized(int xy) {
        return (state[xy] & U_ALIVE) == U_ALIVE;
    }

    public int getAliveCountN(int x, int y) {
        return getAliveCountNLinearized(getLinearizedXY(x, y));
    }

    public int getAliveCountNLinearized(int xy) {
        int s = state[xy];

        if (s == U_ALIVE || s == 0) {
            return 0;
        }

        int r = 0;

        r += ((s & U_0) == U_0) ? 1 : 0;
        r += ((s & U_1) == U_1) ? 1 : 0;
        r += ((s & U_2) == U_2) ? 1 : 0;
        r += ((s & U_3) == U_3) ? 1 : 0;
        r += ((s & U_4) == U_4) ? 1 : 0;
        r += ((s & U_5) == U_5) ? 1 : 0;
        r += ((s & U_6) == U_6) ? 1 : 0;
        r += ((s & U_7) == U_7) ? 1 : 0;

        return r;
    }

    private int getLinearizedXY(int x, int y) {
        if (x == 0 && y == 0) {
            return 0;
        }

        return width * y + x;
    }

    private int getX(int linearizedXY) {
        return linearizedXY % width;
    }

    private int getY(int linearizedXY) {
        return (int)Math.ceil(linearizedXY / height);
    }
}

public class Game extends JFrame implements GLEventListener, KeyListener {
    private static final long serialVersionUID = 1L;

    private final int width = 1000;
    private final int height = 1000;

    private final String name = "Minimal OpenGL";

    private static GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices()[0];

    private final FPSAnimator animator;

    private Board board;

    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1.0f);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
        animator.stop();
        device.setFullScreenWindow(null);
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        int[] src = new int[width * height];
        int black = 0xff000000;
        int white = 0xffffffff;

        updateWorld();

        /*for (int i = 0; i < width * height; i++) {
            src[i] = board.isAliveLinearized(i) ? white : black;
        }*/

        for (int a = 0; a < height; a++) {
            for (int b = 0; b < width; b++) {
                int pos = a * width + b;
                src[pos] = board.isAliveLinearized(pos) ? white : black;
            }
        }

        gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, 0);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, 0);

        gl.glDrawPixels(
            width,
            height,
            GL.GL_RGBA,
            GL.GL_UNSIGNED_BYTE,
            IntBuffer.wrap(src)
        );

        gl.glFlush();
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    public Game() throws HeadlessException {
        super("Minimal OpenGL");

        this.board = new Board(width, height);

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        this.add(canvas);

        this.setName(this.name);
        this.setSize(this.width, this.height);
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        //this.setResizable(false);
        //device.setFullScreenWindow(this);

        canvas.requestFocusInWindow();

        final FPSAnimator animator = new FPSAnimator(canvas, 60,true);
        this.animator = animator;
        animator.start();
    }

    public void keyTyped(KeyEvent keyEvent) {

    }

    public void keyPressed(KeyEvent keyEvent) {

    }

    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        }
    }

    public void play() throws InterruptedException {
        Random random = new Random();

        for (int i = 0; i < width * height; i++) {
            if (random.nextBoolean()) {
                board.setAliveLinearized(i);
            } else {
                board.setDeadLinearized(i);
            }
        }
    }

    private void updateWorld() {
        synchronized (this) {
            Board nextGeneration = new Board(width, height);

            for (int i = 0; i < width * height; i++) {
                int n = this.board.getAliveCountNLinearized(i);
                boolean isAlive = this.board.isAliveLinearized(i);

                if (isAlive && (n == 2 || n == 3) || !isAlive && n == 3) {
                    nextGeneration.setAliveLinearized(i);
                } else {
                    nextGeneration.setDeadLinearized(i);
                }
            }

            board = nextGeneration;
        }
    }
}
