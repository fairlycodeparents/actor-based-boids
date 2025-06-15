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

    private static final double MAX_SPEED = 4.0;
    private static final int FPS = 60;

    private final LoggingAdapter log;
    private final List<ActorRef> boidActors;
    private final List<Boid> boids;
    private final Receive stoppedBehavior;
    private Receive runningBehavior;
    private Receive pausedBehavior;
    private double alignmentWeight, cohesionWeight, separationWeight;
    private ActorRef viewActor;
    private long lastFrameTime;

    /**
     * This message allows to set the view actor that will render the results of the simulation.
     * @param viewActor the actor that will render the results
     */
    public record SetViewActorMsg(ActorRef viewActor) { }

    /**
     * This message allows to start the simulation with a specified number of boids.
     * @param numBoids the number of boids to initialize
     */
    public record StartMsg(int numBoids, double frameSize) { }

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

    /**
     * This message allows to update the weights of the boids behaviors.
     * @param weight the weight to update
     * @param value the new value for the weight
     */
    public record UpdateWeightsMsg(Weights weight, double value) { }

    /**
     * This enum represents the different weights that can be updated in the boids behaviors.
     */
    public enum Weights {
        ALIGNMENT, COHESION, SEPARATION
    }

    /**
     * This message is sent to signal that a boid has been updated.
     * @param boid the updated boid
     */
    public record UpdatedBoidMsg(Boid boid) { }

    /**
     * This class represents a tick in the simulation. It signals that the simulation should update its state.
     */
    public static class TickMsg { }

    public SupervisorActor() {
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.boidActors = new ArrayList<>();
        this.boids = new ArrayList<>();

        this.stoppedBehavior = receiveBuilder()
                .match(SetViewActorMsg.class, msg -> this.viewActor = msg.viewActor())
                .match(StartMsg.class, msg -> {
                    for(ActorRef actor : boidActors){
                        getContext().stop(actor);
                    }
                    boidActors.clear();
                    lastFrameTime = System.currentTimeMillis();
                    log.info("Starting simulation with {} boids", msg.numBoids); // TODO: log used as a debugging tool
                    for (int i = 0; i < msg.numBoids; i++) {
                        P2d pos = new P2d(
                                -msg.frameSize / 2 + Math.random() * msg.frameSize,
                                -msg.frameSize / 2 + Math.random() * msg.frameSize
                        );
                        V2d vel = new V2d(
                                Math.random() * MAX_SPEED / 2 - MAX_SPEED / 4,
                                Math.random() * MAX_SPEED / 2 - MAX_SPEED / 4
                        );
                        ActorRef boidActor = getContext().actorOf(BoidActor.props(vel, pos), "boid-" + i);
                        this.boidActors.add(boidActor);
                        this.boids.add(i,new Boid(pos,vel));
                    }
                    getContext().become(this.runningBehavior);
                    this.updateBoids();
                    getSelf().tell(new TickMsg(), getSelf());
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
                        viewActor.tell(msg, ActorRef.noSender());
                        log.info("Simulation stopped");
                        getContext().become(this.stoppedBehavior);
                })
                .match(PauseMsg.class, msg -> {
                        viewActor.tell(new ViewActor.SetPauseStateMsg(true), getSelf());
                        log.info("Simulation paused");
                        getContext().become(this.pausedBehavior);
                })
                .match(UpdateWeightsMsg.class, msg -> {
                        this.updateWeight(msg);
                        log.info("Updated " + msg.weight);
                })
                .match(UpdatedBoidMsg.class, msg -> this.boids.add(msg.boid))
                .match(TickMsg.class, msg -> {
                        int fps = this.updateFPS();
                        log.info("Tick received: " + fps);
                        if (boids.size() == boidActors.size() && fps > 0) {
                            if (viewActor != null) {
                                viewActor.tell(
                                        new ViewActor.RenderResultsMsg(fps, new ArrayList<>(this.boids)),
                                        getSelf()
                                );
                                log.info("render of " + this.boids.size() + " boids");
                                this.updateBoids();
                            } else {
                                log.warning("ViewActor not set - cannot send render results");
                            }
                        }
                        getSelf().tell(new TickMsg(), getSelf());
                })
                .matchAny(this::unknownMsgHandler)
                .build();

        this.pausedBehavior = receiveBuilder()
                .match(ResumeMsg.class, msg -> {
                        viewActor.tell(new ViewActor.SetPauseStateMsg(false), getSelf());
                        log.info("Simulation resumed");
                        getContext().become(this.runningBehavior);
                })
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

    private int updateFPS() {
        long currentTime = System.currentTimeMillis();
        var dtElapsed = currentTime - lastFrameTime;
        if (dtElapsed < FPS) {
            return -1;
        } else {
            return (int) (1000/dtElapsed);
        }
    }

    private void updateBoids() {
        for (ActorRef boidActor : this.boidActors) {
            boidActor.tell(new BoidActor.UpdateRequestMsg(
                    new ArrayList<>(this.boids), this.alignmentWeight, this.cohesionWeight, this.separationWeight,
                    MAX_SPEED, -1000, 1000, -1000, 1000, 2000, 2000
            ), getSelf());
        }
        this.boids.clear();
    }

}
