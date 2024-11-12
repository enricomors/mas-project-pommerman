package jia;

import jason.environment.grid.Location;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import utils.Utility;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Checks whether a given object is within a given radius with respect to the agent.
 * Arguments are X, Y, Dist, <Object>, <Radius>, <Return List>
 */
public class is_object_in_radius extends DefaultInternalAction {

    private static Logger logger = Logger.getLogger("pommerman.mas2j." + is_object_in_radius.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        // Parse the input terms
        int x = (int) ((NumberTerm) terms[0]).solve();
        int y = (int) ((NumberTerm) terms[1]).solve();

        // Get the distances dictionary
        ListTerm distTerm = (ListTerm) terms[2];
        Map<Location, Integer> dist = Utility.parseDistTerm(distTerm);

        // List<Location> objs = (List<Location>) ((ObjectTerm) terms[3]).getObject();
        ListTerm objTerm = (ListTerm) terms[3];
        List<Location> objs = new ArrayList<>();

        for (Term term : objTerm) {
            // Assuming each term is of the form cell(X,Y,_)      
            Structure cellTerm = (Structure) term;
            int objX = (int) ((NumberTerm) cellTerm.getTerm(0)).solve();
            int objY = (int) ((NumberTerm) cellTerm.getTerm(1)).solve();
            // You can check the third term if necessary
            objs.add(new Location(objX, objY));
        }
        
        int radius = (int) ((NumberTerm) terms[4]).solve();
        
        // Calculate nearest object within radius
        Location nearestObject = Utility.nearestPosition(dist, objs, radius);

        // Return nearest object location as a list [X, Y]
        if (nearestObject != null) {
            ListTerm result = new ListTermImpl();
            result.add(new NumberTermImpl(nearestObject.x));
            result.add(new NumberTermImpl(nearestObject.y));
            return un.unifies(terms[5], result);
        } else {
            return un.unifies(terms[5],  new ListTermImpl()); // Return empty list if no object found
        }
    }
}