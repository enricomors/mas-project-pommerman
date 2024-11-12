package agent;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.*;

import java.util.logging.Logger;


/**
 * Change the default select event function to select cell(_,_,fire) events first
 */
public class BomberAgent extends Agent {

    private Logger logger = Logger.getLogger("pommerman.mas2j." + BomberAgent.class.getName());

    private Trigger fire = Trigger.parseTrigger("+cell(_,_,fire)");
    private Trigger deadMsg = Trigger.parseTrigger("+im_dead");

    private Trigger checkNewPos = Trigger.parseTrigger("+!check_unsafe_pos(_,_,_,_,_)");

    private Unifier un   = new Unifier();
    
    /**
     * Updates the standard selectEvent method to give priority to the events of
     * getting caught in a bomb explosion (represented by the event +cell(_,_,fire))
     * and receiving an im_dead message from another agent
     */
    @Override
    public Event selectEvent(Queue<Event> events) {
        Iterator<Event> ie = events.iterator();
        while (ie.hasNext()) {
            un.clear();
            Event e = ie.next();
            if (un.unifies(fire, e.getTrigger()) || un.unifies(deadMsg, e.getTrigger())) {
                ie.remove();
                return e;
            }
        }
        return super.selectEvent(events);
    }

    /**
     * Overrides the standard selectIntention method to give priority to
     * the intentions containing the !check_unsafe_pos() goal (Safety comes first!)
     */
    @Override
    public Intention selectIntention(Queue<Intention> intentions) {
        Iterator<Intention> ii = intentions.iterator();
        while (ii.hasNext()) {
            Intention i = ii.next();
            if (i.hasTrigger(checkNewPos, new Unifier())) {
                ii.remove();
                return i;
            }
        }
        return super.selectIntention(intentions);
    }
}
