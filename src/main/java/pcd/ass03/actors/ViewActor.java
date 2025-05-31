package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.view.View;

import java.util.List;

public class ViewActor extends AbstractActorWithStash {

    private final View view;
    private final LoggingAdapter log;
    private final Receive behavior;

    /**
     * Message which contains the results to be rendered by the view.
     * It includes the FPS and the list of boids.
     */
    public record RenderResultsMsg(int FPS, List<Boid> boids) { }

    public ViewActor(View view) {
        this.view = view;
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.behavior = receiveBuilder()
                .match(RenderResultsMsg.class, msg -> this.view.render(msg.FPS(), msg.boids()))
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {
        return this.behavior;
    }

    public static Props props(View view) {
        return Props.create(ViewActor.class, view);
    }

}
