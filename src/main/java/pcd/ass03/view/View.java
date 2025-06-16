package pcd.ass03.view;

import akka.actor.ActorRef;
import pcd.ass03.model.Boid;
import java.util.List;

/**
 * Interface representing the view in the Boids simulation.
 */
public interface View {
    /**
     * Sets the supervisor actor for the view.
     * @param supervisor the supervisor actor reference
     */
    void setSupervisorActor(ActorRef supervisor);

    /**
     * Renders the boids on the view.
     * @param FPS the frames per second to display
     * @param boids the list of boids to render
     */
    void render(int FPS, List<Boid> boids);

    /**
     * Starts the view, initializing any necessary components.
     */
    void start();

    /**
     * Updates the pause state of the view.
     * @param isPaused true if the simulation is paused, false otherwise
     */
    void updatePauseState(boolean isPaused);

    /**
     * Returns the current width of the frame.
     * @return the width of the frame
     */
    int getWidth();

    /**
     * Returns the current height of the frame.
     * @return the height of the frame
     */
    int getHeight();
}
