package pcd.ass03.view;

import akka.actor.ActorRef;

public interface View {
    void setSupervisorActor(ActorRef gridActor);
    void setViewActor(ActorRef  viewActor);
}
