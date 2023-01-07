package scheduler;

public enum RTTop {
    ALU,
    MUL,
    OTR; //other

    public static RTTop getRTTop(RT rt) {
        if (rt==RT.AND || rt==RT.OTHER || rt==RT.ADD || rt==RT.SUB || rt==RT.SH || rt==RT.OR || rt==RT.CMP || rt==RT.MEM){
            return ALU;
        } else if (rt==RT.MUL || rt==RT.DIV) {
            return MUL;
        } else
            return OTR;

    }
}
