package example;

// Environment code for project pommerman

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.asSyntax.parser.*;

import java.util.logging.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Implements the Environment for Pommerman
 */
public class PommerEnv extends Environment {

    /** Constants for jason terms and literals */
    // TODO

    private Logger logger = Logger.getLogger("pommerman."+PommerEnv.class.getName());

    private PommerModel model;
    private PommerView view;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        model = new PommerModel();
        view = new PommerView(model);
        model.setView(view);
        // updatePercepts();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
