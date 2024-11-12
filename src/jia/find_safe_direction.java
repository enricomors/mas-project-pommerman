package jia;

import jason.NoValueException; 

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSemantics.DefaultInternalAction;

import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Atom;

import jason.environment.grid.Location;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Collections;

import utils.Utility;
import utils.Utility.Direction;
import utils.QueueElement;

/**
 * Internal action to find a safe direction to move if the agent is in danger
 */
public class find_safe_direction extends DefaultInternalAction {
    private static Logger logger = Logger.getLogger("pommerman.mas2j." + find_safe_direction.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {     
            String agName = ts.getAgArch().getAgName();
            int agID = Utility.getAgIdBasedOnName(agName);
            int x = (int)((NumberTerm)terms[0]).solve();
            int y = (int)((NumberTerm)terms[1]).solve();
            Location myPosition = new Location(x, y);
            ListTermImpl directions = (ListTermImpl) terms[2];
            ListTermImpl items = (ListTermImpl) terms[3];
            Map<Direction, Integer> unsafeDirections = parseUnsafeDirections(directions);
            List<Direction> safeDirections = findSafeDirections(unsafeDirections, myPosition, items, agID);
            ListTerm result = new ListTermImpl();
            for (Direction dir : safeDirections) {
                result.add(new Atom(dir.name().toLowerCase()));
            }
            return un.unifies(terms[4], result);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Checks whether moving in a particular direction could leave the agent stuck or not
     * when the agent has dropped a bomb and thus is in an unsafe position
     * 
     * @param nextPosition next location to check
     * @param bombRange current range of the bomb
     * @param agID id of the agent
     * @return true if moving towards nextPosition will get the agent stuck, false otherwise
     */
    private static boolean isStuckDirection(Location nextPosition, int bombRange, int agID) {
        Queue<Location> queue = new LinkedList<>();
        queue.add(nextPosition);
        Set<Location> seen = new HashSet<>();
        seen.add(nextPosition);
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int nextX = nextPosition.x;
        int nextY = nextPosition.y;
        while (!queue.isEmpty()) {
            Location position = queue.poll();
            int dist = Math.abs(position.x - nextPosition.x) + Math.abs(position.y - nextPosition.y);

            int positionX = position.x;
            int positionY = position.y;
            if (nextX != positionX && nextY != positionY) {
                return false;
            }

            if (dist > bombRange) {
                return false; // Found a path beyond bomb range
            }
            for (int i = 0; i < 4; i++) {
                Location newPosition = new Location(position.x + dx[i], position.y + dy[i]);
                if (seen.contains(newPosition)) continue;
                if (Utility.positionIsBomb(newPosition)) continue;
                if (!Utility.positionOnBoard(newPosition)) continue;
                if (!Utility.positionIsPassable(newPosition, agID)) continue;
                
                seen.add(newPosition);
                queue.add(newPosition);
            }
        }
        return true; // No path found beyond bomb range
    }

    /**
     * Find a safe direction to move towards if the agent is in danger
     * 
     * @param unsafeDirections Map<Direction, Integer> of unsafe directions, where the Integer object 
     * represents the maximum bomb blast strenght in a given direction
     * @param myPosition current position of the agent
     * @param items list of items in the agent's visual field
     * @param agID id of the agent
     * @return List of safe directions in which to move. If there are no such directions, it returns SKIP
     */
    private static List<Direction> findSafeDirections(Map<Direction, Integer> unsafeDirections, Location myPosition, ListTermImpl items, int agID) {
        List<Direction> safe = new ArrayList<>();
        
        // all four directions are unsafe (agent's on a bomb)
        if (unsafeDirections.size() == 4) {
            for (Map.Entry<Direction, Integer> entry : unsafeDirections.entrySet()) {
                Direction direction = entry.getKey();
                int bombRange = entry.getValue();
                Location nextPosition = Utility.getNextPosition(myPosition, direction);
                if (!Utility.positionOnBoard(nextPosition) || !Utility.positionIsPassable(nextPosition, agID)) {
                    continue;
                }
                if (!isStuckDirection(nextPosition, bombRange, agID)) {
                    return Collections.singletonList(direction);
                }
            }
            if (safe.isEmpty()) {
                safe.add(Direction.SKIP);
            }
            return safe;
        }
        int x = myPosition.x;
        int y = myPosition.y;
        List<Direction> disallowed = new ArrayList<>();
        for (int[] dir : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            Location position = new Location(x + dir[0], y + dir[1]);
            Direction direction = Utility.getDirection(myPosition, position);
            if (!Utility.positionOnBoard(position)) {
                disallowed.add(direction);
                continue;
            }
            if (unsafeDirections.containsKey(direction)) {
                continue;
            }
            if (Utility.positionIsPassable(position, agID)) {
                safe.add(direction);
            }
        }
        if (safe.isEmpty()) {
            for (Direction direction : unsafeDirections.keySet()) {
                if (!disallowed.contains(direction)) {
                    safe.add(direction);
                }
            }
        }
        if (safe.isEmpty()) {
            safe.add(Direction.SKIP);
        }
        return safe;
    }
    
    /**
     * Parses the ListTermImpl object containing the unsafe directions into a Map<Direction, Integer> object
     * 
     * @param unsafeDirectionsTerm Jason term containing the list of unsafe directions
     * @return the parsed Map<Direction, Integer> object
     */
    private static Map<Direction, Integer> parseUnsafeDirections(ListTermImpl unsafeDirectionsTerm) {
        Map<Direction, Integer> unsafeDirections = new HashMap<>();
        for (Term term : unsafeDirectionsTerm) {
            ListTerm directionPair = (ListTerm) term;
            try {
                String directionStr = ((Atom) directionPair.get(0)).getFunctor();
                int bombRange = (int) ((NumberTerm) directionPair.get(1)).solve();
                Direction direction = Direction.valueOf(directionStr.toUpperCase());
                unsafeDirections.put(direction, bombRange);
            } catch (NoValueException e) {
                e.printStackTrace();
            }
        }
        return unsafeDirections;
    }
}
