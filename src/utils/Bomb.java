package utils;

import jason.environment.grid.Location;

import java.util.List;
import java.util.ArrayList;

import example.PommerModel;

/**
 * This class contains utility functions to deal with bombs.
 */
public class Bomb {

    // get the model
    static PommerModel model = PommerModel.get();

    /**
     * Computes the range of the bomb explosion
     * 
     * @param blast blast range of the explosion
     * @param bombLocation location of the bomb
     */
    public static List<Location> getExplosionRange(int blast, Location bombLocation) {
        int x = bombLocation.x;
        int y = bombLocation.y;
    
        List<Location> explosionRange = new ArrayList<>();
    
        // Calculate cells to the left of the bomb
        for (int i = 1; i <= blast; i++) {
            if (model.inGrid(x - i, y)) {
                Location loc = new Location(x - i, y);
                if (model.isFree(PommerModel.OBSTACLE, loc)) {
                    explosionRange.add(loc);
                } else {
                    break; // Stop if there's an obstacle
                }
            }
        }
    
        // Calculate cells to the right of the bomb
        for (int i = 1; i <= blast; i++) {
            if (model.inGrid(x + i, y)) {
                Location loc = new Location(x + i, y);
                if (model.isFree(PommerModel.OBSTACLE, loc)) {
                    explosionRange.add(loc);
                } else {
                    break; // Stop if there's an obstacle
                }
            }
        }
    
        // Calculate cells above the bomb
        for (int i = 1; i <= blast; i++) {
            if (model.inGrid(x, y - i)) {
                Location loc = new Location(x, y - i);
                if (model.isFree(PommerModel.OBSTACLE, loc)) {
                    explosionRange.add(loc);
                } else {
                    break; // Stop if there's an obstacle
                }
            }
        }
    
        // Calculate cells below the bomb
        for (int i = 1; i <= blast; i++) {
            if (model.inGrid(x, y + i)) {
                Location loc = new Location(x, y + i);
                if (model.isFree(PommerModel.OBSTACLE, loc)) {
                    explosionRange.add(loc);
                } else {
                    break; // Stop if there's an obstacle
                }
            }
        }
    
        explosionRange.add(bombLocation);
        return explosionRange;
    }
    
}
