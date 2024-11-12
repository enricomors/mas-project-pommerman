package utils;

import jason.environment.grid.Location;

public class QueueElement {

    public int dist;
    public Location position;

    public QueueElement(int dist, Location position) {
        this.dist = dist;
        this.position = position;
    }
}
