package cool;

import java.util.List;

import cool.AST.class_;
import cool.AST.method;
import cool.ClassGraph.Node;

// import cool.AST.program;

public class SemanticCheckPass extends ASTBaseVisitor {

    ClassGraph graph;
    AST.class_ currClass;
    ScopeTable<String> objScopeTable;
    Integer n;

    public SemanticCheckPass(ClassGraph graph) {
        // graph = new ClassGraph();
        this.graph = graph;
        this.objScopeTable = new ScopeTable<>();
        this.n = 1;
    }

    @Override
    public Object visit(AST.program program_node) {
        // TODO Auto-generated method stub

        ClassGraph.Node rootNode = graph.getNode("Object");

        updateFeaturelistDFS(rootNode);
        visitClassesDFS(rootNode);
        // @check for Main and main

    }


    /**
     * Generated a fake id for the recovery routines.
     * @return fake id
     */
    private String generateNewId() {
        n+=1;
        return n.toString();
    }

    /**
     * validates typeid. Sets them to Object in case of error.
     * @param typeid
     * @param lineNo
     * @return
     */
    private String validateType(String typeid, int lineNo) {
        if(!graph.hasClass(typeid))  {
            ErrorHandler.reportError(currClass.filename, lineNo, "No type named "+typeid+". Recovery by setting it to Object.");
            return "Object";
        }
        return typeid;
    }

    /**
     * Validates the method signature and implements several recovery routines in case of error.
     * @param mthd
     */
    private void validateMethodSignature(AST.method mthd) {
        if(mthd.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, mthd.lineNo, "Method can't have name 'self'. Recovery by setting fake id.");
            mthd.name = generateNewId();
        }

        mthd.typeid = validateType(mthd.typeid, mthd.lineNo);

        for(AST.formal fm: mthd.formals) {

            if(fm.name.equals("self")) {
                ErrorHandler.reportError(currClass.filename, fm.lineNo, "Formal can't have name 'self'. Recovery by setting fake id.");
                fm.name = generateNewId();
            }
            fm.typeid = validateType(fm.typeid, fm.lineNo);
        }
    }

    private boolean isSameMethodSignature(AST.method a, AST.method b) {
        if( a.typeid!=b.typeid ||  a.formals.size()!=b.formals.size() ) {
            ErrorHandler.reportError(currClass.filename, b.lineNo, "Method signature doesn't match. Recovery by skipping it.");
            return false;
        }
        for( int i = 0; i < a.formals.size(); ++i) {
            if(a.formals.get(i).typeid != b.formals.get(i).typeid) {
                ErrorHandler.reportError(currClass.filename, b.lineNo, "Method signature doesn't match. Recovery by skipping it.");
                return false;
            }
        }
        return true;
    }


    private void updateFeaturelistDFS(Node node) {

        this.currClass = node.getAstClass();
        List<AST.feature> ftList = node.getAstClass().features;

        for (AST.feature ft : node.getAstClass().features) {
            if(ft instanceof AST.method) {
                AST.method mthd = (AST.method)ft;
                validateMethodSignature(mthd);
                AST.method parentMthd = node.getParentNode().getMethod(mthd.name);

                if(parentMthd != null) { // Attempt to redefine parent method
                    if(!isSameMethodSignature(parentMthd, mthd)) { // Incorrect redfinition
                        ftList.remove(ft);
                    }
                } else { // fresh method defintion
                    node.methods.put(mthd.name, mthd);
                }
            }
        }

        // hax : i'm putting in the parent's 'AST.method' as value. But shouldn't matter as we only care about signature.
        node.methods.putAll(node.getParentNode().methods);

    }

    private void visitClassesDFS(Node node) {
        objScopeTable.enterScope();

        node.getAstClass().accept(this);

        for (ClassGraph.Node ch : node.getChildNodes()) {
            visitClassesDFS(ch);
        }

        objScopeTable.exitScope();

    }

    @Override
    public Object visit(class_ class__node) {
        graph.addClass(class__node);
        return super.visit(class__node);
    }

    @Override
    public Object visit(method method_node) {
        // TODO Auto-generated method stub
        // return super.visit(method_node);

    }



    // For Shrey --- below

    @Override
    public void visit(AST.no_expr no_expr_node) {
        ;
    }

    @Override
    public void visit(AST.bool_const bool_const_node) {

    }

    @Override
    public void visit(AST.string_const string_const_node) {

    }

    @Override
    public void visit(AST.int_const int_const_node) {

    }

    @Override
    public void visit(AST.object object_node) {

    }

    @Override
    public void visit(AST.comp comp_node) {
        comp_node.e1.accept(this);

    }

    @Override
    public void visit(AST.eq eq_node) {
        eq_node.e1.accept(this);
        eq_node.e2.accept(this);

    }

    @Override
    public void visit(AST.leq leq_node) {
        leq_node.e1.accept(this);
        leq_node.e2.accept(this);


    }

    @Override
    public void visit(AST.lt lt_node) {
        lt_node.e1.accept(this);
        lt_node.e2.accept(this);

    }

    @Override
    public void visit(AST.neg neg_node) {
        neg_node.e1.accept(this);

    }

    @Override
    public void visit(AST.divide divide_node) {
        divide_node.e1.accept(this);
        divide_node.e2.accept(this);

    }

    @Override
    public void visit(AST.mul mul_node) {
        mul_node.e1.accept(this);
        mul_node.e2.accept(this);

    }

    @Override
    public void visit(AST.sub sub_node) {
        sub_node.e1.accept(this);
        sub_node.e2.accept(this);

    }

    @Override
    public void visit(AST.plus plus_node) {
        plus_node.e1.accept(this);
        plus_node.e2.accept(this);

    }

    @Override
    public void visit(AST.isvoid isvoid_node) {
        isvoid_node.e1.accept(this);

    }

    @Override
    public void visit(AST.new_ new__node) {

    }

    @Override
    public void visit(AST.assign assign_node) {
        assign_node.e1.accept(this);

    }

    @Override
    public void visit(AST.block block_node) {
        for (AST.expression exp : block_node.l1)
            exp.accept(this);

    }

    @Override
    public void visit(AST.loop loop_node) {
        loop_node.predicate.accept(this);
        loop_node.body.accept(this);

    }

    @Override
    public void visit(AST.cond cond_node) {
        cond_node.predicate.accept(this);
        cond_node.ifbody.accept(this);
        cond_node.elsebody.accept(this);

    }

    @Override
    public void visit(AST.let let_node) {
        let_node.value.accept(this);
        let_node.body.accept(this);

    }

    @Override
    public void visit(AST.dispatch dispatch_node) {
        dispatch_node.caller.accept(this);
        for (AST.expression exp : dispatch_node.actuals)
            exp.accept(this);

    }

    @Override
    public void visit(AST.typcase typcase_node) {
        typcase_node.predicate.accept(this);
        for (AST.branch br : typcase_node.branches)
            br.accept(this);

    }

    @Override
    public void visit(AST.static_dispatch static_dispatch_node) {
        static_dispatch_node.caller.accept(this);
        for (AST.expression exp : static_dispatch_node.actuals)
            exp.accept(this);

    }

}