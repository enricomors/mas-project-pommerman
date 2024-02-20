package example;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

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

    void moveAgent() throws Exception {
        // get location of agent alice
        Location alice = getAgPos(0);
        // move horizontally on current row
        alice.x++;
        // if alice reaches end of row
        if (alice.x == getWidth()) {
            // move to the initial cell of the next row
            alice.x = 0;
            alice.y++;
        }
        // finished searching the whole grid
        if (alice.y == getHeight()) {
            return;
        }
        setAgPos(0, alice);
    }
    
}