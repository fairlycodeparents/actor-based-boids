package pcd.ass03.view;

import akka.actor.ActorRef;
import pcd.ass03.model.Boid;

import java.util.List;

public interface View {
    void setSupervisorActor(ActorRef gridActor);
    void setViewActor(ActorRef  viewActor);
    void render(int FPS, List<Boid> boids);
    void start();
    void updatePauseState(boolean isPaused);
}
