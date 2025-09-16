// ========================= src/controller/TableEntry.java (NOVO ARQUIVO) =========================
package controller;

public class TableEntry {
    public enum NodeType { EXACT, LOWERBOUND, UPPERBOUND }

    public final double score;
    public final int depth;
    public final NodeType type;

    public TableEntry(double score, int depth, NodeType type) {
        this.score = score;
        this.depth = depth;
        this.type = type;
    }
}