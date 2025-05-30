package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import pcd.ass03.view.View;

public class ViewActor extends AbstractActorWithStash {

    private final View view;

    public ViewActor(View view) {
        this.view = view;
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    public static Props props(View view) {
        return Props.create(ViewActor.class, view);
    }

}
