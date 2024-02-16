package example;

import jason.environment.grid.GridWorldModel;

/**
 * Creates the Grid World Model
 */
public class PommerModel extends GridWorldModel {

    public static final int GSize = 11; // grid size

    /** creates a new grid of size GSize x Gsize with 2 agents */
    public PommerModel() {
        super(GSize, GSize, 4);

        /** Initial Location of agents */
        try {
            setAgPos(0, 0, 0);              // top-left corner
            setAgPos(1, GSize-1, 0);        // top-right corner
            setAgPos(2, 0, GSize-1);        // bottom-left corner
            setAgPos(3, GSize-1, GSize-1);  // bottom-right corner
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
