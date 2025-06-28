package pcd.ass03;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import pcd.ass03.actors.SupervisorActor;
import pcd.ass03.actors.TimerActor;
import pcd.ass03.actors.ViewActor;
import pcd.ass03.view.View;
import pcd.ass03.view.ViewImpl;

/**
 * Entry point for the Boids simulation application.
 */
public class Main {
    public static final double WIDTH = 1000;
    public static final double HEIGHT = 1000;

    public static void main(final String[] args) {
        final View view = new ViewImpl();
        final ActorSystem system = ActorSystem.create("BoidsSimulationSystem");
        final ActorRef viewActor = system.actorOf(ViewActor.props(view), "view");
        final ActorRef timerActor = system.actorOf(TimerActor.props(), "timer");
        final ActorRef supervisorActor = system.actorOf(SupervisorActor.props(viewActor, timerActor), "supervisor");

        view.setSupervisorActor(supervisorActor);
        view.start();
    }
}
