package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class SupervisorActor extends AbstractActorWithStash {

    public SupervisorActor() {

    }

    @Override
    public Receive createReceive() {
        return null;
    }

    public static Props props() {
        return Props.create(SupervisorActor.class);
    }

}
