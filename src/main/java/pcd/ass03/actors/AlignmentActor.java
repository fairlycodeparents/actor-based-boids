package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public abstract class AlignmentActor extends AbstractActor {

    public static Props props() {
        return Props.create(AlignmentActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SupervisorActor.AlignmentMsg.class, msg -> {
                    // TODO: Logica di allinemento dei boids
                })
                .build();
    }

}
