package scheduler;

import java.io.BufferedReader;
import java.util.*;

public class SDC {

    private ArrayList<Node> sequence;
    private RC rc;
    private HashMap<Node, Integer> starting_time;
    private HashSet<Equation> eqDD;
    private HashSet<Equation> eqRC;
    public SDC(Schedule schedOfASAP, Schedule schedOfALAP, RC rc) {
        this.rc = rc;
        this.sequence = doReorder(schedOfASAP, schedOfALAP);
        this.eqDD = new HashSet<>();
        this.eqRC = new HashSet<>();
    }

    /**
     * use an array or listarray to note the integer sequence of the nodes.
     * If there is a tie between nodes in ALAP, use an ASAP as tie breaker.
     * @param schedOfASAP schdule of ASAP
     * @param schedOfALAP schdule of ALAP
     * @return result of reordering in arraylist
     */
    private ArrayList<Node> doReorder(Schedule schedOfASAP, Schedule schedOfALAP) {
        ArrayList<Node> toRe = new ArrayList<>();
        Map<Integer, Set<Node>> alap = schedOfALAP.getSlots();
        HashMap<Node, Integer> tmpRes;
        for (Map.Entry<Integer, Set<Node>> entry: alap.entrySet()) {
            tmpRes = new HashMap<>(); // the order in the alap
            for (Node n: entry.getValue()){
                tmpRes.put(n, schedOfASAP.slot(n).lbound);
            }
            List<Map.Entry<Node, Integer>> list = new ArrayList(tmpRes.entrySet());
            Collections.sort(list, (o1, o2) -> (o1.getValue().compareTo(o2.getValue())));
            for (Map.Entry<Node, Integer> n: list) {
                if (!toRe.contains(n.getKey())) toRe.add(n.getKey());
            }
        }

        return toRe;
    }

    private void generateRC(){
        //int c_res_k =
        /**
         * for all Res in ResC
         *      ResK
         *      for all source instance of ResK
         *          this time of utilization of Resk is Vi
         *          find the next usage of this Resource instance Vj
         *          but ensure that the number of resource k between Vi and Vj = Cresk - 1
         *          vi - vj <= - Latency(Vi)
         */

        Map<Set<RT>, Integer> resInst = rc.getResInst();
        Map<Set<RT>, Set<Node>> resInst2Node = new HashMap<>(); //get the resource instance used by diff ops
        for (Set<RT> item: resInst.keySet()) {
            Set<Node> setOfNodes = new HashSet<>();
            for (Node n: sequence) {
                if (item.contains(n.getRT()))
                    setOfNodes.add(n);
            }
            resInst2Node.put(item, setOfNodes);
        }

        System.out.println("Hello World2");
    }

    private void generateDD(){
        for (Node pred: sequence) {
            for(Node succ: pred.successors()){
                Equation eq = new Equation(pred, succ, ineqOp.smallerThanEqual, -pred.getDelay());
                eqDD.add(eq);
            }
        }
    }




    private void lex(BufferedReader input){

    }

    public void generateEq(){
            generateDD();
            generateRC();
    }
}
