package com.llat.algorithms.models;

/**
 *
 */
public enum NDStep {

    HS("HS", "Hypothetical Syllogism"),
    MT("MT", "Modus Tollens"),
    MP("MP", "Modus Ponens"),
    II("II", "Implication Introduction"),
    P("P", "Premise"),
    C("C", "Conclusion"),
    DS("DS", "Disjunctive Syllogism"),
    AE("&E", "Conjunction Elimination"),
    AI("&I", "Conjunction Introduction"),
    RI("⊥I", "Contradiction"),
    RE("⊥E", "Contradiction Elimination"),
    OI("∨I", "Disjunction Introduction"),
    DEM("DeM", "De Morgan"),
    BCI("↔I", "Biconditional Introduction"),
    BCE("↔E", "Biconditional Elimination"),
    MI("MI", "Material Implication"),
    EI("EI", "Existential Introduction"),
    EE("EE", "Existential Elimination"),
    UI("UI", "Universal Introduction"),
    UE("UE", "Universal Elimination");


    /**
     *
     */
    private final String STEP;

    /**
     *
     */
    private final String TEXT_STEP;

    NDStep(String _step, String _textStep) {
        this.STEP = _step;
        this.TEXT_STEP = _textStep;
    }

    @Override
    public String toString() {
        return STEP;
    }

    public String getTextStep() {
        return this.TEXT_STEP;
    }
}
