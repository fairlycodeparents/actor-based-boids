package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.view.View;

import java.util.List;

/**
 * This actor represents a view for the Boids simulation.
 */
public class ViewActor extends AbstractActor {

    private final View view;
    private final LoggingAdapter log;
    private final Receive behavior;

    /**
     * Message which contains the results to be rendered by the view.
     * @param FPS the frame rate
     * @param boids the list of boids to render
     */
    public record RenderResultsMsg(int FPS, List<Boid> boids) { }

    /**
     * Message to update the pause state of the view.
     * @param isPaused true if the simulation is paused, false otherwise
     */
    public record SetPauseStateMsg(boolean isPaused) { }

    public ViewActor(View view) {
        this.view = view;
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.behavior = receiveBuilder()
                .match(RenderResultsMsg.class, msg -> this.view.render(msg.FPS(), msg.boids()))
                .match(SetPauseStateMsg.class, msg -> this.view.updatePauseState(msg.isPaused))
                .match(SupervisorActor.StopMsg.class, msg -> view.start())
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return this.behavior;
    }

    /**
     * Creates Props for a view actor.
     * @param view the view to be used by the actor
     * @return a Props for creating view actor, which can then be further configured
     */
    public static Props props(View view) {
        return Props.create(ViewActor.class, view);
    }
}
