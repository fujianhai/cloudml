package org.cloudml.deployer2.workflow.executables;

import org.cloudml.deployer2.dsl.ActivityEdge;
import org.cloudml.deployer2.dsl.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Maksym on 25.03.2015.
 */
public class EdgeExecutable {
    private static final Logger journal = Logger.getLogger(ObjectExecutable.class.getName());
    ActivityEdge edge;

    public EdgeExecutable(ActivityEdge edge){
        this.edge = edge;
    }

    public void execute(){

        edge.getProperties().put("Status", String.valueOf(Element.Status.ACTIVE));

        String target = "no target";
        if (edge.getTarget() != null) {
            target = edge.getTarget().getClass().getSimpleName();
        }
        String source = edge.getSource().getClass().getSimpleName();
        identifyNode(target);
        identifyNode(source);
        if (edge.isObjectFlow()){
            journal.log(Level.INFO, "Passing data from " + source + " to " + target);
        } else {
            journal.log(Level.INFO, "Moving from " + source + " to " + target);
        }

        edge.getProperties().put("Status", String.valueOf(Element.Status.DONE));
    }

    private void identifyNode(String target) {
        switch (target){
            case "ActivityFinalNode":
                target = "Final node";
                break;
            case "ActivityInitialNode":
                target = "Start node";
                break;
            case "Fork":
                target = "Fork";
                break;
            case "Join":
                target = "Join";
                break;
        }
    }
}