package pcd.ass03.model;

/**
 * Represents the parameters for a boids simulation.
 * This record holds all necessary parameters to configure the behavior of boids in the simulation.
 * @param separation the weight for separation behavior
 * @param alignment the weight for alignment behavior
 * @param cohesion the weight for cohesion behavior
 * @param avoidRadius the radius within which boids avoid each other
 * @param perceptionRadius the radius within which boids perceive other boids
 * @param maxSpeed the maximum speed of a boid
 * @param minX the minimum x-coordinate for the simulation area
 * @param maxX the maximum x-coordinate for the simulation area
 * @param minY the minimum y-coordinate for the simulation area
 * @param maxY the maximum y-coordinate for the simulation area
 * @param width the width of the simulation area
 * @param height the height of the simulation area
 */
public record SimulationParams(
        double separation,
        double alignment,
        double cohesion,
        double avoidRadius,
        double perceptionRadius,
        double maxSpeed,
        double minX,
        double maxX,
        double minY,
        double maxY,
        double width,
        double height
) { }