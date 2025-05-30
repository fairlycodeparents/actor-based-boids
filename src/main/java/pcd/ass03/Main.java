package pcd.ass03;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import pcd.ass03.actors.SupervisorActor;
import pcd.ass03.actors.ViewActor;
import pcd.ass03.view.View;
import pcd.ass03.view.ViewImpl;

import java.io.File;

public class Main {
    public static void main(final String[] args) {
        final View view = new ViewImpl();
        final Config config = ConfigFactory.parseFile(new File("src/main/java/pcd/ass03/application.conf"));
        final ActorSystem system = ActorSystem.create("BoidsSimulationSystem", config);
        final ActorRef viewActor = system.actorOf(ViewActor.props(view), "view");
        final ActorRef gridActor = system.actorOf(SupervisorActor.props(), "supervisor");

        view.setSupervisorActor(gridActor);
        view.setViewActor(viewActor);
    }
}
