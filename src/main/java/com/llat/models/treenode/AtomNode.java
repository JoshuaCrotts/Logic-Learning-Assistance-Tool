package com.llat.models.treenode;

/**
 *
 */
public class AtomNode extends WffTree {

    /**
     * Symbol to define the proposition/atom.
     */
    private final String ATOM_SYMBOL;

    public AtomNode(String _symbol) {
        super(NodeType.ATOM);
        this.ATOM_SYMBOL = _symbol;
    }

    public String getSymbol() {
        return this.ATOM_SYMBOL;
    }

    @Override
    public String getStringRep() {
        return this.getSymbol();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + this.ATOM_SYMBOL;
    }
}
