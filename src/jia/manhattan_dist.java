package jia;

import java.util.logging.Logger;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import static java.lang.Math.abs;
import java.lang.String;

import java.util.logging.Logger;

/**
 * Given two locations (X1,Y1) and (X2, Y2), computes the manhattan distance |X1 - X2| + |Y1 - Y2|
 */
public class manhattan_dist extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("pommerman.mas2j." + manhattan_dist.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {

            int x1 = (int)((NumberTerm)terms[0]).solve(); // coord x agente
            int y1 = (int)((NumberTerm)terms[1]).solve(); // coord y agente
            int x2 = (int)((NumberTerm)terms[2]).solve(); // coord x enemy
            int y2 = (int)((NumberTerm)terms[3]).solve(); // coord y enemy

            int manhDist = abs(x1 - x2) + abs(y1 - y2);

            // logger.info("Manhattan distance = " + manhDist);
            
            // convert manhattan distance value to string to return it as an atom
            String distString = String.valueOf(manhDist);  
            return un.unifies(terms[4], new Atom(distString));

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
