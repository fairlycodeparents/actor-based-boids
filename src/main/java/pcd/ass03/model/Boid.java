package pcd.ass03.model;

import java.util.List;

/**
 * Class representing a boid in the simulation.
 * This class encapsulates the position and velocity of a boid.
 */
public class Boid {

    private P2d pos;
    private V2d vel;

    /**
     * Constructor for the Boid class.
     * @param pos the position of the boid
     * @param vel the velocity of the boid
     */
    public Boid(P2d pos, V2d vel) {
        this.pos = pos;
        this.vel = vel;
    }

    /**
     * Gets the position of the boid.
     * @return the position of the boid
     */
    public P2d getPos() {
        return this.pos;
    }

    /**
     * Gets the velocity of the boid.
     * @return the velocity of the boid
     */
    public V2d getVel() {
        return this.vel;
    }

    /**
     * Updates the boid's position and velocity based on nearby boids.
     * This method calculates the new velocity based on alignment, cohesion, and separation,
     * and then updates the position accordingly.
     *
     * @param nearbyBoids the list of nearby boids
     * @param alignmentWeight weight for alignment behavior
     * @param cohesionWeight weight for cohesion behavior
     * @param separationWeight weight for separation behavior
     * @param avoidRadius radius within which to avoid other boids
     * @param maxSpeed maximum speed of the boid
     * @param minX minimum x boundary for wrapping
     * @param maxX maximum x boundary for wrapping
     * @param minY minimum y boundary for wrapping
     * @param maxY maximum y boundary for wrapping
     * @param width width of the simulation area
     * @param height height of the simulation area
     */
    public void update(List<Boid> nearbyBoids, double alignmentWeight, double cohesionWeight,
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
            avgVx += other.vel.x();
            avgVy += other.vel.y();
        }
        avgVx /= nearbyBoids.size();
        avgVy /= nearbyBoids.size();
        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids) {
        if (nearbyBoids.isEmpty()) return new V2d(0, 0);
        double centerX = 0, centerY = 0;
        for (Boid other : nearbyBoids) {
            centerX += other.pos.x();
            centerY += other.pos.y();
        }
        centerX /= nearbyBoids.size();
        centerY /= nearbyBoids.size();
        return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
    }

    private V2d calculateSeparation(List<Boid> nearbyBoids, double avoidRadius) {
        double dx = 0, dy = 0;
        int count = 0;
        for (Boid other : nearbyBoids) {
            double dist = pos.distance(other.pos);
            if (dist < avoidRadius) {
                dx += pos.x() - other.pos.x();
                dy += pos.y() - other.pos.y();
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
