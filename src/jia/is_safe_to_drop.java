package jia;

import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;

import java.util.logging.Logger;

import jason.environment.grid.Location;

import jason.asSyntax.NumberTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Term;
import jason.NoValueException;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSemantics.DefaultInternalAction;

import example.PommerModel;

/**
 * Internal action that checks if it is safe to drop a bomb at a given location.
 */
public class is_safe_to_drop extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("pommerman.mas2j." + is_safe_to_drop.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {

        // Get the position (X, Y) where the bomb would be dropped
        int x = (int)((NumberTerm)terms[0]).solve();
        int y = (int)((NumberTerm)terms[1]).solve();
        Location myPosition = new Location(x, y);

        // Get the bomb blast strength
        int blastStrength = (int)((NumberTerm)terms[2]).solve();

        // Get the distances dictionary
        ListTerm distTerm = (ListTerm) terms[3];
        Map<Location, Integer> dist = parseDistTerm(distTerm);

        // Get the items dictionary
        ListTerm itemsTerm = (ListTerm) terms[4];
        Map<Integer, List<Location>> items = parseItemsTerm(itemsTerm);

        // Check if the agent will be stuck after dropping the bomb
        boolean safeToDrop = isSafeToDrop(myPosition, blastStrength, dist, items);

        // Return the result
        return safeToDrop;
    }

    /**
     * Checks whether it is safe for an agent to drop a bomb at the current position
     * 
     * @param myPosition current position of the agent
     * @param blastStrength current bomb blast strenght
     * @param dist Map<Location, Integer> of distances from the position of the agent to ther locations in its visual field
     * @param items Map<Integer, List<Location>> of objects in the agent's visual field (represented by an integer) and a list
     * of the locations of each type of object.
     * @return true if is safe for the agent to drop a bomb in the current position, false otherwise
     */
    private boolean isSafeToDrop(Location myPosition, int blastStrength, Map<Location, Integer> dist, Map<Integer, List<Location>> items) {
        int x = myPosition.x;
        int y = myPosition.y;

        boolean isSafe = false;

        // Loops through passage (CLEAN) locations
        for (Location position : items.get(PommerModel.CLEAN)) {
            int distance = dist.getOrDefault(position, Integer.MAX_VALUE);

            if (distance == Integer.MAX_VALUE) {
                continue;
            }

            // We can reach a passage that's outside of the bomb strength.
            if (distance > blastStrength) {
                isSafe = true;
            }

            // We can reach a passage that's outside of the bomb scope.
            int positionX = position.x;
            int positionY = position.y;
            if (positionX != x && positionY != y) {
                isSafe = true;
            }
        }

        return isSafe;
    }

    private Map<Location, Integer> parseDistTerm(ListTerm distTerm) {
        Map<Location, Integer> dist = new Hashtable<>();
        for (Term term : distTerm) {
            ListTerm pair = (ListTerm) term;
            try {
                int x = (int) ((NumberTerm) pair.get(0)).solve();
                int y = (int) ((NumberTerm) pair.get(1)).solve();
                Term distTermValue = pair.get(2);
                int distance = distTermValue instanceof NumberTerm ? (int) ((NumberTerm) distTermValue).solve() : Integer.MAX_VALUE;
                dist.put(new Location(x, y), distance);
            } catch (NoValueException e) {
                e.printStackTrace();
            }
        }
        return dist;
    }
    
    private Map<Integer, List<Location>> parseItemsTerm(ListTerm itemsTerm) {
        Map<Integer, List<Location>> items = new Hashtable<>();
        for (Term term : itemsTerm) {
            ListTerm itemList = (ListTerm) term;
            try {
                int itemType = (int) ((NumberTerm) itemList.get(0)).solve();
                List<Location> locations = new ArrayList<>();
                ListTerm positions = (ListTerm) itemList.get(1);
                for (Term posTerm : positions) {
                    ListTerm locTerm = (ListTerm) posTerm;
                    int x = (int) ((NumberTerm) locTerm.get(0)).solve();
                    int y = (int) ((NumberTerm) locTerm.get(1)).solve();
                    locations.add(new Location(x, y));
                }
                items.put(itemType, locations);
            } catch (NoValueException e) {
                e.printStackTrace(); 
            }
        }
        return items;
    }    

}