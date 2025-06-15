package pcd.ass03.actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * This actor represents a timer that can notify after a specified duration.
 * It is used to manage time-based events in the simulation.
 */
public class TimerActor extends AbstractActorWithStash {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * This class represents a tick in the simulation. It signals that the simulation should update its state.
     */
    public static class TickMsg { }

    /**
     * Message to request a notification after a certain number of milliseconds.
     * @param delayMillis the number of milliseconds after which to send the notification
     * @param requestTimestamp the timestamp when the request was made
     */
    public record RequestNotificationMsg(long delayMillis, long requestTimestamp) { }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestNotificationMsg.class, msg -> {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - msg.requestTimestamp;
                    long remainingTime = msg.delayMillis - elapsedTime;

                    if (remainingTime <= 0) {
                        getSender().tell(new TickMsg(), getSelf());
                        log.info("Notification sent that time has elapsed");
                    } else {
                        getSelf().tell(new RequestNotificationMsg(remainingTime, currentTime), getSender());
                    }
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    /**
     * Creates Props for a timer actor.
     * @return a Props for creating timer actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(TimerActor.class);
    }
}
