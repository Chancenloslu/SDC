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

    public Node getPred() {
        return pred;
    }

    public void setPred(Node pred) {
        this.pred = pred;
    }

    public Node getSucc() {
        return succ;
    }

    public void setSucc(Node succ) {
        this.succ = succ;
    }

    public ineqOp getOp() {
        return Op;
    }

    public void setOp(ineqOp op) {
        Op = op;
    }

    public int getRhs() {
        return rhs;
    }

    public void setRhs(int rhs) {
        this.rhs = rhs;
    }

    public Equation(Node pred, Node succ, ineqOp mathOp, int rhs) {
        this.pred = pred;
        this.succ = succ;
        this.Op = mathOp;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        String OpToPrint = "";
        switch (Op){
            case smallerThan:
                OpToPrint = " < ";
                break;
            case smallerThanEqual:
                OpToPrint = " <= ";
                break;
            case biggerThan:
                OpToPrint = " > ";
                break;
            case biggerThanEqual:
                OpToPrint = " >= ";
                break;
            default:
                break;
        }
        return  pred + "\t - \t" + succ + "\t" + OpToPrint + "\t" + rhs;
    }
}
