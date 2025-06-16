package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.model.Boid;
import pcd.ass03.model.P2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NearbyActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * This message allows to calculate the neighbors of a boid.
     * @param boid the boid whose neighbors are to be calculated
     * @param boids the list of boids
     */
    public record calculateNeighborsMsg(Boid boid, List<Boid> boids, double separation, double alignment, double cohesion,
                                        double avoidRadius, double perceptionRadius, double maxSpeed, double minX,
                                        double maxX, double minY, double maxY, double width, double height) { }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(calculateNeighborsMsg.class, msg -> {
                    final List<Boid> nearbyBoids = getNearbyBoids(msg.boids(), msg.perceptionRadius, msg.boid);
                    getSender().tell(new BoidActor.CalculatedNeighborsMsg(nearbyBoids, msg.separation, msg.alignment,msg.cohesion,
                            msg.avoidRadius, msg.perceptionRadius, msg.maxSpeed,
                            msg.minX, msg.maxX, msg.minY, msg.maxY, msg.width, msg.height), getSelf());
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    /**
     * Creates Props for a nearby actor.
     * @return a Props for creating a nearby actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(NearbyActor.class);
    }

    private List<Boid> getNearbyBoids(List<Boid> boids, double perceptionRadius, Boid boid) {
        var list = new ArrayList<Boid>();
        for (Boid other : boids) {
            if (!Objects.equals(other, boid)) {
                P2d otherPos = other.pos();
                double distance = boid.pos().distance(otherPos);
                if (distance < perceptionRadius) {
                    list.add(other);
                }
            }
        }
        return list;
    }
}
