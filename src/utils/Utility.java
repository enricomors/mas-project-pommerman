package utils;

import jason.environment.grid.Location;

import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

import jason.NoValueException;

import example.PommerModel;

/**
 * Utility class containing useful functions for the project, such as the boolean methods for checking
 * the positions on the map and the enum to represent Directions.
*/
public class Utility {

    // get the model
    static PommerModel model = PommerModel.get();

    private static Logger logger = Logger.getLogger("pommerman.mas2j." + Utility.class.getName());

    /** Enum to represent directions in which the agent can move, plus SKIP */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, SKIP
    };

    public static boolean positionIsPowerup(int x, int y) {
        return (model.hasObject(PommerModel.INCREASE_BLAST, x, y) ||
                model.hasObject(PommerModel.INCREASE_BOMBS, x, y));
    }

    public static boolean positionIsPassage(int x, int y) {
        return (model.isFree(PommerModel.WOODENWALL, x, y) &&
                model.isFree(PommerModel.OBSTACLE, x, y) &&
                model.isFree(PommerModel.BOMB, x, y));
    }

    public static boolean positionIsEnemy(int x, int y, int agID) {
        return (model.getAgAtPos(x, y) != -1 && 
                model.getAgAtPos(x, y) != agID);
    }

    public static boolean positionIsAgent(int x, int y, int agID) {
        return model.getAgAtPos(x, y) == agID;
    }

    public static boolean positionIsBomb(Location position) {
        return model.hasObject(PommerModel.BOMB, position);
    }

    public static boolean positionIsPassable(Location position, int agID) {
        int x = position.x;
        int y = position.y;

        return ((positionIsAgent(x, y, agID) ||
                positionIsPowerup(x, y) ||
                positionIsPassage(x, y)) && 
                (!positionIsEnemy(x, y, agID)));
    }

    public static boolean positionOnBoard(Location position) {
        return model.inGrid(position);
    }

    public static Location getNextPosition(Location myPosition, Direction direction) {
        int x = myPosition.x;
        int y = myPosition.y;

        Location nextPosition = new Location(x, y);

        if (direction.equals(Direction.RIGHT)) {
            nextPosition = new Location(x + 1, y);
        } else if (direction.equals(Direction.LEFT)) {
            nextPosition = new Location(x - 1, y);
        } else if (direction.equals(Direction.DOWN)) {
            nextPosition = new Location(x, y + 1);
        } else if (direction.equals(Direction.UP)) {
            nextPosition = new Location(x, y - 1);
        } else if (direction.equals(Direction.SKIP)) {
            nextPosition = new Location(x, y);
        }
        return nextPosition; 
    }

    public static Direction getDirection(Location position, Location nextPosition) {
        int x = position.x;
        int y = position.y;

        int nextX = nextPosition.x;
        int nextY = nextPosition.y;

        Direction direction = Direction.SKIP;

        if (x == nextX) {
            if (y < nextY) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.UP;
            }
        } else if (y == nextY) {
            if (x < nextX) {
                direction = Direction.RIGHT;
            } else {
                direction = Direction.LEFT;
            }
        }

        return direction;
    }

    public static Direction getDirectionTowardsPosition(Location myPosition, Location targetPosition, Map<Location, Location> prev, int agID) {
        if (targetPosition == null) {
            return null;
        }

        Location nextPosition = targetPosition;
        while (!prev.get(nextPosition).equals(myPosition)) {
            nextPosition = prev.get(nextPosition);
        }
        return getDirection(myPosition, nextPosition);
    }

    /**
     * Gets the nearest position of the given object type within a given radius from the agent
     * 
     * @param dist Map<Locaion, Integer> containing the Integer distance between the agent and a given Location
     * @param objs List of locations of objects
     * @param radius the radius from the agent
     * @return Location of the nearest object
     */
    public static Location nearestPosition(Map<Location, Integer> dist, List<Location> objs, int radius) {
        Location nearest = null;
        int distTo = Integer.MAX_VALUE;

        for (Location obj : objs) {
            if (dist.containsKey(obj)) {
                int d = dist.get(obj);
                if (d <= radius && d <= distTo) {
                    nearest = obj;
                    distTo = d;
                }
            }
        }

        return nearest;
    }

    public static Map<Location, Integer> parseDistTerm(ListTerm distTerm) {
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

    public static Map<Location, Location> parsePrevTerm(ListTerm prevTerm) {
        Map<Location, Location> prev = new Hashtable<>();
        for (Term term : prevTerm) {
            ListTerm pair = (ListTerm) term;
            try {
                int x = (int) ((NumberTerm) pair.get(0)).solve();
                int y = (int) ((NumberTerm) pair.get(1)).solve();
                Location key = new Location(x, y);

                int xPrev = (int) ((NumberTerm) pair.get(2)).solve();
                int yPrev = (int) ((NumberTerm) pair.get(3)).solve();
                Location value = new Location(xPrev, yPrev);
                
                if (!value.equals(PommerModel.NULL_LOCATION)) {
                    prev.put(key, value);
                } else {
                    // no previous location
                    prev.put(key, PommerModel.NULL_LOCATION);
                }
            } catch (NoValueException e) {
                e.printStackTrace();
            }
        }
        return prev;
    }
    
    public static Map<Integer, List<Location>> parseItemsTerm(ListTerm itemsTerm) {
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

    /**
     * Gets agent id given its name.
     * 
     * @param agName name of the agent
     * @return the int id of the agent
     */
    public static int getAgIdBasedOnName(String agName) {
        int agID = 0; // TODO maybe we should use another value as default
        if (agName.contains("bomber")) {
            agID = (Integer.parseInt(agName.substring(6))) - 1;
        } else if (agName.contains("detonator")) {
            agID = (Integer.parseInt(agName.substring(9))) - 1;
        }
        return agID;
    }
    
}
