// ========================= src/controller/TranspositionTable.java (NOVO ARQUIVO) =========================
package controller;

import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {

    private final Map<Long, TableEntry> table = new HashMap<>();

    public void store(long hash, TableEntry entry) {
        // Uma estratégia simples: sempre substitui.
        // Estratégias melhores podem preferir entradas de maior profundidade.
        table.put(hash, entry);
    }

    public TableEntry probe(long hash) {
        return table.get(hash);
    }

    public void clear() {
        table.clear();
    }
}