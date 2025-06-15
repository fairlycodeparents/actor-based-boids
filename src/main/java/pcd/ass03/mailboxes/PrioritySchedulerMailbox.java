package pcd.ass03.mailboxes;

import java.util.Comparator;
import akka.dispatch.Envelope;
import akka.dispatch.UnboundedStablePriorityMailbox;
import pcd.ass03.actors.SupervisorActor;

/**
 * Akka mailbox with custom priority for Scheduler actor messages.
 * Messages of type {@link SupervisorActor.TickMsg} have lower priority than others.
 */
public class PrioritySchedulerMailbox extends UnboundedStablePriorityMailbox {

    /**
     * Constructs the priority mailbox.
     */
    public PrioritySchedulerMailbox() {
        super(new SchedulerMsgComparator());
    }

    /**
     * Comparator to determine the priority of messages in the mailbox.
     * {@link SupervisorActor.TickMsg} messages are considered lower priority.
     */
    public static class SchedulerMsgComparator implements Comparator<Envelope> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final Envelope o1, final Envelope o2) {
            if (o1.message() instanceof SupervisorActor.TickMsg
                    && !(o2.message() instanceof SupervisorActor.TickMsg)) {
                return 1;
            } else if (!(o1.message() instanceof SupervisorActor.TickMsg)
                    && o2.message() instanceof SupervisorActor.TickMsg) {
                return -1;
            } else {
                return 0;
            }
        }

    }

}