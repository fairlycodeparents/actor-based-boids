package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public abstract class CohesionActor extends AbstractActor {

    public static Props props() {
        return Props.create(CohesionActor.class);
    }
/*
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SupervisorActor.CohesionMsg.class, msg -> {
                    // TODO: Logica di coesione dei boids
                })
                .build();
    }
*/
}
