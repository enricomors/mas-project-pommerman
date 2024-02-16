package example;

import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Creates the visualization part
 */
public class PommerView extends GridWorldView {

    /** Creates view instance */
    public PommerView(PommerModel model) {
        super(model, "Pommerman", 600);
        // some default settings
        defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
        setVisible(true);
        repaint();
    }

    /**
     * Draw agents 
     */
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        // label of the agent
        String label = "A"+(id + 1);
        // color of the agent
        c = Color.blue;
        // draws agent
        super.drawAgent(g, x, y, c, -1); // -1 because otherwise it prints the id by default.
        // color of label
        g.setColor(Color.white);
        // write label
        super.drawString(g, x, y, defaultFont, label);
        repaint();
    }
}
