package scheduler;

enum ineqOp{
    smallerThan,
    smallerThanEqual,
    biggerThan,
    biggerThanEqual;
}

public class Equation {
    private Node pred;
    private Node succ;
    private ineqOp Op;
    private int rhs;


    public Equation(Node pred, Node succ, ineqOp mathOp, int rhs) {
        this.pred = pred;
        this.succ = succ;
        this.Op = mathOp;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        String OpToPrint;
        switch (Op){
            case smallerThan:
                OpToPrint = "<";
                break;
            case smallerThanEqual:
                OpToPrint = "<=";
                break;
            case biggerThan:
                OpToPrint = ">";
                break;
            case biggerThanEqual:
                OpToPrint = ">=";
                break;
            default:
                break;
        }
        return "Equation{" + pred +
                ", succ=" + succ +
                ", rhs=" + rhs +
                '}';
    }
}
