package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;
import pcd.ass03.model.V2d;

import java.util.List;
/**
 * This actor represents a boid in the boids simulation.
 * It processes messages to update its state based on the boids behavior.
 */
public class BoidActor extends AbstractActor {

    private final LoggingAdapter log;
    private V2d vel;
    private P2d pos;
    private final ActorRef nearbyActor;

    /**
     * Message to request an update of the boid's state.
     * Contains the list of nearby boids and parameters for the boid's behavior.
     * @param boids the list of boids in the simulation
     * @param separation the weight for separation behavior
     * @param alignment the weight for alignment behavior
     * @param cohesion the weight for cohesion behavior
     * @param maxSpeed the maximum speed of the boid
     * @param minX the minimum x-coordinate for the boid's position
     * @param maxX the maximum x-coordinate for the boid's position
     * @param minY the minimum y-coordinate for the boid's position
     * @param maxY the maximum y-coordinate for the boid's position
     * @param width the width of the simulation area
     * @param height the height of the simulation area
     */
    public record UpdateRequestMsg(List<Boid> boids, double separation, double alignment, double cohesion,
                                   double avoidRadius, double perceptionRadius, double maxSpeed, double minX,
                                   double maxX, double minY, double maxY, double width, double height) { }


    /**
     * This message is sent to signal that neighbors has been calculated.
     */
    public record CalculatedNeighborsMsg(List<Boid> boids, double separation, double alignment, double cohesion,
                                         double avoidRadius, double perceptionRadius, double maxSpeed, double minX,
                                         double maxX, double minY, double maxY, double width, double height) { }

    /**
     * Constructor for the BoidActor, initializes the actor with a given velocity and position.
     * @param vel the initial velocity of the boid
     * @param pos the initial position of the boid
     */
    public BoidActor(V2d vel, P2d pos) {
        this.vel = vel;
        this.pos = pos;
        this.log = Logging.getLogger(getContext().getSystem(), this);
        this.nearbyActor = getContext().actorOf(NearbyActor.props(), "nearby-"+getSelf().path().name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UpdateRequestMsg.class, msg -> {
                    Boid current = new Boid(this.pos, this.vel);
                    nearbyActor.tell(new NearbyActor.calculateNeighborsMsg(current, msg.boids, msg.separation, msg.alignment, msg.cohesion,
                            msg.avoidRadius, msg.perceptionRadius, msg.maxSpeed,
                            msg.minX, msg.maxX, msg.minY, msg.maxY, msg.width, msg.height), getSelf());
                }).match(CalculatedNeighborsMsg.class, msg -> {
                    List<Boid> nearbyBoids = msg.boids;
                    update(nearbyBoids, msg.separation, msg.alignment, msg.cohesion, msg.avoidRadius, msg.maxSpeed,
                            msg.minX, msg.maxX, msg.minY, msg.maxY, msg.width, msg.height);
                    getSender().tell(new SupervisorActor.UpdatedBoidMsg(new Boid(this.pos, this.vel)), getSelf());
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    /**
     * Creates Props for a boid actor.
     * @param vel the initial velocity of the boid
     * @param pos the initial position of the boid
     * @return a Props for creating a boid actor, which can then be further configured
     */
    public static Props props(V2d vel, P2d pos) {
        return Props.create(BoidActor.class, vel, pos);
    }

    private void update(List<Boid> nearbyBoids, double separationWeight, double alignmentWeight, double cohesionWeight,
                        double avoidRadius, double maxSpeed, double minX, double maxX, double minY, double maxY,
                        double width, double height) {

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
}
