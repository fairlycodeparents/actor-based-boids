package pcd.ass03.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.function.Consumer;
import java.util.function.Function;

public class PromiseActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * Message to request the calculation of nearby boids.
     * Contains the list of boids, perception radius, and the boid itself.
     * @param function the function to apply to the input
     * @param input the input for the function
     * @param onComplete the consumer to call with the result of the function
     * @param <X> the type of the input
     * @param <Y> the type of the output
     */
    public record RequestMsg<X, Y>(Function<X, Y> function, X input, Consumer<Y> onComplete) {}

    /**
     * Message to indicate that a request has been completed.
     * Contains the result of the request and a consumer to call with the result.
     * @param onComplete the consumer to call with the result
     * @param result the result of the request
     * @param <Y> the type of the result
     */
    public record CompletedRequestMsg<Y>(Consumer<Y> onComplete, Y result) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMsg.class, this::handleRequest)
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }

    private <X, Y> void handleRequest(RequestMsg<X, Y> msg) {
        Y result = msg.function().apply(msg.input());
        getSender().tell(new CompletedRequestMsg<>(msg.onComplete(), result), getSelf());
    }

    /**
     * Creates Props for a nearby actor.
     * @return a Props for creating a nearby actor, which can then be further configured
     */
    public static Props props() {
        return Props.create(PromiseActor.class);
    }
}

