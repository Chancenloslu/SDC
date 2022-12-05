package scheduler;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;

public class SDC {

    private Schedule schedule;

    private HashMap<Node, Integer> starting_time;
    private HashSet<Equation> data_dependency;
    public SDC(Schedule schedule) {
        this.schedule = schedule;
        data_dependency = new HashSet<Equation>();
        starting_time = new HashMap<>();
        for (Node nd: schedule.nodes()) {
            starting_time.put(nd, schedule.slot(nd).lbound);
        }
    }


    private void generateDD(){
        for (Node pred: starting_time.keySet()) {
            for(Node succ: pred.successors()){
                Equation eq = new Equation(pred, succ, ineqOp.smallerThanEqual, -pred.getDelay());
                data_dependency.add(eq);
            }
        }
    }




    private void lex(BufferedReader input){

    }

    public void generateEq(Boolean DD) {
        if (DD)
            generateDD();
    }
}
