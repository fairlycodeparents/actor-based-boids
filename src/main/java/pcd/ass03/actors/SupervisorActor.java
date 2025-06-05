package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;
import pcd.ass03.model.V2d;

import java.util.ArrayList;
import java.util.List;

/**
 * This actor supervises the Boids simulation, managing its lifecycle and state transitions.
 * It can start, stop, pause, and resume the simulation.
 */
public class SupervisorActor extends AbstractActorWithStash {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 1000;
    private static final double MAX_SPEED = 4.0;

    private final LoggingAdapter log;
    private final List<ActorRef> boidActors;
    private final List<Boid> boids;
    private Receive stoppedBehavior, runningBehavior, pausedBehavior;
    private double alignmentWeight, cohesionWeight, separationWeight;

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

    public enum Weights {
        ALIGNMENT, COHESION, SEPARATION;
    }

    /**
     * This message allows to update the weights of the boids behaviors.
     * @param weight the weight to update
     * @param value the new value for the weight
     */
    public record UpdateWeightsMsg(Weights weight, double value) { }

    /**
     * This message is sent to signal that a boid has been updated.
     * @param boid the updated boid
     */
    public record UpdatedBoidMsg(Boid boid) {}

    /**
     * This class represents a tick in the simulation. It signals that the simulation should update its state.
     */
    public record TickMsg(long FPS) { }

    private void unknownMsgHandler(Object msg) {
        log.info("Received unknown message: " + msg);
    }

    private void updateWeight(UpdateWeightsMsg msg) {
        switch (msg.weight) {
            case ALIGNMENT -> this.alignmentWeight = msg.value;
            case COHESION -> this.cohesionWeight = msg.value;
            case SEPARATION -> this.separationWeight = msg.value;
        }
    }

    public SupervisorActor() {
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.boidActors = new ArrayList<>();
        this.boids = new ArrayList<>();
        this.stoppedBehavior = receiveBuilder()
                .match(StartMsg.class, msg -> {
                    for(ActorRef actor : boidActors){
                        getContext().stop(actor);
                    }
                    boidActors.clear();
                    log.info("Starting simulation with {} boids", msg.numBoids); // TODO: log used as a debugging tool
                    for (int i = 0; i < msg.numBoids; i++) {
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
                    getContext().become(this.runningBehavior);
                    log.info("Current boid actors:" +
                            this.boidActors.stream().map(act -> act.path().name()).toList());
                })
                .matchAny(this::unknownMsgHandler)
                .build();
        this.runningBehavior = receiveBuilder()
                .match(StopMsg.class, msg -> {
                    for (ActorRef boidActor : this.boidActors) {
                        getContext().stop(boidActor);
                    }
                    log.info("Simulation stopped");
                    getContext().become(this.stoppedBehavior);
                })
                .match(PauseMsg.class, msg -> getContext().become(this.pausedBehavior))
                .match(UpdateWeightsMsg.class, this::updateWeight)
                .match(UpdatedBoidMsg.class, msg -> this.boids.add(msg.boid))
                .match(TickMsg.class, msg -> {
                    // TODO: if a tick is received, the supervisor should check if the boids are updated.
                    // if they are, it should send the updated boids to the view actor.
                    // Otherwise, it should send back a msg who sent the tick to signal that more time is needed.
                })
                .matchAny(this::unknownMsgHandler)
                .build();
        this.pausedBehavior = receiveBuilder()
                .match(ResumeMsg.class, msg -> getContext().become(this.runningBehavior))
                .match(UpdateWeightsMsg.class, this::updateWeight)
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
