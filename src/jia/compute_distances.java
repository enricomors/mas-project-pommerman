package jia;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Arrays;
import java.util.Map;

import java.util.logging.Logger;

import jason.environment.grid.Location;

import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.LiteralImpl;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSemantics.DefaultInternalAction;

import example.PommerModel;
import utils.Utility;

/**
 * Internal action that Dijkstra's Algorithm to compute distances from the agent's position 
 * to other objects in the model
 */
public class compute_distances extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("pommerman.mas2j." + compute_distances.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {

        // gets agent name and ID
        String agName = ts.getAgArch().getAgName();
        int agID = Utility.getAgIdBasedOnName(agName);

        // logger.info("Agent name = bomber" + (agID + 1));

        // get the model
        PommerModel model = PommerModel.get();

        // ! using depth of 3 should be equivalent to the 5x5 visual field
        int depth = 4;

        // create list of bombs
        List<Location> bombs = model.getBombsList();

        int[][] dataMatrix = model.getDataMatrix();

        // creates dictionary for distances (indexed by position)
        Hashtable<Location, Integer> dist = new Hashtable<>();

        // ! dictionary for items (indexed by item type), which is maybe useless
        Hashtable<Integer, List<Location>> items = new Hashtable<>();

        // creates dictionary of previous positions for reconstructing paths
        Hashtable<Location, Location> prev = new Hashtable<>();

        // creates a new queue Q
        Queue<Location> q = new LinkedList<>();

        // my_position
        int x = (int)((NumberTerm)terms[0]).solve(); // coord x agente
        int y = (int)((NumberTerm)terms[1]).solve(); // coord y agente
        Location myPosition = new Location(x, y);

        for (int r = Math.max(0, x - depth); r < Math.min(model.GSize, x + depth); r++) {
            for (int c = Math.max(0, y - depth); c < Math.min(model.GSize, y + depth); c++) {
                Location position = new Location(r, c);

                if (
                    outOfRange(myPosition, position, depth) || 
                    model.hasObject(PommerModel.OBSTACLE, position) ||
                    model.hasObject(PommerModel.FIRE, position)
                ) {
                    continue;
                }

                // set previous position to null
                prev.put(position, PommerModel.NULL_LOCATION);
                // gets item at position (r, c)
                int item = dataMatrix[r][c];
                // save item in items dictionary items[item].append(position)
                items.computeIfAbsent(item, k -> new ArrayList<>()).add(position);
                
                if (position.equals(myPosition)) {
                    // puts position in queue Q to evaluate distance
                    q.add(position);
                    // sets distance for position to 0
                    dist.put(position, 0);
                } else { 
                    // set distance to position to +inf
                    dist.put(position, Integer.MAX_VALUE);
                }
            }
        }

        for (Location bomb : bombs) {
            if (bomb.equals(myPosition)) {
                // save bombs location to items dictionary
                items.computeIfAbsent(PommerModel.BOMB, k -> new ArrayList<>()).add(myPosition);
            }
        }

        while (!q.isEmpty()) {
            Location position = q.poll();

            // if position is passable
            if (Utility.positionIsPassable(position, agID)) {
                int posX = position.x;
                int posY = position.y;
                int val = dist.get(position) + 1;

                for (int[] dir : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                    Location newPosition = new Location(posX + dir[0], posY + dir[1]);
                    if (!dist.containsKey(newPosition)) {
                        continue;
                    }
                    if (val < dist.get(newPosition)) {
                        dist.put(newPosition, val);
                        prev.put(newPosition, position);
                        q.add(newPosition);
                    } else if (val == dist.get(newPosition) && Math.random() < 0.5) {
                        dist.put(newPosition, val);
                        prev.put(newPosition, position);
                    }
                }
            }
        }

        /* Create ListTerms for return */

        ListTerm distTerm = new ListTermImpl();
        for (Map.Entry<Location, Integer> entry : dist.entrySet()) {
            ListTerm pair = new ListTermImpl();
            pair.add(new NumberTermImpl(entry.getKey().x));
            pair.add(new NumberTermImpl(entry.getKey().y));
            int distance = entry.getValue();
            if (distance == Integer.MAX_VALUE) {
                pair.add(LiteralImpl.parseLiteral("unreachable"));
            } else {
                pair.add(new NumberTermImpl(distance));
            }
            distTerm.add(pair);
        }

        ListTerm prevTerm = new ListTermImpl();
        for (Map.Entry<Location, Location> entry : prev.entrySet()) {
            ListTerm pair = new ListTermImpl();
            pair.add(new NumberTermImpl(entry.getKey().x));
            pair.add(new NumberTermImpl(entry.getKey().y));
            if (!entry.getValue().equals(PommerModel.NULL_LOCATION)) {
                pair.add(new NumberTermImpl(entry.getValue().x));
                pair.add(new NumberTermImpl(entry.getValue().y));
            } else {
                pair.add(new NumberTermImpl(-1));
                pair.add(new NumberTermImpl(-1));
            }
            prevTerm.add(pair);
        }

        ListTerm itemsTerm = new ListTermImpl();
        for (Map.Entry<Integer, List<Location>> entry : items.entrySet()) {
            ListTerm itemList = new ListTermImpl();
            itemList.add(new NumberTermImpl(entry.getKey()));  // Item type
            ListTerm positions = new ListTermImpl();
            for (Location loc : entry.getValue()) {
                ListTerm locTerm = new ListTermImpl();
                locTerm.add(new NumberTermImpl(loc.x));
                locTerm.add(new NumberTermImpl(loc.y));
                positions.add(locTerm);
            }
            itemList.add(positions);
            itemsTerm.add(itemList);
        }
        
        // Return as a ListTerm containing dist and prev
        ListTerm result = new ListTermImpl();
        result.add(distTerm);
        result.add(prevTerm);
        result.add(itemsTerm);

        return un.unifies(result, terms[2]);
    }

    /**
     * Return True if two Locations are out of range
     * 
     * @param l1 first location
     * @param l2 second location
     * @param range
     */
    private boolean outOfRange(Location l1, Location l2, int range) {
        int x1 = l1.x, y1 = l1.y;
        int x2 = l2.x, y2 = l2.y;
        return Math.abs(y2 - y1) + Math.abs(x2 - x1) > range;
    }
}
