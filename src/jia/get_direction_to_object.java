package jia;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

import jason.environment.grid.Location;

import java.util.logging.Logger;

import utils.Utility;
import utils.Utility.Direction;

import java.util.Map;

/**
 * Gets the direction in which the agent should move to reach a given object on the grid
 */
public class get_direction_to_object extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("pommerman.mas2j." + get_direction_to_object.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        //! gets agent name and ID for debugging purposes
        String agName = ts.getAgArch().getAgName();
        int agID = Utility.getAgIdBasedOnName(agName);

        // Parse the input terms
        int x = (int) ((NumberTerm) terms[0]).solve();
        int y = (int) ((NumberTerm) terms[1]).solve();

        ListTerm targetPositionTerm = (ListTerm) terms[2];
        int targetX = (int) ((NumberTerm) targetPositionTerm.get(0)).solve();
        int targetY = (int) ((NumberTerm) targetPositionTerm.get(1)).solve();
        Location targetPosition = new Location(targetX, targetY);

        Map<Location, Integer> dist = Utility.parseDistTerm((ListTerm) terms[3]);
        Map<Location, Location> prev = Utility.parsePrevTerm((ListTerm) terms[4]);

        // Calculate direction towards target object
        Location myPosition = new Location(x, y);
        Direction direction = Utility.getDirectionTowardsPosition(myPosition, targetPosition, prev, agID);

        // Return direction as a term
        if (direction != null) {
            return un.unifies(terms[5], new Atom(direction.name().toLowerCase()));
        } else {
            return un.unifies(terms[5], new Atom("skip")); // Default or error value
        }
    }
}
