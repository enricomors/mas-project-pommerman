package example;

import jason.environment.grid.GridWorldView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.logging.*;
import java.util.Hashtable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Creates the visualization part - INTERFACCIA GRAFICA
 */
public class PommerView extends GridWorldView {

    PommerEnv env = null;
    Color ruddyBrown = new Color(187,101,40); // new color for wooden walls
    boolean setImages = true; // flag to set the images

    //visual components
    JSlider jSpeed; // controls speed
    JLabel jCycle;  // timer displayed

    JLabel jBombA1; // dynamic info for agent 1
    JLabel jRangeA1;

    JLabel jBombA2; // dynamic info for agent 2
    JLabel jRangeA2;

    JLabel jBombA3; // dynamic info for agent 3
    JLabel jRangeA3;

    JLabel jBombA4; // dynamic info for agent 4
    JLabel jRangeA4;




    // logger for debugging
    static Logger logger = Logger.getLogger("pommerman."+PommerModel.class.getName());

    /** Creates view instance */
    public PommerView(PommerModel model) {
        super(model, "Pommerman", 600);
        setSize(650,740);
        // some default settings
        defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
        setVisible(true);
        repaint();
    }

    /**
     * Setter for the environment
     */
    public void setEnv(PommerEnv env) {
        this.env = env;
    }

    /**
     * Default method to initialize visual components
     */
    @Override
    public void initComponents(int width) {
        super.initComponents(width);

        // ==== BOTTOM SLIDER SPEED ====

        // Slider
        jSpeed = new JSlider();
        jSpeed.setMinimum(0);
        jSpeed.setMaximum(400);

        // set initial value (the higher, the slower)
        jSpeed.setValue(300);

        jSpeed.setPaintTicks(true);
        jSpeed.setPaintLabels(true);
        jSpeed.setMajorTickSpacing(100);
        jSpeed.setMinorTickSpacing(20);
        jSpeed.setInverted(true);

        // Create labels for the slider
        Hashtable<Integer,Component> labelTable = new Hashtable<Integer,Component>();
        labelTable.put(0, new JLabel("max"));
        labelTable.put(200, new JLabel("speed"));
        labelTable.put(400, new JLabel("min"));
        jSpeed.setLabelTable(labelTable);

        // add slider to panel - iCreate a panel for the slider and add it to the panel
        JPanel tempPanel = new JPanel(new FlowLayout());
        //tempPanel.setBorder(BorderFactory.createEtchedBorder());
        tempPanel.add(jSpeed);

        // container dello slider, lo fa centrato
        JPanel args = new JPanel(); //container di tutti e 3 le componenti
        args.setLayout(new BoxLayout(args, BoxLayout.Y_AXIS));
        args.add(tempPanel);

        add(args, BorderLayout.SOUTH); // add the ultimate slider panel under the game window


        // ==== UPPER PANELS AGENTS INFO + CYCLE ====

        // Info panel on the upper part of the view
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        upperPanel.setBorder(BorderFactory.createEtchedBorder());

        // title Pommerman
        tempPanel = new JPanel(new FlowLayout());
        tempPanel.add(new JLabel("Pommerman"));
        tempPanel.setBorder(BorderFactory.createEtchedBorder());  //BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        upperPanel.add(tempPanel);

        // cycle
        tempPanel = new JPanel(new FlowLayout());
        tempPanel.add(new JLabel("Cycle:"));
        jCycle = new JLabel("0");
        tempPanel.add(jCycle);
        upperPanel.add(tempPanel);

        // flow container for the 4 box layout that contains the info for each layer
        JPanel agentContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));

        // AGENT 1
        JPanel ag1Box = new JPanel();
        ag1Box.setLayout(new BoxLayout(ag1Box, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new FlowLayout());   // label agent 1
        tempPanel.add(new JLabel("<html><span style='font-size:12px'>Agent 1</span></html>"));
        URL imageUrl_1 = getClass().getResource("/img/agent0.png"); // img
        ImageIcon imageIcon_1 = new ImageIcon(imageUrl_1);
        tempPanel.add(new JLabel(imageIcon_1));
        ag1Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // bomb 1
        tempPanel.add(new JLabel("Bombs:"));
        jBombA1 = new JLabel("1");
        tempPanel.add(jBombA1);
        ag1Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // range 1
        tempPanel.add(new JLabel("Range:"));
        jRangeA1 = new JLabel("1");
        tempPanel.add(jRangeA1);
        ag1Box.add(tempPanel);

        // AGENT 2
        JPanel ag2Box = new JPanel();
        ag2Box.setLayout(new BoxLayout(ag2Box, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new FlowLayout());                   // label agent 2
        tempPanel.add(new JLabel("<html><span style='font-size:12px'>Agent 2</span></html>"));
        URL imageUrl_2 = getClass().getResource("/img/agent1.png"); // img
        ImageIcon imageIcon_2 = new ImageIcon(imageUrl_2);
        tempPanel.add(new JLabel(imageIcon_2));
        ag2Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());                   // bomb
        tempPanel.add(new JLabel("Bombs:"));
        jBombA2 = new JLabel("1");
        tempPanel.add(jBombA2);
        ag2Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());                   // range
        tempPanel.add(new JLabel("Range:"));
        jRangeA2 = new JLabel("1");
        tempPanel.add(jRangeA2);
        ag2Box.add(tempPanel);

        // AGENT 3
        JPanel ag3Box = new JPanel();
        ag3Box.setLayout(new BoxLayout(ag3Box, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new FlowLayout());   // label agent 3
        tempPanel.add(new JLabel("<html><span style='font-size:12px'>Agent 3</span></html>"));
        URL imageUrl_3 = getClass().getResource("/img/agent2.png"); // img
        ImageIcon imageIcon_3 = new ImageIcon(imageUrl_3);
        tempPanel.add(new JLabel(imageIcon_3));
        ag3Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // bomb
        tempPanel.add(new JLabel("Bombs:"));
        jBombA3 = new JLabel("1");
        tempPanel.add(jBombA3);
        ag3Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // range
        tempPanel.add(new JLabel("Range:"));
        jRangeA3 = new JLabel("1");
        tempPanel.add(jRangeA3);
        ag3Box.add(tempPanel);

        // AGENT 2
        JPanel ag4Box = new JPanel();
        ag4Box.setLayout(new BoxLayout(ag4Box, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new FlowLayout());   // label agent 2
        tempPanel.add(new JLabel("<html><span style='font-size:12px'>Agent 4</span></html>"));
        URL imageUrl_4 = getClass().getResource("/img/agent3.png"); // img
        ImageIcon imageIcon_4 = new ImageIcon(imageUrl_4);
        tempPanel.add(new JLabel(imageIcon_4));
        ag4Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // bomb
        tempPanel.add(new JLabel("Bombs:"));
        jBombA4 = new JLabel("1");
        tempPanel.add(jBombA4);
        ag4Box.add(tempPanel);

        tempPanel = new JPanel(new FlowLayout());   // range
        tempPanel.add(new JLabel("Range:"));
        jRangeA4 = new JLabel("1");
        tempPanel.add(jRangeA4);
        ag4Box.add(tempPanel);

        agentContainer.add(ag1Box);
        agentContainer.add(ag2Box);
        agentContainer.add(ag3Box);
        agentContainer.add(ag4Box);
        upperPanel.add(agentContainer);

        add(upperPanel, BorderLayout.NORTH);

        // ===================================================================

        // Events handling for the slider
        jSpeed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (env != null) {
                    env.setSleep((int)jSpeed.getValue());
                }
            }
        });
    }


    // =============================
    // === METHOD TO UPDATE VIEW ===
    // =============================

    /**
     * Gets the current cycle to update the cycle counter in the view
     */
    public void setCycle(int c) {
        if (jCycle != null) {
            PommerModel wm = (PommerModel)model;  //GridWorldModel

            String steps = "";
            if (wm.getMaxSteps() > 0) {
                steps = "/" + wm.getMaxSteps();
            }
            jCycle.setText(c+steps);

            //jGoldsC.setText(wm.getGoldsInDepotRed() + " x " + wm.getGoldsInDepotBlue() + "/" + wm.getInitialNbGolds());
        }
    }

    /**
     * Increase the count of the bomb in the view
     */
    public void increaseCountBomb(int agID) {

        //int agID = env.getAgIdBasedOnName(agentName);
        String str_bomb = null;
        int n_bomb;

        switch (agID) {
            case 0:                                             // agent 1 rip sbagliati i nomi var, troppa sbatta cambiarli
                if(jBombA1 != null){
                    //get text from label
                    str_bomb = jBombA1.getText();
                    //cast text to int
                    n_bomb = Integer.parseInt(str_bomb);
                    //check if bomb < 0
                    jBombA1.setText(Integer.toString(n_bomb+1));
                }
                break;
            case 1:                                             // agent 2
                if(jBombA2 != null){
                    str_bomb = jBombA2.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    jBombA2.setText(Integer.toString(n_bomb+1));
                }
                break;
            case 2:                                             // agent 3
                if(jBombA3 != null){
                    str_bomb = jBombA3.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    jBombA3.setText(Integer.toString(n_bomb+1));
                }
                break;
            case 3:                                             // agent 4
                if(jBombA4 != null){
                    str_bomb = jBombA4.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    jBombA4.setText(Integer.toString(n_bomb+1));
                }
                break;
            default:
                logger.info("There's something wrong with agent ID - increaseCountBomb");
                break;
        }
    }

    /**
     * Decrease the count of the bomb in the view
     */
    public void decreaseCountBomb(int agID) {//String agentName

        //int agID = env.getAgIdBasedOnName(agentName);
        String str_bomb = null;
        int n_bomb;

        switch (agID) {
            case 0:                                             // agent 1
                if(jBombA1 != null){
                    //get text from label
                    str_bomb = jBombA1.getText();
                    //cast text to int
                    n_bomb = Integer.parseInt(str_bomb);
                    //check if bomb < 0
                    if(n_bomb > 0){
                        jBombA1.setText(Integer.toString(n_bomb-1));
                    }else{
                        logger.warning("Bomb count cannot be negative, find the error!");
                    }
                }
                break;
            case 1:                                             // agent 2
                if(jBombA2 != null){
                    str_bomb = jBombA2.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    if(n_bomb > 0){
                        jBombA2.setText(Integer.toString(n_bomb-1));
                    }else{
                        logger.warning("Bomb count cannot be negative, find the error!");
                    }
                }
                break;
            case 2:                                             // agent 3
                if(jBombA3 != null){
                    str_bomb = jBombA3.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    if(n_bomb > 0){
                        jBombA3.setText(Integer.toString(n_bomb-1));
                    }else{
                        logger.warning("Bomb count cannot be negative, find the error!");
                    }
                }
                break;
            case 3:                                             // agent 4
                if(jBombA4 != null){
                    str_bomb = jBombA4.getText();
                    n_bomb = Integer.parseInt(str_bomb);
                    if(n_bomb > 0){
                        jBombA4.setText(Integer.toString(n_bomb-1));
                    }else{
                        logger.warning("Bomb count cannot be negative, find the error!");
                    }
                }
                break;
            default:
                logger.info("There's something wrong with agent ID - decreaseCountBomb");
                break;
        }
    }


    /**
     * Increase the range of the bomb in the view
     */
    public void increaseRange(int agID){

        //int agID = env.getAgIdBasedOnName(agentName);
        String str_range = null;
        int n_range;

        switch (agID) {
            case 0:                                             // agent 1
                if(jRangeA1 != null){
                    //get text from label
                    str_range = jRangeA1.getText();
                    //cast text to int
                    n_range = Integer.parseInt(str_range);
                    //increase range +1
                    jRangeA1.setText(Integer.toString(n_range+1));
                }
                break;
            case 1:                                             // agent 2
                if(jRangeA2 != null){
                    str_range = jRangeA2.getText();
                    n_range = Integer.parseInt(str_range);
                    jRangeA2.setText(Integer.toString(n_range+1));
                }
                break;
            case 2:                                             // agent 3
                if(jRangeA3 != null){
                    str_range = jRangeA3.getText();
                    n_range = Integer.parseInt(str_range);
                    jRangeA3.setText(Integer.toString(n_range+1));
                }
                break;
            case 3:                                             // agent 4
                if(jRangeA4 != null){
                    str_range = jRangeA4.getText();
                    n_range = Integer.parseInt(str_range);
                    jRangeA4.setText(Integer.toString(n_range+1));
                }
                break;
            default:
                logger.info("There's something wrong with agent ID - increaseRange");
                break;
        }
    }

    // ============================
    // === METHOD TO DRAW STUFF ===
    // ============================
    /**
     * Default method which gets called anytime Swing has to draw something
     */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object); // call the superclass's draw method
        if(setImages) {
            drawImageObjects(g, x, y, object);
        } else {
            // no images required, draw normal objects
            drawDefaultObjects(g, x, y, object);
        }
    }

    /**
     * Default mehtod which gets called when drawing agents
     */
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        if (setImages) {
            drawImageAgent(g, x, y, c, id);
        } else {
            drawDefaultAgent(g, x, y, c, id);
        }
        // for repainting agents as they move
        repaint();
    }

    /**
     * Draws agent using icon resources located in resources/img folder
     */
    public void drawImageAgent(Graphics g, int x, int y, Color c, int id) {
        // Load and draw the corresponding PNG image based on the agent's ID
        switch (id) {
            case 0:
                drawImage(g, x, y, "agent0.png");
                break;
            case 1:
                drawImage(g, x, y, "agent1.png");
                break;
            case 2:
                drawImage(g, x, y, "agent2.png");
                break;
            case 3:
                drawImage(g, x, y, "agent3.png");
                break;
            default:
                // If the ID is out of range, draw a default agent
                super.drawAgent(g, x, y, c, id);
                break;
        }
    }

    /**
     * Draws agent using standard Java Graphics
     */
    public void drawDefaultAgent(Graphics g, int x, int y, Color c, int id) {
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
    }

    /**
     * Draws environment objects using icon resources located in resources/img folder
     */
    public void drawImageObjects(Graphics g, int x, int y, int object) {
        // Load and draw PNG images based on object type
        switch (object) {
            case PommerModel.WOODENWALL:
                drawImage(g, x, y, "wood.png");
                break;
            case PommerModel.BOMB:
                drawImage(g, x, y, "bomb.png");
                break;
            case PommerModel.INCREASE_BLAST:
                drawImage(g, x, y, "incrrange.png");
                break;
            case PommerModel.INCREASE_BOMBS:
                drawImage(g, x, y, "extrabomb.png");
                break;
                // Add cases for other object types if needed
            case PommerModel.FIRE:
                drawImage(g, x, y, "flames.png");
                break;
        }
    }

    /**
     * Draws environment objects using standard Java Graphics
     */
    public void drawDefaultObjects(Graphics g, int x, int y, int object) {
        switch (object) {
            case PommerModel.WOODENWALL:
                drawWoodenWalls(g, x, y);
                break;
            }
    }

    /**
     * Draw woodenwalls using standard graphic objects
     */
    public void drawWoodenWalls(Graphics g, int x, int y) {
        g.setColor(ruddyBrown);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.pink);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.drawLine(x * cellSizeW + 2, y * cellSizeH + 2, (x + 1) * cellSizeW - 2, (y + 1) * cellSizeH - 2);
        g.drawLine(x * cellSizeW + 2, (y + 1) * cellSizeH - 2, (x + 1) * cellSizeW - 2, y * cellSizeH + 2);
    }

    /**
     * Draws a .png image in the view
     */
    private void drawImage(Graphics g, int x, int y, String imageName) {
        URL imageUrl = getClass().getResource("/img/" + imageName);
        ImageIcon imageIcon = new ImageIcon(imageUrl);
        Image image = imageIcon.getImage();

        g.drawImage(image, x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH, null);
    }
}
