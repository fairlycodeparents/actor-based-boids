package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.P2d;
import pcd.ass03.model.V2d;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.SliderUI;

/**
 * This actor supervises the Boids simulation, managing its lifecycle and state transitions.
 * It can start, stop, pause, and resume the simulation.
 */
public class SupervisorActor extends AbstractActorWithStash {

    private static final int SLIDER_VALUE = 10;
    private static final double WIDTH = 1000;
    private static final double HEIGHT = 1000;
    private static final double MAX_SPEED = 4.0;

    private final LoggingAdapter log;
    private final List<ActorRef> boidActors;
    private Receive stoppedBehavior, runningBehavior, pausedBehavior;
    private static int alignmentValue = SLIDER_VALUE;
    private static int cohesionValue = SLIDER_VALUE;
    private static int separationValue = SLIDER_VALUE;
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

    public static final class SeparationMsg {
        private final int value;

        public SeparationMsg(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final class CohesionMsg {
        private final int value;

        public CohesionMsg(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final class AlignmentMsg {
        private final int value;

        public AlignmentMsg(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    private void unknownMsgHandler(Object msg) {
        log.info("Received unknown message: " + msg);
    }

    public SupervisorActor() {
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.boidActors = new ArrayList<>();
        this.stoppedBehavior = receiveBuilder()
                .match(StartMsg.class, msg -> {
                    log.info("Starting simulation with {} boids", msg.numBoids); // TODO: log used as a debugging tool
                    if (msg.numBoids > this.boidActors.size()) {
                        for (int i = this.boidActors.size(); i < msg.numBoids; i++) {
                            P2d pos = new P2d(
                                    -WIDTH / 2 + Math.random() * WIDTH,
                                    -HEIGHT / 2 + Math.random() * HEIGHT
                            );
                            V2d vel = new V2d(
                                    Math.random() * MAX_SPEED / 2 - MAX_SPEED / 4,
                                    Math.random() * MAX_SPEED / 2 - MAX_SPEED / 4
                            );
                            ActorRef boidActor = getContext().actorOf(BoidActor.props(vel, pos), "boid-" + i);
                            this.boidActors.add(boidActor);
                        }
                    } else if (msg.numBoids < this.boidActors.size()) {
                        for (int i = msg.numBoids - 1; i <= this.boidActors.size(); i++) {
                            ActorRef boidActor = this.boidActors.getLast();
                            log.info("Stopping boid actor: {}", boidActor.path().name());
                            getContext().stop(boidActor);
                            this.boidActors.remove(boidActor);
                        }
                    }
                    getContext().become(this.runningBehavior);
                    log.info("Current boid actors:" +
                            this.boidActors.stream().map(act -> act.path().name()).toList());
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

    private void broadcastToBoids(Object msg) {
        // TODO
        log.info("Broadcasting message to boids: " + msg);
        log.info("ALIGNMENT VALUE: " + alignmentValue);
        log.info("COHESION VALUE: " + cohesionValue);
        log.info("SEPARATION VALUE: " + separationValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AlignmentMsg.class, msg -> {
                    this.alignmentValue = msg.getValue();
                    broadcastToBoids(msg);
                })
                .match(CohesionMsg.class, msg -> {
                    this.cohesionValue = msg.getValue();
                    broadcastToBoids(msg);
                })
                .match(SeparationMsg.class, msg -> {
                    this.separationValue = msg.getValue();
                    broadcastToBoids(msg);
                })
                .matchAny(this::unknownMsgHandler)
                .build();
    }

    /**
     * Creates Props for a supervisor actor.
     * @return a Props for creating a supervisor actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(SupervisorActor.class);
    }
}
