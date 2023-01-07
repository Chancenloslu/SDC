package scheduler;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LinearProgram;

import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDC {

    private ArrayList<Node> sequence;
    private RC rc;
    private HashMap<Node, Integer> starting_time;
    private ArrayList<Equation> eqDD;
    private ArrayList<Equation> eqRC;
    public SDC(Schedule schedOfASAP, Schedule schedOfALAP, RC rc) {
        this.rc = rc;
        this.sequence = doReorder(schedOfASAP, schedOfALAP);
        this.eqDD = new ArrayList<>();
        this.eqRC = new ArrayList<>();
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
        /* if in the same timestep, order with asap */
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
        /* parse the resource Instance & map the nodes to it */
        Map<Set<RT>, Integer> resInst = rc.getResInst();
        Map<Set<RT>, ArrayList<Node>> resInst2Node = new HashMap<>(); //get the resource instance used by diff ops
        for (Set<RT> item: resInst.keySet()) {
            ArrayList<Node> setOfNodes = new ArrayList<>();
            for (Node n: sequence) {
                if (item.contains(n.getRT()))
                    setOfNodes.add(n);
            }
            resInst2Node.put(item, setOfNodes);
        }

        /* start generating equation */
        for (Map.Entry<Set<RT>, ArrayList<Node>> entry: resInst2Node.entrySet()) {
            int alpha = resInst.get(entry.getKey()); //amount of certain resource instance
            ArrayList<Node> nodes = entry.getValue();
            for (int i=0; i<entry.getValue().size()-alpha; i++) {
                try {
                    Node pred = nodes.get(i);
                    Node succ = nodes.get(i+alpha);
                    Equation eq = new Equation(pred, succ, ineqOp.smallerThanEqual, -pred.getDelay());
                    eqRC.add(eq);
                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException("exception thrown: " + e);
                }
            }
        }
        printRCeq();
    }

    private void generateDD(){
        for (Node pred: sequence) {
            for(Node succ: pred.successors()){
                Equation eq = new Equation(pred, succ, ineqOp.smallerThanEqual, -pred.getDelay());
                eqDD.add(eq);
            }
        }
        printDDeq();
    }

    private void printRCeq() {
        System.out.println("\nEquations for Resource Constarints:\n");
        for (Equation e: eqRC) {
            System.out.println(e);
        }
    }

    private void printDDeq() {
        System.out.println("\nEquations for Data Dependencies:\n");
        for (Equation e : eqDD) {
            System.out.println(e);
        }
    }

    public Schedule lp_solver() throws LpSolveException {
        Schedule toRe = new Schedule();
        LpSolve lp;
        int ret;
        int Ncol = sequence.size();
        int[] colno = new int[Ncol];
        double[] row = new double[Ncol];
        lp = LpSolve.makeLp(0, Ncol);
        if(lp.getLp() == 0)
            ret = 1;  //couldn't construct a new model...

        return toRe;
    }

    /**
     * low level interface solver
     * @return
     */
    public Schedule ll_solver() {
        Schedule toRe = new Schedule();
        int Ncol = sequence.size();
        ArrayList<Equation> eqConstraint = new ArrayList<>(eqDD);
        eqConstraint.addAll(eqRC);
        int Nrow = eqConstraint.size();

        double[][] matrix = new double[Nrow][Ncol];
        Pattern p = Pattern.compile("(n(\\d+))");
        Matcher m;
        for (int i=0; i<Nrow; i++) {
            Equation eq = i<eqDD.size() ? eqDD.get(i) : eqRC.get(i-eqDD.size());
            String[] predNsucc = new String[]{eq.getPred().id, eq.getSucc().id};
            for (int j=0; j<predNsucc.length; j++) {
                String str = predNsucc[j];
                m = p.matcher(str);
                if (m.matches()){
                    int index = Integer.parseInt(m.group(2));
                    matrix[i][index] = j==0? 1.0:-1.0;
                }else {
                    throw new RuntimeException( "node name " + str +" not matched");
                }
            }
        }

        double[] tmp = new double[Ncol];
        tmp[Ncol-1] = 1.0;
        /* Objective function */
        LinearProgram lp = new LinearProgram(tmp);
        /* Constraints function */
        for (int i=0; i<Nrow; i++) {
            lp.addConstraint(new LinearSmallerThanEqualsConstraint(matrix[i], eqConstraint.get(i).getRhs(), "c"+i));
        }

        lp.setMinProblem(true);
        LinearProgramSolver solver = SolverFactory.newDefault();
        double[] sol = solver.solve(lp);
        LPSolution losol = new LPSolution(sol, lp);
        System.out.println(losol);
        return toRe;
    }

    /**
     * high level interface solver
     * @return
     */
    public Schedule hl_solver()  {
        Schedule ret = new Schedule();

        LPWizard lpw = new LPWizard();
        /* set the objective function */
        /*LPWizard tmpLpw = lpw.plus(sequence.get(0).id, 1.0);
        for (int i=1; i<sequence.size(); i++) {
            tmpLpw = tmpLpw.plus(sequence.get(i).id,1.0);
        }*/
        lpw.plus("n20", 1.0);
        lpw.setMinProblem(true);
        //lpw.addConstraint("c0", t_total, ">=").plus(finalnode, 1.0);
        /* constraint of data dependancies*/
        for (int i=0; i<eqDD.size(); i++) {
            String mathOp = "";
            Equation eq = eqDD.get(i);
            switch (eq.getOp()){
                case smallerThanEqual:
                    mathOp = ">=";
                    break;
                case smallerThan:
                    mathOp = ">";
                    break;
                case biggerThanEqual:
                    mathOp = "<=";
                    break;
                case biggerThan:
                    mathOp = "<";
                    break;
                default:
                    break;
            }
            lpw.addConstraint("eqDD"+i, eq.getRhs(), mathOp).plus(eq.getPred().id,1).plus(eq.getSucc().id,-1).setAllVariablesInteger();
        }
        /* constraint of resource constraint*/
        for (int i=0; i<eqRC.size(); i++) {
            String mathOp = "";
            Equation eq = eqRC.get(i);
            switch (eq.getOp()) {
                case smallerThanEqual:
                    mathOp = ">=";
                    break;
                case smallerThan:
                    mathOp = ">";
                    break;
                case biggerThanEqual:
                    mathOp = "<=";
                    break;
                case biggerThan:
                    mathOp = "<";
                    break;
                default:
                    break;
            }
            lpw.addConstraint("eqRC"+i, eq.getRhs(), mathOp).plus(eq.getPred().id,1).plus(eq.getSucc().id,-1).setAllVariablesInteger();
        }

        LPSolution lps = lpw.solve();
        System.out.println(lps);

        for (int i = 0; i < sequence.size(); i++) {
            Node n = sequence.get(i);
            int timeSlot = (int) lps.getInteger(n.id);
            int end = timeSlot + n.getDelay() - 1;
            ret.add(n, new Interval(timeSlot, end), n.getRT().name);
        }
        return ret;
    }
    public Schedule runSolver (int seletion) {
        Schedule res = null;
        generateEq();
        switch (seletion) {
            case 0:
                try {
                    res = lp_solver();
                } catch (LpSolveException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 1:
                res = ll_solver();
                break;
            case 2:
                res = hl_solver();
                break;
            default:
                throw new RuntimeException("no such Solver in SDC!");
        }

        return res;
    }
    public void generateEq(){
            generateDD();
            generateRC();
    }
}
