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
    public static final Term ma = Literal.parseLiteral("move_agent");

    static Logger logger = Logger.getLogger("pommerman."+PommerEnv.class.getName());

    //private PommerModel model;
    //private PommerView view;
    PommerModel model;
    PommerView view;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        this.model = new PommerModel();
        this.view = new PommerView(model);
        model.setView(view);
        // updatePercepts();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info(agName+" doing: "+ action);
        try {
            if (action.equals(ma)) {
                // do something
                model.moveAgent();
                logger.info("azione move_agent");
            } else {
                logger.info("azione inaspettata");
                return false; // inform action was not executed with success
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return true; // inform action was executed with success
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
