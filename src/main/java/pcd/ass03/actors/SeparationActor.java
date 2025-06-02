package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public abstract class SeparationActor extends AbstractActor {

    public static Props props() {
        return Props.create(SeparationActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SupervisorActor.SeparationMsg.class, msg -> {
                    // TODO: Logica di separazione dei boids
                })
                .build();
    }

}
