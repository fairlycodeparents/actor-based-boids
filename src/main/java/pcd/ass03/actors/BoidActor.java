package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;
import pcd.ass03.model.V2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This actor represents a boid in the boids simulation.
 * It processes messages to update its state based on the boids behavior.
 */
public class BoidActor extends AbstractActor {

    private final LoggingAdapter log;
    private Receive behavior;
    private V2d vel;
    private P2d pos;

    /**
     * This message allows to update the boid state.
     * @param boids the list of boids in the simulation
     * @param separation the separation factor for the boids
     * @param alignment the alignment factor for the boids
     * @param cohesion the cohesion factor for the boids
     */
    public record UpdateRequestMsg(List<Boid> boids, double separation, double alignment, double cohesion) { }

    private void unknownMsgHandler(Object msg) {
        log.info("Received unknown message: " + msg);
    }

    /**
     * Constructor for the BoidActor, initializes the actor with a given velocity and position.
     * @param vel the initial velocity of the boid
     * @param pos the initial position of the boid
     */
    public BoidActor(V2d vel, P2d pos) {
        this.vel = vel;
        this.pos = pos;
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.behavior = receiveBuilder()
                .match(UpdateRequestMsg.class, msg -> {
                    log.info("Received update request.");
                    // TODO: process the update (get neighbours, compute new velocity and position)
                })
                .matchAny(this::unknownMsgHandler)
                .build();
    }

    private List<Boid> getNearbyBoids(List<Boid> boids, double perceptionRadius) {
        var list = new ArrayList<Boid>();
        for (Boid other : boids) {
            Boid current = new Boid(this.pos, this.vel);
            if (!Objects.equals(other, current)) {
                P2d otherPos = other.pos();
                double distance = this.pos.distance(otherPos);
                if (distance < perceptionRadius) {
                    list.add(other);
                }
            }
        }
        return list;
    }

    private void update(List<Boid> nearbyBoids, double alignmentWeight, double cohesionWeight,
                       double separationWeight, double avoidRadius, double maxSpeed,
                       double minX, double maxX, double minY, double maxY, double width, double height) {

        // Calculate new velocity based on nearby boids
        V2d separation = calculateSeparation(nearbyBoids, avoidRadius);
        V2d alignment = calculateAlignment(nearbyBoids);
        V2d cohesion = calculateCohesion(nearbyBoids);

        // New velocity is a combination of current velocity and the calculated forces
        V2d newVel = vel
                .sum(alignment.mul(alignmentWeight))
                .sum(separation.mul(separationWeight))
                .sum(cohesion.mul(cohesionWeight));

        // Limit the speed of the boid
        if (newVel.abs() > maxSpeed) {
            newVel = newVel.getNormalized().mul(maxSpeed);
        }

        this.vel = newVel;

        // Calculate new position based on the updated velocity
        P2d newPos = this.pos.sum(this.vel);

        // Wrap around logic for the boid's position
        if (newPos.x() < minX) newPos = newPos.sum(new V2d(width, 0));
        if (newPos.x() >= maxX) newPos = newPos.sum(new V2d(-width, 0));
        if (newPos.y() < minY) newPos = newPos.sum(new V2d(0, height));
        if (newPos.y() >= maxY) newPos = newPos.sum(new V2d(0, -height));

        this.pos = newPos;
    }

    private V2d calculateAlignment(List<Boid> nearbyBoids) {
        if (nearbyBoids.isEmpty()) return new V2d(0, 0);
        double avgVx = 0, avgVy = 0;
        for (Boid other : nearbyBoids) {
            V2d vel = other.vel();
            avgVx += vel.x();
            avgVy += vel.y();
        }
        avgVx /= nearbyBoids.size();
        avgVy /= nearbyBoids.size();
        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids) {
        if (nearbyBoids.isEmpty()) return new V2d(0, 0);
        double centerX = 0, centerY = 0;
        for (Boid other : nearbyBoids) {
            P2d pos = other.pos();
            centerX += pos.x();
            centerY += pos.y();
        }
        centerX /= nearbyBoids.size();
        centerY /= nearbyBoids.size();
        return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
    }

    private V2d calculateSeparation(List<Boid> nearbyBoids, double avoidRadius) {
        double dx = 0, dy = 0;
        int count = 0;
        for (Boid other : nearbyBoids) {
            double dist = this.pos.distance(other.pos());
            if (dist < avoidRadius) {
                P2d pos = other.pos();
                dx += this.pos.x() - pos.x();
                dy += this.pos.y() - pos.y();
                count++;
            }
        }
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new V2d(dx, dy).getNormalized();
        }
        return new V2d(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return this.behavior;
    }

    /**
     * Creates Props for a supervisor actor.
     * @return a Props for creating a supervisor actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(BoidActor.class);
    }
}
