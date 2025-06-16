package pcd.ass03.view;

import pcd.ass03.Main;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying the boids simulation.
 */
public class BoidsPanel extends JPanel {

    private final View view;
    private List<Boid> boids;
    private int frameRate;

    /**
     * Constructor for the BoidsPanel.
     * @param view the view
     */
    public BoidsPanel(View view) {
        this.boids = new ArrayList<>();
        this.view = view;
    }

    /**
     * Sets the frame rate.
     * @param frameRate the frame rate
     */
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * Sets the boids.
     * @param boids the list of boids
     */
    public void setBoids(List<Boid> boids) {
        this.boids = boids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);

        var width = this.view.getWidth();
        var height = this.view.getHeight();
        var xScale = width / Main.WIDTH;
        var yScale = height / Main.HEIGHT;

        g.setColor(Color.BLUE);
        List<Boid> boids = new ArrayList<>(this.boids);
        for (Boid boid : boids) {
            P2d pos = boid.pos();
            int px = (int)(width / 2.0 + pos.x() * xScale);
            int py = (int)(height / 2.0 - pos.y() * yScale);
            g.fillOval(px,py, 5, 5);
        }

        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + boids.size(), 10, 25);
        g.drawString("FPS: " + frameRate, 10, 40);
    }
}

