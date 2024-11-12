package example;

import jason.asSyntax.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import example.PommerEnv.Move;

import java.util.logging.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import utils.Bomb;

/**
 * Creates the Grid World Model
 */
public class PommerModel extends GridWorldModel {

    public static final int GSize = 11; // grid size
    
    // define constants for custom objects in the environment
    public static final int WOODENWALL = 8;
    public static final int BOMB = 16;
    public static final int FIRE = 32;
    public static final int INCREASE_BLAST = 64;
    public static final int INCREASE_BOMBS = 128;

    public static final Location NULL_LOCATION = new Location(-1, -1);
    
    // logger for debugging 
    static Logger logger = Logger.getLogger("pommerman."+PommerModel.class.getName());

    static Random rand = new Random();

    // instantiation of exactly one object is needed to coordinate actions across a system -cit wiki
    protected static PommerModel model = null;

    private int maxSteps = 0; // number of steps of the simulation

    PommerView view;

    /**
     * Create a single instance of PommerModel
     * 
     * @param w width of the grid
     * @param h height of the grid
     * @param nbAgs number of agents
     * 
     * @return The instantiated model
     */
    synchronized public static PommerModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new PommerModel(w, h, nbAgs); //PommerModel
        }
        return model;
    }

    /** CONSTRUCTOR
     * Creates a new grid of size GSize x Gsize with 2 agents.
     */
    private PommerModel(int w, int h, int nAgents) {
        super(w, h, nAgents);
    }

    /**
     *  Returns instance of PommerModel
     */
    public static PommerModel get() {
        return model;
    }

    /**
     * Set max steps counter
     */
    public void setMaxSteps(int s) {
        maxSteps = s;
    }

    /**
     * Return max steps counter
     */
    public int getMaxSteps() {
        return maxSteps;
    }

    /* ACTIONS */

    /**
     * Implements the agent movement in the environment.
     * 
     * @param dir direction to move the agent
     * @param ag id of the agent
     * 
     * @return True if action was executed 
     */
    boolean move(Move dir, int ag) throws Exception {
        // logger.info("Bomber "+ (ag + 1) +" move " + dir);
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

    /**
     * Drops a bomb in the map
     */
    boolean dropBomb(int ag, int blastStrenght) {
        //decreaseCountBomb(ag);
        Location l = getAgPos(ag);
        // logger.info("Bomber "+ag+" dropped bomb at location: "+l);
        // logger.info("Blast Strenght: "+blastStrenght);
        // adds bomb to the map
        model.add(PommerModel.BOMB, l.x, l.y);
        return true;
    }

    /**
     * Detonates bomb
     * 
     * @param bombLoc location of bomb in the environment
     * @param blastStrength range of the explosion
     */
    boolean detonateBomb(Location bombLoc, int blastStrength) {
        // logger.info("Bomb at " + bombLoc.x + "," + bombLoc.y + " detonating");
        List<Location> explosion = Bomb.getExplosionRange(blastStrength, bombLoc);
        // draw fire in the explosion location
        for (Location loc : explosion) {
            model.add(PommerModel.FIRE, loc);            
            // If there's a wooden wall at this location, remove it
            if (model.hasObject(PommerModel.WOODENWALL, loc)) {
                model.remove(PommerModel.WOODENWALL, loc);
                // eventually, release a powerup
                releasePowerup(loc);
            }
        }
        model.remove(PommerModel.BOMB, bombLoc);    //rimuovi bommba dalla mappa
        return true;
    }  

    /**
     * Removes fire from the location
     */
    boolean removeFire(Location bombLoc, int blastStrength) {
        // logger.info("Removing flames of bomb at "+ bombLoc.x + "," + bombLoc.y);
        List<Location> explosion = Bomb.getExplosionRange(blastStrength, bombLoc);
        // remove fire in explosion locations
        for (Location loc : explosion) {
            model.remove(PommerModel.FIRE, loc);
        }
        return true;
    }

    /**
     * Remove powerup from location and updates counter in the view
     */
    boolean removePowerUp(Location loc) {
        if (model.hasObject(PommerModel.INCREASE_BLAST, loc)) {
            model.remove(PommerModel.INCREASE_BLAST, loc);
        } else if (model.hasObject(PommerModel.INCREASE_BOMBS, loc)) {
            model.remove(PommerModel.INCREASE_BOMBS, loc);
        }
        return true;
    }

    boolean removeAgent(int x, int y) {
        model.remove(PommerModel.AGENT, x, y);
        return true;
    }

    /**
     * Initialize world model instance with agents and obstaces
     */
    static PommerModel initWorld() throws Exception {
        
        PommerModel model = PommerModel.create(GSize, GSize, 4);    // GRID SIZE, GRID SIZE, NUMBER OF AGENTS

        /* SET MAX NUMBER OF STEPS */
        model.setMaxSteps(1000);
        
        /* INITIALIZE AGENTS */
        model.setAgPos(0, 0, 0);              // top-left corner
        model.setAgPos(1, GSize-1, 0);        // top-right corner
        model.setAgPos(2, 0, GSize-1);        // bottom-left corner
        model.setAgPos(3, GSize-1, GSize-1);  // bottom-right corner

        /* INITIALIZE OBSTACLES */
        model.add(PommerModel.OBSTACLE, 1, 1);
        model.add(PommerModel.OBSTACLE, 1, 3);
        model.add(PommerModel.OBSTACLE, 1, 5);
        model.add(PommerModel.OBSTACLE, 1, 7);
        model.add(PommerModel.OBSTACLE, 1, 9);

        model.add(PommerModel.OBSTACLE, 3, 1);
        model.add(PommerModel.OBSTACLE, 3, 3);
        model.add(PommerModel.OBSTACLE, 3, 5);
        model.add(PommerModel.OBSTACLE, 3, 7);
        model.add(PommerModel.OBSTACLE, 3, 9);

        model.add(PommerModel.OBSTACLE, 5, 1);
        model.add(PommerModel.OBSTACLE, 5, 3);
        model.add(PommerModel.OBSTACLE, 5, 5);
        model.add(PommerModel.OBSTACLE, 5, 7);
        model.add(PommerModel.OBSTACLE, 5, 9);

        model.add(PommerModel.OBSTACLE, 7, 1);
        model.add(PommerModel.OBSTACLE, 7, 3);
        model.add(PommerModel.OBSTACLE, 7, 5);
        model.add(PommerModel.OBSTACLE, 7, 7);
        model.add(PommerModel.OBSTACLE, 7, 9);

        model.add(PommerModel.OBSTACLE, 9, 1);
        model.add(PommerModel.OBSTACLE, 9, 3);
        model.add(PommerModel.OBSTACLE, 9, 5);
        model.add(PommerModel.OBSTACLE, 9, 7);
        model.add(PommerModel.OBSTACLE, 9, 9);

        /* INITIALIZE WOODEN WALLS RANDOOMLY */
        
        int count = 0;
        int xgen;
        int ygen;

        // We choose a predefinite number of wooden walls
        while(count <= 36) {
            xgen = rand.nextInt(11);
            ygen = rand.nextInt(11);
            // checks if generated location is correct
            if(model.checkAgNeighbours(xgen, ygen)){
                // if generated location is free from agent, obstacles
                if(model.isFree(xgen, ygen) && model.isFree(PommerModel.WOODENWALL, xgen, ygen)){
                    // add wooden wall in generated location
                    model.add(PommerModel.WOODENWALL, xgen, ygen);
                    count++;     
                }
            }
        }

        return model;
    }
 

    public static int[][] getDataMatrix() {
        int[][] dataMatrix = new int[model.width][model.height];
        for(int i = 0; i < model.width; i++) {
            for(int j = 0; j < model.height; j++) {
                dataMatrix[i][j] = model.data[i][j];
            }
        }
        return dataMatrix;
    }

    /**
     * Returns a list with all current locations of bombs in the model
     */
    public static List<Location> getBombsList() {
        List<Location> bombList = new ArrayList<>();
        for (int i = 0; i < model.width; i++) {
            for (int j = 0; j < model.height; j++) {
                if (model.hasObject(PommerModel.BOMB, i, j)) {
                    Location bLoc = new Location(i, j);
                    bombList.add(bLoc);
                }
            }
        }
        return bombList;
    }

    /**
     * Checks if position to place a wooden wall is not an agent neighbouring cell.
     * This is to avoid the agent being stuck on its initial position due to wooden
     * blocks being too close 
     */
    private boolean checkAgNeighbours(int xgen, int ygen) {
        // Define the coordinates where wooden walls should not be placed
        int[][] restrictedCoordinates = {
            {0, 1}, {1, 0},
            {0, 2}, {2, 0},
            {9, 0}, {10, 1},
            {8, 0}, {10, 2},
            {0, 9}, {1, 10},
            {0, 8}, {2, 10},
            {9, 10}, {10, 9},
            {8, 10}, {10, 8}
        };
        // Check if (xgen, ygen) is different from all restricted coordinates
        for (int[] coord : restrictedCoordinates) {
            if (xgen == coord[0] && ygen == coord[1]) {
                return false; // (xgen, ygen) is a restricted coordinate
            }
        }
        return true; // (xgen, ygen) is not a restricted coordinate
    }
    
    private void releasePowerup(Location l) {
        model.remove(PommerModel.WOODENWALL, l);

        int pup = rand.nextInt(3);

        switch (pup) {
            case 0:
                // logger.info("No power up at location");
                break;
            case 1:
                // logger.info("+1 Bomb at location ");
                model.add(PommerModel.INCREASE_BOMBS, l);
                break;
            case 2:
                // logger.info("+1 Blast strength at location ");
                model.add(PommerModel.INCREASE_BLAST, l);
                break;
            default:
                logger.info("Something wrong with releasePowerup()");
                break;
        }
    }

}
