package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

/**
 * Returns the numeric id of the current agent. Used in the interaction between the
 * Bomber agent and the Detonator agent, to find the correct agent to communicate with.
 */
public class my_id extends DefaultInternalAction {

    String agID;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        // get agent name
        String agName = ts.getAgArch().getAgName();

        if (agName.contains("bomber")) {
            agID = agName.substring(6);
        }
        if (agName.contains("detonator")) {
            agID = agName.substring(9);
        }
        return un.unifies(terms[0], new Atom(agID));
    }
    
}
