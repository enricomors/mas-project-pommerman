package jia;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import example.PommerModel;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import jason.environment.grid.GridWorldModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import example.PommerModel;
import example.PommerEnv;

/**
 * Checks if the agent's current position is unsafe, i.e. there are exploding bombs nearby
 */
public class is_position_unsafe extends DefaultInternalAction {
    
    private Logger logger = Logger.getLogger("pommerman.mas2j." + is_position_unsafe.class.getName());

    // Bomb table - key: (x_cord, y_cord) - value: bomb_range
    Hashtable<Location, Integer> bombTableAg = new Hashtable<>();

    // Create a set to store all explosion locations
    Set<Location> allExplosions = new HashSet<>();

    /**
     * Overrides default method to execute action.
     * 
     * @param ts
     * @param un Unifier object to unify return variables.
     * @param terms list of terms in the AgentSpeak predicate.
     * 
     * @return True if action executed correctly, False otherwise.
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            // Default action (stay still)
            String direction = "skip";

            // Get the model
            PommerModel model = PommerModel.get();

            int x = (int) ((NumberTerm) terms[0]).solve(); // x coordinate of the agent
            int y = (int) ((NumberTerm) terms[1]).solve(); // y coordinate of the agent

            ListTerm items = (ListTerm) terms[2];
            ListTerm dist = (ListTerm) terms[3];

            enum Action {
                Right, Left, Up, Down
            }

            Map<Action, Integer> ret = new HashMap<>();
            for (Action action : Action.values()) {
                ret.put(action, 0);
            }

            boolean bombsFound = false;

            // Iterate through items to find bombs
            for (Term item : items) {
                ListTermImpl itemEntry = (ListTermImpl) item;
                int itemType = (int) ((NumberTerm) itemEntry.get(0)).solve();

                // Only process bomb items
                if (itemType == PommerModel.BOMB) { // Assuming PommerModel.BOMB is the code for bombs
                    bombsFound = true;
                    ListTermImpl bombPositions = (ListTermImpl) itemEntry.get(1);
                    for (Term bombPosition : bombPositions) {
                        ListTermImpl position = (ListTermImpl) bombPosition;
                        int bombPosX = (int) ((NumberTerm) position.get(0)).solve();
                        int bombPosY = (int) ((NumberTerm) position.get(1)).solve();

                        // Retrieve blast strength from the bombTable
                        Integer blastStrength = PommerEnv.bombTable.get(new Location(bombPosX, bombPosY));
                        if (blastStrength == null) continue;

                        // Find the distance for the current bomb position
                        int distance = Integer.MAX_VALUE;
                        for (Term distTerm : dist) {
                            ListTermImpl distEntry = (ListTermImpl) distTerm;
                            int distX = (int) ((NumberTerm) distEntry.get(0)).solve();
                            int distY = (int) ((NumberTerm) distEntry.get(1)).solve();
                            Term distanceTerm = distEntry.get(2);

                            if (distX == bombPosX && distY == bombPosY && distanceTerm instanceof NumberTerm) {
                                distance = (int) ((NumberTerm) distanceTerm).solve();
                                break;
                            }
                        }

                        if (distance == Integer.MAX_VALUE || distance > blastStrength) continue;

                        if (x == bombPosX && y == bombPosY) {
                            // We are on a bomb. All directions are in range of bomb.
                            for (Action action : Action.values()) {
                                ret.put(action, Math.max(ret.get(action), blastStrength));
                            }
                        } else if (x == bombPosX) {
                            if (y < bombPosY) {
                                // Bomb is down.
                                ret.put(Action.Down, Math.max(ret.get(Action.Down), blastStrength));
                            } else {
                                // Bomb is up.
                                ret.put(Action.Up, Math.max(ret.get(Action.Up), blastStrength));
                            }
                        } else if (y == bombPosY) {
                            if (x < bombPosX) {
                                // Bomb is right.
                                ret.put(Action.Right, Math.max(ret.get(Action.Right), blastStrength));
                            } else {
                                // Bomb is left.
                                ret.put(Action.Left, Math.max(ret.get(Action.Left), blastStrength));
                            }
                        }
                    }
                }
            }

            ListTerm result = new ListTermImpl();
            if (bombsFound) {
                // Return only the directions with blast strength > 0
                for (Map.Entry<Action, Integer> entry : ret.entrySet()) {
                    if (entry.getValue() > 0) {
                        ListTermImpl entryList = new ListTermImpl();
                        entryList.add(new Atom(entry.getKey().toString()));
                        entryList.add(new NumberTermImpl(entry.getValue()));
                        result.add(entryList);
                    }
                }
            }

            return un.unifies(terms[4], result);

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}