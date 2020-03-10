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

public class Game extends JFrame implements GLEventListener, KeyListener {
    private static final long serialVersionUID = 1L;

    private final int width = 1920;
    private final int height = 1080;

    private final String name = "Minimal OpenGL";

    private static GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices()[0];

    private final FPSAnimator animator;
    private final TextRenderer fpsRenderer;

    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClearColor(0.392f, 0.584f, 0.929f, 1.0f);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
        animator.stop();
        fpsRenderer.dispose();
        device.setFullScreenWindow(null);
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        System.out.println("Draw: start");

        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        int[] src = new int[width * height];
        int black = 0xff000000;
        int white = 0xffffffff;
        Random random = new Random();

        for (int a = 0; a < height; a++) {
            for (int b = 0; b < width; b++) {
                src[a * width + b] = random.nextBoolean() ? white : black;
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

        fpsRenderer.beginRendering(width, height);
        fpsRenderer.setColor(Color.CYAN);
        fpsRenderer.draw("FPS: " + animator.getFPS(), 10, 10);
        fpsRenderer.endRendering();

        System.out.println("Draw: done");
        gl.glFlush();
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    public Game() throws HeadlessException {
        super("Minimal OpenGL");

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        this.add(canvas);

        this.setName(this.name);
        this.setSize(this.width, this.height);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        device.setFullScreenWindow(this);

        canvas.requestFocusInWindow();

        final FPSAnimator animator = new FPSAnimator(canvas, 60,true);
        this.animator = animator;
        animator.start();

        this.fpsRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 40));
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

    public void play() {

    }
}
