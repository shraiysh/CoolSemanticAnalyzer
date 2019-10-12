package cool;

import java.util.*;

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
    public void visit(AST.program program_node) {

        ClassGraph.Node rootNode = graph.getNode("Object");

        updateFeaturelistDFS(rootNode);
        visitClassesDFS(rootNode);

        if (!graph.hasClass("Main")) {
            ErrorHandler.reportError("No file", -1, "Main class absent in program.");
        } else {
            ClassGraph.Node m = graph.getNode("Main");
            if (m.getMethod("main") == null) {
                ErrorHandler.reportError(m.getAstClass().filename, m.getAstClass().lineNo, 
                                            "main method absent inside Main class.");
            } else {
                if( m.getMethod("main").formals.size() != 0 ) {
                    ErrorHandler.reportError(m.getAstClass().filename, m.getAstClass().lineNo,
                                         "main method in Main class has non zero arguments.");
                }
            }
        }

    }

    /**
     * Generated a fake id for the recovery routines.
     * 
     * @return fake id
     */
    private String generateNewId() {
        n += 1;
        return n.toString();
    }

    /**
     * validates typeid. Sets them to Object in case of error.
     * 
     * @param typeid
     * @param lineNo
     * @return
     */
    private String validateType(String typeid, int lineNo) {
        if (!graph.hasClass(typeid)) {
            ErrorHandler.reportError(currClass.filename, lineNo,
                    "No type named " + typeid + ". Recovery by setting it to Object.");
            return "Object";
        }
        return typeid;
    }

    /**
     * Validates the method signature and implements several recovery routines in
     * case of error.
     * 
     * @param mthd
     */
    private void validateMethodSignature(AST.method mthd) {
        if (mthd.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, mthd.lineNo,
                    "Method can't have name 'self'. Recovery by setting fake id.");
            mthd.name = generateNewId();
        }

        mthd.typeid = validateType(mthd.typeid, mthd.lineNo);

        for (AST.formal fm : mthd.formals) {

            if (fm.name.equals("self")) {
                ErrorHandler.reportError(currClass.filename, fm.lineNo,
                        "Formal can't have name 'self'. Recovery by setting fake id.");
                fm.name = generateNewId();
            }
            fm.typeid = validateType(fm.typeid, fm.lineNo);
        }
    }

    private boolean isSameMethodSignature(AST.method a, AST.method b) {
        if (a.typeid != b.typeid || a.formals.size() != b.formals.size()) {
            ErrorHandler.reportError(currClass.filename, b.lineNo,
                    "Method signature doesn't match. Recovery by skipping it.");
            return false;
        }
        for (int i = 0; i < a.formals.size(); ++i) {
            if (a.formals.get(i).typeid != b.formals.get(i).typeid) {
                ErrorHandler.reportError(currClass.filename, b.lineNo,
                        "Method signature doesn't match. Recovery by skipping it.");
                return false;
            }
        }
        return true;
    }

    private void updateFeaturelistDFS(ClassGraph.Node node) {

        this.currClass = node.getAstClass();
        List<AST.feature> ftList = node.getAstClass().features;
        ClassGraph.Node parentNode = node.getParentNode();
        for (AST.feature ft : node.getAstClass().features) {
            if (ft instanceof AST.method) {
                AST.method mthd = (AST.method) ft;
                validateMethodSignature(mthd);
                //                                      hax for handling Object
                AST.method parentMthd = ( parentNode == null ? null : parentNode.getMethod(mthd.name) );

                if (parentMthd != null) { // Attempt to redefine parent method
                    if (!isSameMethodSignature(parentMthd, mthd)) { // Incorrect redfinition
                        ftList.remove(ft); // remove this method if it is incompatible.
                    }
                } else { // fresh method defintion
                    node.methods.put(mthd.name, mthd);
                }
            }
        }
        // hax : i'm putting in the parent's 'AST.method' as value. But shouldn't matter
        // as we only care about signature.
        if(parentNode != null) node.methods.putAll(parentNode.methods);

        for (ClassGraph.Node ch : node.getChildNodes()) {
            updateFeaturelistDFS(ch);
        }
    }

    private void visitClassesDFS(ClassGraph.Node node) {
                
        for (ClassGraph.Node ch : node.getChildNodes()) {
            objScopeTable.enterScope();
            ch.getAstClass().accept(this);
            visitClassesDFS(ch);
            objScopeTable.exitScope();
        }

    }

    @Override
    public void visit(AST.class_ class__node) {
        objScopeTable.insert("self", class__node.name);

        for(AST.feature ft : class__node.features) if(ft instanceof AST.attr){
            AST.attr at = (AST.attr)ft;
            if(at.name.equals("self")) {
                ErrorHandler.reportError(currClass.filename, at.lineNo, "Attribute can't have name 'self'. "
                                                                        + "Recovery by skipping this one.");
            } else if (objScopeTable.lookUpGlobal(at.name) != null) {
                ErrorHandler.reportError(currClass.filename, at.lineNo, "Attribute "+at.name+" has been redefined. "
                                                                        + "Recovery by skipping this one.");
            } else { // good to go with this one
                at.accept(this);
            }
        }

        for(AST.feature ft : class__node.features) if(ft instanceof AST.method) {
            ft.accept(this);
        }
    }


    @Override
    public void visit(AST.attr attr_node) {
        if(attr_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, attr_node.lineNo, "Attribute can't have name 'self'. "
                                                                            +"Recovery by skipping this one.");
        } else if (!graph.hasClass(attr_node.name)) {
            attr_node.typeid = validateType(attr_node.typeid, attr_node.lineNo);
            objScopeTable.insert(attr_node.name, attr_node.typeid);
            attr_node.value.accept(this);
        } else {
            objScopeTable.insert(attr_node.name, attr_node.typeid);
            attr_node.value.accept(this);

            // If assignment has been done && doesn't conform
            if(!(attr_node.value instanceof AST.no_expr) && !graph.isAncestor(attr_node.typeid, attr_node.value.type)) {
                ErrorHandler.reportError(currClass.filename, attr_node.lineNo, "Expression doesnt conform to type of Attribute.");
            }
        }
    }


    @Override
    public void visit(AST.method method_node) {
        objScopeTable.enterScope();

        for(AST.formal fm : method_node.formals) {
            fm.accept(this);
        }

        objScopeTable.exitScope();
    }
    
    @Override
    public void visit(AST.formal formal_node) {
        if (formal_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, formal_node.lineNo, "Formal can't have name 'self'");
        } else if (objScopeTable.lookUpLocal(formal_node.name) != null) {
            ErrorHandler.reportError(currClass.filename, formal_node.lineNo, "Formal " + formal_node.name
                                                                                + " has multiple declarations.");
        } else {
            formal_node.typeid = validateType(formal_node.typeid, formal_node.lineNo);
            objScopeTable.insert(formal_node.name, formal_node.typeid);
        }
    }
    
    
    @Override
    public void visit(AST.branch branch_node) {
        objScopeTable.enterScope();

        if(branch_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, branch_node.lineNo, "Can't bind to 'self' in Case.");
        }
        branch_node.type = validateType(branch_node.type, branch_node.lineNo);

        branch_node.value.accept(this);
        objScopeTable.exitScope();
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