package pcd.ass03.model;

/**
 * Represents a boid in the simulation.
 * @param pos the position of the boid
 * @param vel the velocity of the boid
 */
public record Boid(P2d pos, V2d vel) { }