package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;

import jason.asSyntax.Term;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import jason.environment.grid.GridWorldModel;
import example.PommerModel;

//import env.example.PommerModel;

/**
 * Class which implements the agents' internal action random_direction, which
 * chooses a random direction to go after observing the agent visual field.
 * Used by the !move_randomly goal of the bomber agents
 */
public class get_random_direction extends DefaultInternalAction {
    
    private Logger logger = Logger.getLogger("pommerman.mas2j." + get_random_direction.class.getName());

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
            // azione di deafult (resta fermo)
            String direction = "skip"; 
            // get the model
            PommerModel model = PommerModel.get();

            // risolvi i terms che sarebbero -> coord agente
            int x = (int)((NumberTerm)terms[0]).solve(); // coord x agente
            int y = (int)((NumberTerm)terms[1]).solve(); // coord y agente

            // Define the possible moves
            int[][] moves = { {x+1, y}, {x, y+1}, {x, y-1}, {x-1, y} };

            // Create a list to store the valid moves
            List<int[]> validMoves = new ArrayList<int[]>();

            // Check each move to see if it's valid - if the cell is free from agent, obsacles and wooden walls
            // TODO we also have to check that there's not a bomb, or that the cell is not in the range of acton of the bomb
            for (int[] move : moves) {
                if (
                    model.isFree(move[0], move[1]) && 
                    model.inGrid(move[0], move[1]) && 
                    model.isFree(PommerModel.WOODENWALL, move[0], move[1])
                ) {
                    validMoves.add(move);
                }
            }
            
            /* DEBUG: print validMoves */
            // for (int[] move : validMoves) {
            //     String agentID = ts.getUserAgArch().getAgName();
            //     logger.info("L'agente "+agentID+" ha come mossa valida: "+ Arrays.toString(move));
            // }
            

            // Choose a random valid move
            if (!validMoves.isEmpty()) {
                int[] chosenMove = validMoves.get(new Random().nextInt(validMoves.size()));
                // logger.info("Mosssa scelta = " + chosenMove[0] + " - " + chosenMove[1]);
                // Convert the chosen move to a direction
                if (chosenMove[0] > x) {
                    direction = "right";
                } else if (chosenMove[0] < x) {
                    direction = "left";
                } else if (chosenMove[1] > y) {
                    direction = "down";
                } else {
                    direction = "up";
                }
            } else {
                //TODO azione skip se non abbiamo caselle valide
                logger.info("No valid moves, do("+direction+")");
            }

            // Unify the direction with the output variable if no valid direction is found, returns "skip"
            String agentID = ts.getUserAgArch().getAgName();
            // logger.info("L'Agente "+agentID+" sta andando "+direction);
            return un.unifies(terms[2], new Atom(direction));

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}