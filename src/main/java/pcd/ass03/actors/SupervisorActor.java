package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;
import pcd.ass03.model.V2d;

import java.util.ArrayList;
import java.util.List;

import static pcd.ass03.Main.*;

/**
 * This actor supervises the Boids simulation, managing its lifecycle and state transitions.
 * It can start, stop, pause, and resume the simulation.
 */
public class SupervisorActor extends AbstractActor {
    private static final int MAX_FPS = 60;
    private static final double MAX_SPEED = 4.0;
    private static final double AVOID_RADIUS = 20.0;
    private static final double PERCEPTION_RADIUS = 50.0;

    private final LoggingAdapter log;
    private final List<ActorRef> boidActors;
    private final List<Boid> boids;
    private final Receive stoppedBehavior;
    private Receive runningBehavior, pausedBehavior;
    private double alignmentWeight = 1.0, cohesionWeight = 1.0, separationWeight = 1.0;
    private long lastFrameTime;

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
     * Constructor for the SupervisorActor, initializes the actor with the view and notifier actors.
     * @param viewActor the actor responsible for rendering the simulation results
     * @param notifierActor the actor responsible for managing time-based notifications
     */
    public SupervisorActor(ActorRef viewActor, ActorRef notifierActor) {
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.boidActors = new ArrayList<>();
        this.boids = new ArrayList<>();

        this.stoppedBehavior = receiveBuilder()
                .match(StartMsg.class, msg -> {
                    boidActors.forEach(actor -> getContext().stop(actor));
                    boidActors.clear();
                    lastFrameTime = System.currentTimeMillis();
                    log.info("Starting simulation with " + msg.numBoids + " boids");
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
                        boidActor.tell(new BoidActor.SetSupervisorActorMsg(getSelf()),ActorRef.noSender());
                        this.boidActors.add(boidActor);
                        this.boids.add(new Boid(pos,vel));
                    }
                    getContext().become(this.runningBehavior);
                    this.updateBoids();
                })
                .match(TimerActor.TickMsg.class, msg -> log.info("Received TickMsg but simulation's stopped"))
                .matchAny(this::unknownMsgHandler)
                .build();

        this.runningBehavior = receiveBuilder()
                .match(StopMsg.class, msg -> {
                        this.boidActors.forEach(boidActor -> getContext().stop(boidActor));
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
                        log.info("Update " + msg.weight + " weight to " + msg.value);
                })
                .match(UpdatedBoidMsg.class, msg -> {
                    this.boids.add(msg.boid);
                    if (boids.size() == boidActors.size()) {
                        long framePeriod = 1000 / MAX_FPS;
                        long currentTime = System.currentTimeMillis();
                        long dtElapsed = currentTime - lastFrameTime;
                        long remaining = framePeriod - dtElapsed;
                        if (remaining > 0) {
                            notifierActor.tell(new TimerActor.RequestNotificationMsg(remaining, currentTime), getSelf());
                        } else {
                            this.updateView(viewActor, (int) (1000 / (System.currentTimeMillis() - lastFrameTime)));
                        }
                    }
                })
                .match(TimerActor.TickMsg.class, msg ->
                        updateView(viewActor, MAX_FPS))
                .matchAny(this::unknownMsgHandler)
                .build();

        this.pausedBehavior = receiveBuilder()
                .match(ResumeMsg.class, msg -> {
                        viewActor.tell(new ViewActor.SetPauseStateMsg(false), getSelf());
                        log.info("Simulation resumed");
                        getContext().become(this.runningBehavior);
                        updateBoids();
                })
                .match(UpdateWeightsMsg.class, this::updateWeight)
                .match(TimerActor.TickMsg.class, msg -> log.info("Received TickMsg but simulation's paused"))
                .matchAny(this::unknownMsgHandler)
                .build();
    }

    private void updateView(ActorRef viewActor, int FPS) {
        List<Boid> copy = new ArrayList<>(this.boids);
        if (viewActor != null) {
            viewActor.tell(
                    new ViewActor.RenderResultsMsg(FPS, copy),
                    getSelf()
            );
            lastFrameTime = System.currentTimeMillis();
            this.updateBoids();
        } else {
            log.warning("ViewActor not set - cannot send render results");
        }
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
    public static Props props(ActorRef viewActor, ActorRef notifierActor) {
        return Props.create(SupervisorActor.class, viewActor, notifierActor);
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

    private void updateBoids() {
        List<Boid> copy = new ArrayList<>(this.boids);
        this.boidActors.forEach(boidActor -> boidActor.tell(new BoidActor.UpdateRequestMsg(
                copy, this.separationWeight, this.alignmentWeight, this.cohesionWeight,
                AVOID_RADIUS, PERCEPTION_RADIUS, MAX_SPEED, -WIDTH/2, WIDTH/2, -HEIGHT/2,
                HEIGHT/2, WIDTH, HEIGHT), getSelf()));
        this.boids.clear();
    }

}
