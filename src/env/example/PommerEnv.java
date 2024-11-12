package example;

// Environment code for project pommerman

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTermImpl;

// import jason.environment.Environment;
import jason.environment.TimeSteppedEnvironment;
import jason.environment.grid.Location;
import utils.Bomb;

/**
 * Implements the Environment for Pommerman - NOSTRO MONDO, ANCHE I PERCEIVE QUI
 */
public class PommerEnv extends TimeSteppedEnvironment {

    static Logger logger = Logger.getLogger("pommerman."+PommerEnv.class.getName());

    public static PommerEnv instance;

    int sleep = 0;
    private long sum = 0;

    PommerModel model;
    PommerView view;

    // enum move
    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    Random rand = new Random();

    // Constants for jason terms and literals
    public static final Term update = Literal.parseLiteral("update_visual_field(X,Y)");

    Term up = Literal.parseLiteral("do(up)");
    Term down = Literal.parseLiteral("do(down)");
    Term right = Literal.parseLiteral("do(right)");
    Term left = Literal.parseLiteral("do(left)");
    Term skip = Literal.parseLiteral("do(skip)");

    Literal dropBomb = Literal.parseLiteral("drop_bomb(X,Y,S)");
    Literal detonateBomb = Literal.parseLiteral("detonate_bomb(X,Y,S)");
    Literal removeFire = Literal.parseLiteral("remove_fire(X,Y,S)");
    Literal pickPowerup = Literal.parseLiteral("pick_powerup(X,Y)");
    Literal removeAgent = Literal.parseLiteral("remove_agent(X,Y)");

    // hash table that stores the positions and the blast strenght of the bombs
    public static Hashtable<Location, Integer> bombTable = new Hashtable<>();

    /********************************** */
    /** FUNZIONI CHIAMATE IN AUTOMATICO */
    /********************************** */
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        logger.warning("Calling init with args = " + args[0]);
        // sets action request to fail if another actions is still pending
        setOverActionsPolicy(OverActionsPolicy.queue);

        setSleep(Integer.parseInt(args[0]));

        initWorld();
    }

    @Override
    protected void updateNumberOfAgents() {
        setNbAgs(model.getNbOfAgs());
    }

    // @Override
    // protected int requiredStepsForAction(String agName, Structure action) {
    //     // determine the delay for the action.
    //     if (action.getFunctor().equals("detonate_bomb")) {
    //         // logger.info("Detonate bomb with delay 5");
    //         return 5;
    //     } else {
    //         return 1;
    //     }
    // }

    /**
     * Execute action in the environment. This is invoked by default whenever
     * an agent tries to execute an action on the environment
     * 
     * @param agentName name of the agent who wants to execute the action
     * @param action term representing the action executed by the agent
     * 
     * @return True if action was executed correctly, false otherwise
     */
    @Override
    public boolean executeAction(String agentName, Structure action) {
        logger.info(agentName+" doing: "+ action); // logging
        boolean result = false; // result 
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }

            // get the agent id based on its name
            int agID = getAgIdBasedOnName(agentName);
            
            // select correct action
            if (action.equals(up)) {
                result = model.move(Move.UP, agID);
            } else if (action.equals(down)) {
                result = model.move(Move.DOWN, agID);
            } else if (action.equals(left)) {
                result = model.move(Move.LEFT, agID);
            } else if (action.equals(right)) {
                result = model.move(Move.RIGHT, agID);
            } else if (action.equals(skip)) {
                result = true;
            /* Drop Bomb */
            } else if (action.getFunctor().equals("drop_bomb")) {
                int bombX = Integer.parseInt(action.getTerm(0).toString());
                int bombY = Integer.parseInt(action.getTerm(1).toString());
                int blastStrength = Integer.parseInt(action.getTerm(2).toString());
                Location bombLoc = new Location(bombX, bombY);
                // add bomb to hashtable
                bombTable.put(bombLoc, blastStrength);
                view.decreaseCountBomb(agID);
                result = model.dropBomb(agID, blastStrength);
            } else if (action.equals(update)) {
                result = true;
            /* Detonate Bomb */
            } else if (action.getFunctor().equals("detonate_bomb")) {
                int bombX = Integer.parseInt(action.getTerm(0).toString());
                int bombY = Integer.parseInt(action.getTerm(1).toString());
                int blastStrength = Integer.parseInt(action.getTerm(2).toString());
                Location bombLoc = new Location(bombX, bombY);
                // remove bomb from hash table
                bombTable.remove(bombLoc);
                view.increaseCountBomb(agID);        
                result = model.detonateBomb(bombLoc, blastStrength);
            /* Remove Fire */
            } else if (action.getFunctor().equals("remove_fire")) {
                int bombX = Integer.parseInt(action.getTerm(0).toString());
                int bombY = Integer.parseInt(action.getTerm(1).toString());
                int blastStrength = Integer.parseInt(action.getTerm(2).toString());
                result = model.removeFire(new Location(bombX, bombY), blastStrength);
            } else if (action.getFunctor().equals("pick_powerup")) {
                int pupX = Integer.parseInt(action.getTerm(0).toString());
                int pupY = Integer.parseInt(action.getTerm(1).toString());
                
                if (action.getTerm(2).toString().equals("inc_bombs")) {
                    logger.info("updating bomb counter");
                    view.increaseCountBomb(agID);
                }
                if (action.getTerm(2).toString().equals("inc_blast")) {
                    logger.info("updating blast strenght");
                    view.increaseRange(agID);
                }

                result = model.removePowerUp(new Location(pupX, pupY));
            } else if (action.getFunctor().equals("remove_agent")) {
                int agX = Integer.parseInt(action.getTerm(0).toString());
                int agY = Integer.parseInt(action.getTerm(1).toString());
                result = model.removeAgent(agX, agY);
            } else {
                logger.info("executing: " + action + ", but not implemented!");
            }
            // action was executed with success
            if (result) {
                if (agID < 5) {
                    updateAgPercept(agID); // update visual field for bomber agents
                }
                return true; // inform action was executed with success
                // TODO update distances
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + agentName, e);
        }
        return false; // inform action was not executed with success
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected void stepStarted(int step) {
        if (view != null) view.setCycle(getStep());
    }

    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        if (step == 0) {
            sum = 0;
            return;
        }

        sum += time;
        logger.info("Cycle "+step+" finished in "+time+" ms, mean is "+(sum/step)+".");
        
        // test end of match
        try {
            if (step >= model.getMaxSteps() && model.getMaxSteps() > 0) {
                String msg = "Finished at the maximal number of steps!";
                logger.info("** "+msg);
                // TODO show msg in gui
                getEnvironmentInfraTier().getRuntimeServices().stopMAS();
            }
            // TODO add test for agent victory
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*******************************************/
    /** FUNZIONI PERCEPT + funzioni ausiliarie */
    /*******************************************/

    /**
     * Gets agent id given its name.
     * 
     * @param agName name of the agent
     */
    public int getAgIdBasedOnName(String agName) {
        int agID = 0; // TODO maybe we should use another value as default
        if (agName.contains("bomber")) {
            agID = (Integer.parseInt(agName.substring(6))) - 1;
        } else if (agName.contains("detonator")) {
            agID = (Integer.parseInt(agName.substring(9))) - 1;
        }
        return agID;
    }

    /**
     * Calls updateAgPercept() for each agent in the environment
     */
    protected void updateAgsPercept() {
        // logger.info("Numero totale di agenti= "+model.getNbOfAgs());
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    /**
     * Initialize environment
     */
    public void initWorld() {
        try {
            model = PommerModel.initWorld();
            super.init(new String[] { "1000" }); // set step timeout new String[] { "1000" }
            
            updateNumberOfAgents();
            clearPercepts();

            int maxSteps = model.getMaxSteps();
            if (maxSteps == 0) maxSteps = 1000;
            addPercept(Literal.parseLiteral("steps(" + maxSteps + ")"));

            updateAgsPercept();

            instance = this; // save the pommer env instance

            view = new PommerView(model);
            view.setEnv(this);
            model.setView(view);

        } catch (Exception e) {
            logger.warning("Error creating world " + e);
        }
    }

    /**
     * Update agents percepts after changing location. Calls the updates on the
     * Visual field
     * 
     * @param agID agent integer ID
     */
    private void updateAgPercept(int agID) {
        // sets agent name
        String agentName = "bomber" + (agID + 1);
        // clear percepts of the agent
        clearPercepts(agentName);
        // gets agent location 
        Location l = model.getAgPos(agID);
        addPercept(agentName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
        /* Updates field of the bomber agent */

        updateVisualField(agID, l.x - 2, l.y - 2);
        updateVisualField(agID, l.x - 2, l.y - 1);
        updateVisualField(agID, l.x - 2, l.y);
        updateVisualField(agID, l.x - 2, l.y + 1);
        updateVisualField(agID, l.x - 2, l.y + 2);

        updateVisualField(agID, l.x - 1, l.y - 2);
        updateVisualField(agID, l.x - 1, l.y + 2);

        updateVisualField(agID, l.x, l.y - 2);
        updateVisualField(agID, l.x, l.y + 2);
        
        updateVisualField(agID, l.x + 1, l.y - 2);
        updateVisualField(agID, l.x + 1, l.y + 2);

        updateVisualField(agID, l.x + 2, l.y - 2);
        updateVisualField(agID, l.x + 2, l.y - 1);
        updateVisualField(agID, l.x + 2, l.y);
        updateVisualField(agID, l.x + 2, l.y + 1);
        updateVisualField(agID, l.x + 2, l.y + 2);

        updateVisualField(agID, l.x - 1, l.y - 1);
        updateVisualField(agID, l.x - 1, l.y);
        updateVisualField(agID, l.x - 1, l.y + 1);
        updateVisualField(agID, l.x, l.y - 1);
        updateVisualField(agID, l.x, l.y);
        updateVisualField(agID, l.x, l.y + 1);
        updateVisualField(agID, l.x + 1, l.y - 1);
        updateVisualField(agID, l.x + 1, l.y);
        updateVisualField(agID, l.x + 1, l.y + 1);
    }

    /**
     * Updates agents visual field. Returns literals indicating the content of
     * Agent's neighbouring cells.
     * 
     * @param agentName Name of the agent + ID
     * @param x x coordinate of the agent agentName
     * @param y y coordinate of the agent agentName
     */
    private void updateVisualField(int agID, int x, int y) {
        // Agent location
        Location agLoc = model.getAgPos(agID);
        // sets agent name
        String agentName = "bomber" + (agID + 1);

        if (model == null || !model.inGrid(x,y)) return;

        /* Checks for other objects in the visual field */
        if (model.hasObject(PommerModel.OBSTACLE, x, y)){
            addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",obstacle)"));
        } else if (model.hasObject(PommerModel.WOODENWALL, x, y)) {
            addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",wooden_wall)"));
        } else {
            if (model.getAgAtPos(x, y) != -1 && model.getAgAtPos(x, y) != agID) {
                addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",enemy)"));
            }  
            if (model.hasObject(PommerModel.BOMB, x, y)) {
                // retrieve bomb at (x,y) from hashtable
                Location bombLoc = new Location(x, y);
                int blastStrenght = bombTable.get(bombLoc);
                // logger.info("blastStrenght = " + blastStrenght);
                addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",bomb(" + blastStrenght + "))"));
            }
            if (model.hasObject(PommerModel.INCREASE_BLAST, x, y)) {
                addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",inc_blast)"));
            }
            if (model.hasObject(PommerModel.INCREASE_BOMBS, x, y)) {
                addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",inc_bombs)"));
            }
            if (model.hasObject(PommerModel.FIRE, x, y)) {
                addPercept(agentName, Literal.parseLiteral("cell(" + x + "," + y + ",fire)"));
            }
        }
    }
}
