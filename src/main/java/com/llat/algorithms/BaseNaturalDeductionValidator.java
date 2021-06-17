package com.llat.algorithms;

import com.llat.algorithms.models.NDFlag;
import com.llat.algorithms.models.NDStep;
import com.llat.algorithms.models.NDWffTree;
import com.llat.models.treenode.*;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 */
public abstract class BaseNaturalDeductionValidator {

    /**
     *
     */
    protected final LinkedList<WffTree> ORIGINAL_WFFTREE_LIST;

    /**
     *
     */
    protected final ArrayList<NDWffTree> PREMISES_LIST;

    /**
     *
     */
    protected final NDWffTree CONCLUSION_WFF;

    public BaseNaturalDeductionValidator(LinkedList<WffTree> _wffTreeList) {
        this.ORIGINAL_WFFTREE_LIST = _wffTreeList;
        this.PREMISES_LIST = new ArrayList<>();
        this.CONCLUSION_WFF = new NDWffTree(_wffTreeList.get(_wffTreeList.size() - 1).getChild(0), NDStep.C);

        // Add all premises to the list. The invariant is that the last element is guaranteed to be the conclusion.
        for (int i = 0; i < _wffTreeList.size() - 1; i++) {
            // Trim ROOT off the node if it's still there from ANTLR processing.
            WffTree wff = _wffTreeList.get(i).getNodeType() == NodeType.ROOT ? _wffTreeList.get(i).getChild(0)
                    : _wffTreeList.get(i);
            this.addPremise(new NDWffTree(wff, NDFlag.ACTIVE, NDStep.P));
        }
    }

    /**
     * Computes a natural deduction proof for a logic formula. The details should be listed in the
     * subclasses for FOPL and PL respectively.
     *
     * @return list of NDWffTree "args". These serve as the premises, with the last element in the list being
     * the conclusion.
     */
    public abstract LinkedList<NDWffTree> getNaturalDeductionProof();

    /**
     * Determines if we can "satisfy" a premise Wff W. W is satisfied when it is used in
     * the reduction or expansion of another Wff W'. In other words, if W is used to construct
     * W', W is satisfied. The idea is to recursively compute "goals" for a premise, then
     * by determining if those goals are satisfied, we can construct the overall goal.
     *
     * For example, suppose the goal is (A & B). The subgoals are then A and B. If A and B are
     * premises, these goals are satisfied by default. Thus, the overarching goal is satisfied.
     * Different rules apply for different operators.
     *
     * We pass both the parent NDWffTree as well as the WffTree since we cannot break down the
     * parent NDwffTree's WffTree - we can only recursively pass the children of _tree.
     *
     * @param _tree - WffTree object to recursively check for satisfaction.
     * @param _parent - NDWffTree "parent" of _tree; any children of _tree will be temporary "children" of _parent.
     * @return
     */
    protected boolean satisfy(WffTree _tree, NDWffTree _parent) {
        boolean satisfied = false;
        if (this.findDeMorganEquivalence(_tree, _parent)) {
            return true;
        } else if (this.findMaterialImplication(_tree, _parent)) {
            return true;
        } else if (_tree.isImp()) {
            satisfied = this.satisfyImplication(_tree, _parent);
        } else if (_tree.isAnd()) {
            satisfied = this.satisfyConjunction(_tree, _parent);
        } else if (_tree.isOr()) {
            satisfied = this.satisfyDisjunction(_tree, _parent);
        } else if (_tree.isBicond()) {
            satisfied = this.satisfyBiconditional(_tree, _parent);
        }

        // If we couldn't find anything to deduce/reduce the proposition with,
        // try to search for it in the premises list.
        for (NDWffTree ndWffTree : this.PREMISES_LIST) {
            if (ndWffTree.getWffTree().stringEquals(_tree)) {
                return true;
            }
        }

        return satisfied;
    }

    /**
     * Satisfying an implication nodes comes through for primary methods:
     *
     * 1. Modus Ponens (P -> Q), P therefore Q
     * 2. Modus Tollens (P -> Q), ~Q therefore ~P
     * 3. Hypothetical Syllogism (P->Q), (Q->R) therefore (P->R)
     * 4. Construction of (P -> Q) from P and Q.
     *
     * @param _impNode - implication node to check for satisfaction.
     * @param _parent - parent NDWffTree.
     * @return true if we can satisfy the implication goal, false otherwise.
     */
    protected boolean satisfyImplication(WffTree _impNode, NDWffTree _parent) {
        // If the parent is not the conclusion then we can attempt to do other rules on it.
        if (!this.isConclusion(_parent) && _parent.getWffTree().isImp()) {
            if (this.findModusPonens(_impNode, _parent)
                    || this.findModusTollens(_impNode, _parent)
                    || this.findHypotheticalSyllogism(_impNode, _parent)) {
                return true;
            }
        }

        // Otherwise, try to construct an implication node - see if both sides are satisfiable.
        if (this.satisfy(_impNode.getChild(0), _parent) && this.satisfy(_impNode.getChild(1), _parent)) {
            ImpNode impNode = new ImpNode();
            impNode.addChild(_impNode.getChild(0));
            impNode.addChild(_impNode.getChild(1));
            this.addPremise(new NDWffTree(impNode, NDFlag.II, NDStep.II,
                    this.getPremiseNDWffTree(_impNode.getChild(0)),
                    this.getPremiseNDWffTree(_impNode.getChild(1))));
            return true;
        }

        // Finally, check to see if this wff is a premise somewhere.
        return false;
    }

    /**
     * @param _conjTree
     * @param _parent
     * @return true if we can satisfy the conjunction goal, false otherwise.
     */
    protected boolean satisfyConjunction(WffTree _conjTree, NDWffTree _parent) {
        // First try to simplify if the root is a conjunction.
        if (!this.isConclusion(_parent) && _parent.getWffTree().isAnd()) {
            if (this.findSimplification(_conjTree, _parent)
                    && _conjTree.stringEquals(_parent.getWffTree())) {
                return true;
            }
        }

        // Then try to create a conjunction if it's a goal and satisfied on both sides.
        if (this.satisfy(_conjTree.getChild(0), _parent) && this.satisfy(_conjTree.getChild(1), _parent)) {
            AndNode andNode = new AndNode();
            andNode.addChild(_conjTree.getChild(0));
            andNode.addChild(_conjTree.getChild(1));
            this.addPremise(new NDWffTree(andNode, NDFlag.AI, NDStep.AI,
                    this.getPremiseNDWffTree(_conjTree.getChild(0)),
                    this.getPremiseNDWffTree(_conjTree.getChild(1))));
            return true;
        }
        return false;
    }

    /**
     * @param _disjTree
     * @param _parent
     * @return true if we can satisfy the disjunction goal, false otherwise.
     */
    protected boolean satisfyDisjunction(WffTree _disjTree, NDWffTree _parent) {
        // First try to perform DS if the root is a disjunction.
        if (!this.isConclusion(_parent) && _parent.getWffTree().isOr()) {
            if (this.findDisjunctiveSyllogism(_disjTree, _parent)
                    && _disjTree.stringEquals(_parent.getWffTree())) {
                return true;
            }
        }

        // Then try to create a conjunction if it's a goal and one of the two children are satisfied.
        if (this.satisfy(_disjTree.getChild(0), _parent) || this.satisfy(_disjTree.getChild(1), _parent)) {
            // There's two conditions: we're either adding from the conclusion or from
            // another premise. If the parent is the conclusion, then we're adding from
            // that (obviously) and one of the nodes won't be retrievable via getPremise....
            OrNode orNode = new OrNode();
            orNode.addChild(_disjTree.getChild(0));
            orNode.addChild(_disjTree.getChild(1));

            // Find out which operand is null (if any).
            NDWffTree lhsDisj = this.getPremiseNDWffTree(_disjTree.getChild(0));
            NDWffTree rhsDisj = this.getPremiseNDWffTree(_disjTree.getChild(1));
            if (this.isConclusion(_parent)) {
                if (lhsDisj == null) {
                    lhsDisj = new NDWffTree(_disjTree.getChild(0), NDStep.OI);
                } else {
                    rhsDisj = new NDWffTree(_disjTree.getChild(1), NDStep.OI);
                }
            }
            this.addPremise(new NDWffTree(orNode, NDFlag.OI, NDStep.OI, lhsDisj, rhsDisj));
            return true;
        }
        return false;
    }

    /**
     * @param _bicondTree
     * @param _parent
     * @return true if we can satisfy the biconditional goal, false otherwise.
     */
    protected boolean satisfyBiconditional(WffTree _bicondTree, NDWffTree _parent) {
        // First check to see if we can break any biconditionals down.
        if (!this.isConclusion(_parent) && _parent.getWffTree().isBicond()) {
            if (this.findBiconditionalElimination(_bicondTree, _parent)
                    && _bicondTree.stringEquals(_parent.getWffTree())) return true;
        }
        // We first have a subgoal of X -> Y and Y -> X.
        ImpNode impLhs = new ImpNode();
        ImpNode impRhs = new ImpNode();
        impLhs.addChild(_bicondTree.getChild(0));
        impLhs.addChild(_bicondTree.getChild(1));
        impRhs.addChild(_bicondTree.getChild(1));
        impRhs.addChild(_bicondTree.getChild(0));

        // Check to see if both implications are satisfied.
        if (this.satisfy(impLhs, _parent) && this.satisfy(impRhs, _parent)) {
            BicondNode bicondNode = new BicondNode();
            bicondNode.addChild(_bicondTree.getChild(0));
            bicondNode.addChild(_bicondTree.getChild(1));
            this.addPremise(new NDWffTree(bicondNode, NDFlag.BC, NDStep.BCI,
                    this.getPremiseNDWffTree(impLhs),
                    this.getPremiseNDWffTree(impRhs)));
            return true;
        }
        return false;
    }

    /**
     * @param _mpTree
     * @param _parent
     * @return true if we apply a modus ponens rule, false otherwise.
     */
    protected boolean findModusPonens(WffTree _mpTree, NDWffTree _parent) {
        if (!_parent.isMPActive()) {
            for (NDWffTree ndWffTree : this.PREMISES_LIST) {
                // Check to see if we have the antecedent satisfied.
                if (ndWffTree.getWffTree().stringEquals(_mpTree.getChild(0))) {
                    NDWffTree consequentNode = new NDWffTree(_mpTree.getChild(1), NDStep.MP, _parent, ndWffTree);
                    this.addPremise(consequentNode);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param _mtTree
     * @param _parent
     * @return true if we apply a modus tollens rule, false otherwise.
     */
    protected boolean findModusTollens(WffTree _mtTree, NDWffTree _parent) {
        if (!_parent.isMTActive()) {
            for (NDWffTree ndWffTree : this.PREMISES_LIST) {
                // Check to see if we have the negated consequent satisfied.
                if (_mtTree.getChild(1).stringEquals(BaseTruthTreeGenerator.getFlippedNode(ndWffTree.getWffTree()))) {
                    WffTree flippedWff = BaseTruthTreeGenerator.getFlippedNode(_mtTree.getChild(0));
                    NDWffTree flippedNode = new NDWffTree(flippedWff, NDStep.MT, _parent, ndWffTree);
                    this.addPremise(flippedNode);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param _andTree
     * @param _parent
     * @return true if we apply a simplification (&E) rule, false otherwise.
     */
    protected boolean findSimplification(WffTree _andTree, NDWffTree _parent) {
        if (!_parent.isAndEActive() && !_parent.isAndIActive()) {
            _parent.setFlags(NDFlag.AE);
            NDWffTree andLhs = new NDWffTree(_andTree.getChild(0), NDStep.AE, _parent);
            NDWffTree andRhs = new NDWffTree(_andTree.getChild(1), NDStep.AE, _parent);
            this.addPremise(andLhs);
            this.addPremise(andRhs);
            return true;
        }
        return false;
    }

    /**
     * @param _disjTree
     * @param _parent
     * @return true if we apply a disjunctive syllogism rule, false otherwise.
     */
    protected boolean findDisjunctiveSyllogism(WffTree _disjTree, NDWffTree _parent) {
        if (!_parent.isDSActive() && _disjTree.stringEquals(_parent.getWffTree())) {
            WffTree flippedLhs = BaseTruthTreeGenerator.getFlippedNode(_disjTree.getChild(0));
            WffTree flippedRhs = BaseTruthTreeGenerator.getFlippedNode(_disjTree.getChild(1));
            boolean lhs = this.isGoal(flippedLhs);
            boolean rhs = this.isGoal(flippedRhs);

            // If we do not satisfy one of them but do satisfy the other, then we can perform DS.
            if (Boolean.logicalXor(lhs, rhs)) {
                NDWffTree ndWffTree = null;
                if (lhs) {
                    ndWffTree = new NDWffTree(_disjTree.getChild(1), NDStep.DS, _parent, this.getPremiseNDWffTree(flippedLhs));
                } else {
                    ndWffTree = new NDWffTree(_disjTree.getChild(0), NDStep.DS, _parent, this.getPremiseNDWffTree(flippedRhs));
                }

                _parent.setFlags(NDFlag.DS);
                this.addPremise(ndWffTree);
                return true;
            }
        }
        return false;
    }

    /**
     * @param _impNode
     * @param _parent
     * @return true if we apply a hypothetical syllogism rule, false otherwise.
     */
    protected boolean findHypotheticalSyllogism(WffTree _impNode, NDWffTree _parent) {
        for (NDWffTree othNdWffTree : this.PREMISES_LIST) {
            WffTree othImp = othNdWffTree.getWffTree();
            if (_parent != othNdWffTree && othImp.isImp()
                    && (!_parent.isHSActive() || !othNdWffTree.isHSActive())) {
                // X == Y && Y == Z OR Y == Z && X == Y check to see if the antecedent of one
                // is equal to the consequent of the other.
                ImpNode impNode = null;
                if (_impNode.getChild(1).stringEquals(othImp.getChild(0))) {
                    impNode = new ImpNode();
                    impNode.addChild(_impNode.getChild(0));
                    impNode.addChild(othImp.getChild(1));
                } else if (othImp.getChild(1).stringEquals(_impNode.getChild(0))) {
                    impNode = new ImpNode();
                    impNode.addChild(othImp.getChild(0));
                    impNode.addChild(_impNode.getChild(1));
                }

                // If we found one, then we add it.
                if (impNode != null) {
                    _parent.setFlags(NDFlag.HS);
                    othNdWffTree.setFlags(NDFlag.HS);
                    this.addPremise(new NDWffTree(impNode, NDStep.HS, _parent, othNdWffTree));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param _bicondTree
     * @param _parent
     * @return true if we apply a biconditional elimination rule, false otherwise.
     */
    protected boolean findBiconditionalElimination(WffTree _bicondTree, NDWffTree _parent) {
        if (!_parent.isBCActive()) {
            _parent.setFlags(NDFlag.BC);
            ImpNode impLhs = new ImpNode();
            ImpNode impRhs = new ImpNode();
            impLhs.addChild(_bicondTree.getChild(0));
            impLhs.addChild(_bicondTree.getChild(1));
            impRhs.addChild(_bicondTree.getChild(1));
            impRhs.addChild(_bicondTree.getChild(0));
            this.addPremise(new NDWffTree(impLhs, NDStep.BCE, _parent));
            this.addPremise(new NDWffTree(impRhs, NDStep.BCE, _parent));
            return true;
        }

        return false;
    }

    /**
     * @param _binopTree
     * @param _parent
     * @return true if we apply a De Morgan's rule, false otherwise.
     */
    protected boolean findDeMorganEquivalence(WffTree _binopTree, NDWffTree _parent) {
        if (!_parent.isDEMActive() && !this.isConclusion(_parent)) {
            // Two types: one is ~(X B Y) => (~X ~B ~Y)
            WffTree deMorganNode = null;
            if (_binopTree.isNegation() && (_binopTree.getChild(0).isOr() || _binopTree.getChild(0).isAnd() || _binopTree.getChild(0).isImp())) {
                deMorganNode = BaseTruthTreeGenerator.getNegatedBinaryNode(_binopTree.getChild(0)); // B
                deMorganNode.addChild(_binopTree.getChild(0).isImp() ? _binopTree.getChild(0).getChild(0)
                        : BaseTruthTreeGenerator.getFlippedNode(_binopTree.getChild(0).getChild(0))); // LHS X
                deMorganNode.addChild(BaseTruthTreeGenerator.getFlippedNode(_binopTree.getChild(0).getChild(1))); // RHS Y
            }
            // Other is (X B Y) => ~(~X ~B ~Y)
            else if ((_binopTree.isOr() || _binopTree.isAnd() || _binopTree.isImp())) {
                WffTree negBinaryNode = BaseTruthTreeGenerator.getNegatedBinaryNode(_binopTree); // B
                negBinaryNode.addChild(BaseTruthTreeGenerator.getFlippedNode(_binopTree.getChild(0))); // LHS X
                negBinaryNode.addChild(BaseTruthTreeGenerator.getFlippedNode(_binopTree.getChild(1))); // RHS Y
                deMorganNode = new NegNode();
                deMorganNode.addChild(negBinaryNode);
            }
            // If we found a node, then it'll be applied/inserted here.
            if (deMorganNode != null && isGoal(deMorganNode)) {
                _parent.setFlags(NDFlag.DEM);
                this.addPremise(new NDWffTree(deMorganNode, NDFlag.DEM, NDStep.DEM, _parent));
                return true;
            }
        }
        return false;
    }

    /**
     * @param _binopNode
     * @param _parent
     * @return true if we apply a material implication rule, false otherwise.
     */
    protected boolean findMaterialImplication(WffTree _binopNode, NDWffTree _parent) {
        if (!_parent.isMIActive() && !this.isConclusion(_parent)) {
            WffTree newWff = null;
            // Convert (P -> Q) to (~P V Q).
            if (_binopNode.isImp()) {
                OrNode orNode = new OrNode();
                NegNode negLhs = new NegNode();
                negLhs.addChild(_binopNode.getChild(0));
                orNode.addChild(negLhs);
                orNode.addChild(_binopNode.getChild(1));
                newWff = orNode;
            }
            // Convert (~P V Q) to (P -> Q)
            else if (_binopNode.isOr()) {
                WffTree lhs = _binopNode.getChild(0);
                WffTree rhs = _binopNode.getChild(1);
                if (lhs.isNegation()) {
                    ImpNode impNode = new ImpNode();
                    impNode.addChild(lhs.getChild(0)); // Un-negate the lhs.
                    impNode.addChild(rhs);
                    newWff = impNode;
                }
            }
            // If we performed a MI then add it.
            if (newWff != null && isGoal(newWff)) {
                _parent.setFlags(NDFlag.MI);
                NDWffTree ndWffTree = new NDWffTree(newWff, NDFlag.MI, NDStep.MI, _parent);
                this.addPremise(ndWffTree);
                return true;
            }
        }

        return false;
    }

    /**
     * Finds contradictions in the premises. A contradiction, like with truth trees, occurs when we have a wff w1, and
     * somewhere in the premises, ~w1 also exists. This derives a contradiction and then concludes our, well, conclusion!
     * This is used in indirect proofs.
     *
     * @return true if a contradiction is found (and thus the proof is complete), false otherwise.
     */
    protected boolean findContradictions() {
        for (int i = 0; i < this.PREMISES_LIST.size(); i++) {
            for (int j = i + 1; j < this.PREMISES_LIST.size(); j++) {
                if (i != j) {
                    NDWffTree wffOne = this.PREMISES_LIST.get(i);
                    NDWffTree wffTwo = this.PREMISES_LIST.get(j);
                    // Compute the negated of one of the nodes and see if they're equivalent.
                    if (BaseTruthTreeGenerator.getFlippedNode(wffOne.getWffTree()).stringEquals(wffTwo.getWffTree())) {
                        NDWffTree falseNode = new NDWffTree(new FalseNode(), NDFlag.ACTIVE, NDStep.RI, wffOne, wffTwo);
                        NDWffTree conclusionNode = new NDWffTree(this.CONCLUSION_WFF.getWffTree(), NDFlag.ACTIVE, NDStep.RE, falseNode);
                        // Assign this as the conclusion node.
                        this.addPremise(falseNode);
                        this.CONCLUSION_WFF.setDerivationStep(conclusionNode.getDerivationStep());
                        this.CONCLUSION_WFF.setDerivedParents(conclusionNode.getDerivedParents());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param _conclusionNode
     */
    protected void activateLinks(NDWffTree _conclusionNode) {
        if (_conclusionNode == null) { return; }
        _conclusionNode.setActive(true);
        for (NDWffTree ndWffTree : _conclusionNode.getDerivedParents()) {
            this.activateLinks(ndWffTree);
        }
    }

    /**
     * Attempts to add a premise (NDWffTree) to our running list of premises. A premise is NOT added if there is already
     * an identical premise in the tree OR it's a "redundant node". The definition for that is below.
     *
     * @param _ndWffTree NDWffTree to insert as a premise.
     */
    protected void addPremise(NDWffTree _ndWffTree) {
        // THIS NEEDS TO BE ADAPTED TO WORK WITH CONTRADICTIONS SINCE THOSE WILL FAIL!!!!!!!
        if (!this.PREMISES_LIST.contains(_ndWffTree) && !this.isRedundantTree(_ndWffTree)) {
            this.PREMISES_LIST.add(_ndWffTree);
        }
    }

    /**
     * @param _wffTree
     * @return true if _wffTree is a goal, false otherwise.
     */
    protected boolean isGoal(WffTree _wffTree) {
        for (NDWffTree ndWffTree : this.PREMISES_LIST) {
            if (_wffTree.stringEquals(ndWffTree.getWffTree())) {
                return true;
            }
        }
        return _wffTree.stringEquals(this.CONCLUSION_WFF.getWffTree());
    }

    /**
     * A tree is "redundant" if it implies a tautology. For instance, (A & A), (A V A), (A -> A), (A <-> A), etc.
     *
     * @param _ndWffTree NDWffTree to check.
     * @return true if the node is redundant, false otherwise.
     */
    protected boolean isRedundantTree(NDWffTree _ndWffTree) {
        return _ndWffTree.getWffTree().isBinaryOp()
                && _ndWffTree.getWffTree().getChild(0).stringEquals(_ndWffTree.getWffTree().getChild(1));
    }

    /**
     * @param _tree
     * @return NDWffTree object with _tree as its WffTree instance, null if it is not a current premise.
     */
    protected NDWffTree getPremiseNDWffTree(WffTree _tree) {
        for (int i = 0; i < this.PREMISES_LIST.size(); i++) {
            NDWffTree ndWffTree = this.PREMISES_LIST.get(i);
            if (ndWffTree.getWffTree().stringEquals(_tree)) {
                return ndWffTree;
            }
        }
        return null;
    }

    /**
     * Looks through our list of premises to determine if we have found the conclusion yet. We also assign the
     * derived parents of that node and the derivation step to the conclusion wff object (since they aren't the same
     * reference). This is used in the activateLinks method.
     *
     * @return true if the premise list has the conclusion, false otherwise.
     */
    protected boolean findConclusion() {
        for (NDWffTree ndWffTree : this.PREMISES_LIST) {
            if (ndWffTree.getWffTree().stringEquals(this.CONCLUSION_WFF.getWffTree())) {
                this.CONCLUSION_WFF.setActive(true);
                this.CONCLUSION_WFF.setDerivedParents(ndWffTree.getDerivedParents());
                this.CONCLUSION_WFF.setDerivationStep(ndWffTree.getDerivationStep());
                return true;
            }
        }
        return false;
    }

    /**
     * @param _ndWffTree
     * @return true if the NDWffTree is the conclusion, false otherwise.
     */
    protected boolean isConclusion(NDWffTree _ndWffTree) {
        return this.CONCLUSION_WFF.getWffTree().stringEquals(_ndWffTree.getWffTree())
                || this.CONCLUSION_WFF == _ndWffTree;
    }
}
