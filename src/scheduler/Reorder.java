package scheduler;

import java.util.*;

public class Reorder extends Scheduler{


    private ArrayList<Node> sequence;
    private RC rc;

    public Reorder(RC rc) {
        super();
        this.rc = rc;
        this.sequence = new ArrayList<>();
    }

    /**
     * Use the graph given to create a schedule.
     *
     * @param sg - the dependency graph
     * @return a schedule for the given graph
     */
    @Override
    public Schedule schedule(Graph sg) {
        return null;
    }

    private void doReorder(Schedule schedOfASAP, Schedule schedOfALAP) {
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
                if (!sequence.contains(n.getKey())) sequence.add(n.getKey());
            }
        }
    }

    public Schedule schedule(Schedule schedOfASAP, Schedule schedOfALAP){
        doReorder(schedOfASAP, schedOfALAP);

        Schedule s = new Schedule();

        RC avalRC = rc;
        for (int timeStep=0; timeStep<schedOfALAP.length(); timeStep++) {
            Set<Node> ns = schedOfALAP.nodes(timeStep);
            /* the candidate set */
            HashMap<RT, Set<Node>> U = new HashMap<>();
            for (Node n: ns) {
                //if (n.isCandidate())
                   // U.put(n.getRT(), )
            }
            /* the set of operations of type RT started, but not completed by time step t*/
            HashMap<RT, Set<Node>> T = new HashMap<>();

            /* find out if the RC is satisfied */

            HashMap<Node, RT> map = new HashMap<>();
            for (Node n: ns) {
                map.put(n, n.getRT());
            }

            Map<String, Set<RT> > tempRes = new HashMap<>(avalRC.getAllRes());
            HashMap<Node, RT> nodesOutRC = new HashMap<>();
            HashMap<Node, RT> nodesInRC = new HashMap<>();
            for (Map.Entry<Node, RT> node: map.entrySet()){
                Boolean resIsEnog = false;
                for (Map.Entry<String, Set<RT>> entry: tempRes.entrySet()) {
                    if (entry.getValue().contains(node.getValue())) {
                        tempRes.remove(entry.getKey());
                        resIsEnog = true;
                        break;
                    }
                }
                if(!resIsEnog)
                    nodesOutRC.put(node.getKey(), node.getValue());
                else
                    nodesInRC.put(node.getKey(), node.getValue());
            }

            /* random distribute the Resource if enough*/
            if (nodesOutRC.isEmpty()) {
                for (Node n: nodesInRC.keySet()) {
                    ArrayList<String> list = new ArrayList(rc.getRes(n.getRT()));
                    int randomIndex = new Random().nextInt(list.size());
                    String randomItem = list.get(randomIndex);
                    Interval slot = new Interval(timeStep, timeStep + n.getDelay() - 1);
                    s.add(n , slot, randomItem);
                    list.remove(randomItem);
                }
            }

            /*
            Set<String> set = avalRC.getRes(entry.getValue());
            if(!set.isEmpty()) {
                tempRes.remove()
            }
            if (RTTop.getRTTop(entry.getKey())==RTTop.ALU) {
                numOfALU -= entry.getValue().size();
            }
            if (RTTop.getRTTop(entry.getKey())==RTTop.MUL) {
                numOfMUL -= entry.getValue().size();
            }*/


            // TODO: 17.12.22 update the avalRC before next round
        }





/*
        for (int slot=0; slot<schedOfALAP.length(); slot++) {
            if (schedOfALAP.nodes(slot).size() == 1) {
                ArrayList<Node> list = new ArrayList<>(schedOfALAP.nodes(slot));
                sequence.add(list);
            }else {
                for (Node n : schedOfALAP.nodes(slot)) {
                    Interval itv = schedOfALAP.slot(n);
                    int lb = itv.lbound;
                    if (lb == slot) {

                    }
                }
            }
        }*/
        return s;
    }
}
