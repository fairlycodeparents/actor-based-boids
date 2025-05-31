package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * This actor supervises the Boids simulation, managing its lifecycle and state transitions.
 * It can start, stop, pause, and resume the simulation.
 */
public class SupervisorActor extends AbstractActorWithStash {

    private final LoggingAdapter log;
    private Receive stoppedBehavior, runningBehavior, pausedBehavior;

    private int numBoids;

    /**
     * This message allows to start the simulation with a specified number of boids.
     * @param numBoids the number of boids to initialize
     */
    public record StartMsg(int numBoids) { }

    /**
     * This message allows to stop the simulation.
     */
    public static final class StopMsg { }

    /**
     * This message allows to pause the simulation.
     */
    public static final class PauseMsg { }

    /**
     * This message allows to resume the simulation after a pause.
     */
    public static final class ResumeMsg { }

    private void unknownMsgHandler(Object msg) {
        log.info("Received unknown message: " + msg);
    }

    public SupervisorActor() {
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.stoppedBehavior = receiveBuilder()
                .match(StartMsg.class, msg -> {
                    this.numBoids = msg.numBoids; //TODO: initialize n boids actors
                    getContext().become(this.runningBehavior);
                })
                .matchAny(this::unknownMsgHandler)
                .build();
        this.runningBehavior = receiveBuilder()
                .match(StopMsg.class, msg -> {
                    //TODO: kill all boids actors
                    getContext().become(this.stoppedBehavior);
                })
                .match(PauseMsg.class, msg -> getContext().become(this.pausedBehavior))
                .matchAny(this::unknownMsgHandler)
                .build();
        this.pausedBehavior = receiveBuilder()
                .match(ResumeMsg.class, msg -> getContext().become(this.runningBehavior))
                .matchAny(this::unknownMsgHandler)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return this.stoppedBehavior;
    }

    /**
     * Creates Props for a supervisor actor.
     * @return a Props for creating a supervisor actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(SupervisorActor.class);
    }
}
