package pcd.ass03.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying the boids simulation.
 */
public class BoidsPanel extends JPanel {

    private int frameRate;

    /**
     * Constructor for the BoidsPanel.
     * @param view the view
     * @param width the logical width
     * @param height the logical height
     */
    public BoidsPanel(View view, double width, double height) {
    }

    /**
     * Sets the frame rate.
     * @param frameRate the frame rate
     */
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + frameRate, 10, 40);
    }
}

