package cool;

import java.util.*;

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
        this.objScopeTable = new ScopeTable<String>();
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
        for (int i = 0; i < ftList.size(); i++) {
            AST.feature ft = ftList.get(i);
            if (ft instanceof AST.method) {
                AST.method mthd = (AST.method) ft;
                validateMethodSignature(mthd);
                //                                      hax for handling Object
                AST.method parentMthd = ( parentNode == null ? null : parentNode.getMethod(mthd.name) );

                if (parentMthd != null) { // Attempt to redefine parent method
                    if (!isSameMethodSignature(parentMthd, mthd)) { // Incorrect redfinition
                        ftList.remove(ft); // remove this method if it is incompatible.
                        i--;
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
            this.currClass = ch.getAstClass();
            objScopeTable.enterScope();
            ch.getAstClass().accept(this);
            visitClassesDFS(ch);
            objScopeTable.exitScope();
        }

    }

    @Override
    public void visit(AST.class_ class__node) {

        System.out.println("Visiting class " + class__node.lineNo);

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

        System.out.println("Visiting attr " + attr_node.lineNo);

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

        System.out.println("Visiting method " + method_node.lineNo);

        objScopeTable.enterScope();

        for(AST.formal fm : method_node.formals) {
            fm.accept(this);
        }

        objScopeTable.exitScope();
    }
    
    @Override
    public void visit(AST.formal formal_node) {

        System.out.println("Visiting formal " + formal_node.lineNo);

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

        System.out.println("Visiting branch " + branch_node.lineNo);

        objScopeTable.enterScope();

        if(branch_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, branch_node.lineNo, "Can't bind to 'self' in Case.");
        }
        branch_node.type = validateType(branch_node.type, branch_node.lineNo);

        objScopeTable.insert(branch_node.name, branch_node.type);

        branch_node.value.accept(this);
        objScopeTable.exitScope();
    }

    // For Shrey --- below

    @Override
    public void visit(AST.no_expr no_expr_node) {
        System.out.println("Visiting no_epxr " + no_expr_node.lineNo);

        no_expr_node.type = "_no_type";
    }

    @Override
    public void visit(AST.bool_const bool_const_node) {
        System.out.println("Visiting bool_const " + bool_const_node.lineNo);

        bool_const_node.type = "Bool";
    }

    @Override
    public void visit(AST.string_const string_const_node) {
        System.out.println("Visiting string_const " + string_const_node.lineNo);

        string_const_node.type = "String";
    }

    @Override
    public void visit(AST.int_const int_const_node) {
        System.out.println("Visiting inst_const " + int_const_node.lineNo);

        int_const_node.type = "Int";
    }

    @Override
    public void visit(AST.object object_node) {

        System.out.println("Visiting object " + object_node.lineNo);


        String type = objScopeTable.lookUpGlobal(object_node.name);
        if(object_node.name.equals("self")) {
            object_node.type = currClass.name;
        }
        else if(type != null){
            object_node.type = type;
        }
        else {
            // RECOVERY : Give it the object type
            object_node.type = "Object";
            ErrorHandler.reportError(currClass.filename, object_node.lineNo, "Undeclared identifier " + object_node.name);
        }
    }

    @Override
    public void visit(AST.comp comp_node) {

        System.out.println("Visiting comp_node " + comp_node.lineNo);

        comp_node.e1.accept(this);
        if(!comp_node.e1.type.equals("Bool")) {
            ErrorHandler.reportError(currClass.filename, comp_node.lineNo, "Argument of 'not' has type " 
                + comp_node.e1.type  + " instead of Bool.");
        }

        // Normal and RECOVERY : Give it Bool
        comp_node.type = "Bool";
    }

    @Override
    public void visit(AST.eq eq_node) {
        System.out.println("Visiting eq_node " + eq_node.lineNo);

        eq_node.e1.accept(this);
        eq_node.e2.accept(this);

        if(!eq_node.e1.type.equals(eq_node.e2.type)) {
            // Different types

            // Check int string bool
            boolean first = eq_node.e1.type == "Int" ||
                            eq_node.e1.type == "String" ||
                            eq_node.e1.type == "Bool"
                            ;

            boolean second = eq_node.e2.type == "Int" ||
                             eq_node.e2.type == "String" ||
                             eq_node.e2.type == "Bool"
                             ;

            if(first || second) {
                ErrorHandler.reportError(currClass.filename, eq_node.lineNo,
                    "Illegal comparison with a basic type.");
            }
        }

        // Normal or RECOVERY : Give it Bool
        eq_node.type = "Bool";

    }

    boolean checkIntAndReport(AST.expression e1, AST.expression e2, String opr, int lineNo) {
        boolean res = !e1.type.equals("Int") || !e2.type.equals("Int");
        if(res) {
            ErrorHandler.reportError(currClass.filename, lineNo,
                "non-Int arguments: " + e1.type + " " + opr +" " + e2.type);
        }
        return res;
    }

    @Override
    public void visit(AST.leq leq_node) {
        System.out.println("Visiting leq_node " + leq_node.lineNo);

        leq_node.e1.accept(this);
        leq_node.e2.accept(this);

        checkIntAndReport(leq_node.e1, leq_node.e2, "<=", leq_node.lineNo);
        // Error or RECOVERY : Give it Bool
        leq_node.type = "Bool";

    }

    @Override
    public void visit(AST.lt lt_node) {
        System.out.println("Visiting lt_node " + lt_node.lineNo);

        lt_node.e1.accept(this);
        lt_node.e2.accept(this);

        checkIntAndReport(lt_node.e1, lt_node.e2, "<", lt_node.lineNo);
        // Error or RECOVERY : Give it Bool
        lt_node.type = "Bool";
    }

    @Override
    public void visit(AST.neg neg_node) {
        System.out.println("Visiting neg_node " + neg_node.lineNo);

        neg_node.e1.accept(this);
        if(!neg_node.e1.type.equals("Int")) {
            ErrorHandler.reportError(currClass.filename, neg_node.lineNo,
                "Argument of '~' has type " + neg_node.e1.type + " instead of Int.");
        }
        neg_node.type = "Int";
    }

    @Override
    public void visit(AST.divide divide_node) {

        System.out.println("Visiting divide_node " + divide_node.lineNo);


        divide_node.e1.accept(this);
        divide_node.e2.accept(this);

        checkIntAndReport(divide_node.e1, divide_node.e2, "/", divide_node.lineNo);
        // Error or RECOVERY : Give it Int
        divide_node.type = "Int";
    }

    @Override
    public void visit(AST.mul mul_node) {

        System.out.println("Visiting mul_node " + mul_node.lineNo);

        mul_node.e1.accept(this);
        mul_node.e2.accept(this);


        checkIntAndReport(mul_node.e1, mul_node.e2, "*", mul_node.lineNo);
        // Error or RECOVERY : Give it Int
        mul_node.type = "Int";

    }

    @Override
    public void visit(AST.sub sub_node) {
        System.out.println("Visiting sub_node " + sub_node.lineNo);

        sub_node.e1.accept(this);
        sub_node.e2.accept(this);


        checkIntAndReport(sub_node.e1, sub_node.e2, "-", sub_node.lineNo);
        // Error or RECOVERY : Give it Int
        sub_node.type = "Int";
    }

    @Override
    public void visit(AST.plus plus_node) {

        System.out.println("Visiting plus_node " + plus_node.lineNo);

        plus_node.e1.accept(this);
        plus_node.e2.accept(this);


        checkIntAndReport(plus_node.e1, plus_node.e2, "+", plus_node.lineNo);
        // Error or RECOVERY : Give it Int
        plus_node.type = "Int";

    }

    @Override
    public void visit(AST.isvoid isvoid_node) {
        System.out.println("Visiting isvoid_node " + isvoid_node.lineNo);

        isvoid_node.e1.accept(this);

        isvoid_node.type = "Bool";
    }

    @Override
    public void visit(AST.new_ new__node) {
        System.out.println("Visiting new__node " + new__node.lineNo);

        if(!graph.hasClass(new__node.typeid)) {
            ErrorHandler.reportError(currClass.filename, new__node.lineNo,
                "'new' used with undefined class " + new__node.typeid + ".");
            new__node.type = "Object";
        }
        else {
            new__node.type = new__node.typeid;
        }
    }

    @Override
    public void visit(AST.assign assign_node) {
        System.out.println("Visiting assign_node " + assign_node.lineNo);

        assign_node.e1.accept(this);
    
        String type = objScopeTable.lookUpGlobal(assign_node.name);

        if(assign_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, assign_node.lineNo,
                "Cannot assign to 'self'.");
        }
        else {
            if(type == null) {
                ErrorHandler.reportError(currClass.filename, assign_node.lineNo,
                    "Assignment to undeclared variable " + assign_node.name);
            }
            else if(!graph.isAncestor(type, assign_node.e1.type)) {
                ErrorHandler.reportError(currClass.filename, assign_node.lineNo,
                    "Type " + assign_node.e1.type + " of assigned expression does not conform to declared type " + type + " of identifier " + assign_node.name + ".");
            }
        }

        // Finally, give it the type of rhs
        assign_node.type = assign_node.e1.type;
    }

    @Override
    public void visit(AST.block block_node) {
        System.out.println("Visiting block_node " + block_node.lineNo);

        for (AST.expression exp : block_node.l1)
            exp.accept(this);

        // Give it the type of last expr
        block_node.type = block_node.l1.get(block_node.l1.size() - 1).type;
    }

    @Override
    public void visit(AST.loop loop_node) {
        System.out.println("Visiting loop_node " + loop_node.lineNo);

        loop_node.predicate.accept(this);
        loop_node.body.accept(this);

        if(!loop_node.predicate.type.equals("Bool")) {
            ErrorHandler.reportError(currClass.filename, loop_node.lineNo,
                "Loop condition does not have type Bool.");
        }

        loop_node.type = "Object";
    }

    @Override
    public void visit(AST.cond cond_node) {
        System.out.println("Visiting cond_node " + cond_node.lineNo);

        cond_node.predicate.accept(this);
        cond_node.ifbody.accept(this);
        cond_node.elsebody.accept(this);

        if(!cond_node.predicate.type.equals("Bool")) {
            ErrorHandler.reportError(currClass.filename, cond_node.lineNo,
                "Loop condition does not have type Bool.");
        }

        cond_node.type = graph.getLCA(cond_node.ifbody.type, cond_node.elsebody.type);

    }

    @Override
    public void visit(AST.let let_node) {
        System.out.println("Visiting let_node " + let_node.lineNo);

        let_node.body.accept(this);

        objScopeTable.enterScope();

        if(let_node.name.equals("self")) {
            ErrorHandler.reportError(currClass.filename, let_node.lineNo,
                "'self' cannot be bound in a let expression.");
        }
        else {
            String type = let_node.typeid;
            // Check if class is defined
            if(!graph.hasClass(let_node.typeid)) {
                ErrorHandler.reportError(currClass.filename, let_node.lineNo,
                    "'let' used with undefined class " + let_node.typeid);
                type = "Object";
            }

            objScopeTable.insert(let_node.name, type);

            // There is assignment
            if(!(let_node.value instanceof AST.no_expr)) {
                
                let_node.value.accept(this);

                if(!graph.isAncestor(type, let_node.value.type)) {
                    // Not Conforming
                    ErrorHandler.reportError(currClass.filename, let_node.lineNo,
                        "Type " + let_node.value.type + " of assigned expression does not conform to declared type " + type + " of identifier " + let_node.name + ".");                
                }
            }

        }

        let_node.type = let_node.body.type;

        objScopeTable.exitScope();

    }

    @Override
    public void visit(AST.dispatch dispatch_node) {
        System.out.println("Visiting dispatch_node " + dispatch_node.lineNo);

        dispatch_node.caller.accept(this);
        for (AST.expression exp : dispatch_node.actuals)
            exp.accept(this);

        String classname = dispatch_node.caller.type;
        if(dispatch_node.caller.type.equals("self")) {
            classname = currClass.name;
        }

        // Gives the method with proper signature
        AST.method method = graph.getNode(classname).getMethod(dispatch_node.name);

        System.out.println(currClass.name);

        if(method == null) {
            ErrorHandler.reportError(currClass.filename, dispatch_node.lineNo,
                "Method " + dispatch_node.name + "(...) not a feature of class " + classname);
            dispatch_node.type = "Object";
        }
        else {

            // Check if the conforming calls
            List<AST.expression> actuals = dispatch_node.actuals;
            List<AST.formal> formals = method.formals;

            for(int i = 0; i < actuals.size(); i++) {
                if(i < formals.size()) {
                    if(!graph.isAncestor(formals.get(i).typeid, actuals.get(i).type)) {
                        ErrorHandler.reportError(currClass.filename, dispatch_node.lineNo,
                            "Type mismatch for arg " + formals.get(i).name + 
                            ". Formal : " + formals.get(i).typeid +
                            ", Actual : " + actuals.get(i).type );
                    }
                }
            }
            dispatch_node.type = method.typeid;
        }
    }

    @Override
    public void visit(AST.typcase typcase_node) {
        System.out.println("Visiting typcase_node " + typcase_node.lineNo);

        typcase_node.predicate.accept(this);
        for (AST.branch br : typcase_node.branches)
            br.accept(this);

        // Check no two types same
        List<String> type_list = new ArrayList<String>();
        for(AST.branch branch : typcase_node.branches) {
            if(type_list.contains(branch.type)) {
                // Error
                ErrorHandler.reportError(currClass.name, branch.lineNo,
                    "Two cases should not be same type in case expression");
            }
        }

        typcase_node.branches.get(0).accept(this);
        typcase_node.type = typcase_node.branches.get(0).value.type;

        // accepting and joining types of other branches
        for(int i=1; i<typcase_node.branches.size(); i++) {
            typcase_node.branches.get(i).accept(this);
            typcase_node.type = graph.getLCA(typcase_node.type, typcase_node.branches.get(i).value.type);
        }
    }

    @Override
    public void visit(AST.static_dispatch static_dispatch_node) {
        System.out.println("Visiting static_dispatch_node " + static_dispatch_node.lineNo);

        static_dispatch_node.caller.accept(this);
        for (AST.expression exp : static_dispatch_node.actuals)
            exp.accept(this);

        String classname = static_dispatch_node.caller.type;

        if(!graph.isAncestor(static_dispatch_node.typeid, static_dispatch_node.caller.type)) {
            ErrorHandler.reportError(currClass.name, static_dispatch_node.lineNo,
               "Class " + static_dispatch_node.typeid + " is not an ancestor of the caller type " + static_dispatch_node.caller.type);
        }

        AST.method method = graph.getNode(static_dispatch_node.typeid).getMethod(static_dispatch_node.name);

        if(method == null) {
            ErrorHandler.reportError(currClass.filename, static_dispatch_node.lineNo,
                "Method " + static_dispatch_node.name + "(...) not a feature of class " + classname);
            static_dispatch_node.type = "Object";
        }
        else {

            // Check if the conforming calls
            List<AST.expression> actuals = static_dispatch_node.actuals;
            List<AST.formal> formals = method.formals;

            for(int i = 0; i < actuals.size(); i++) {
                if(i < formals.size()) {
                    if(!graph.isAncestor(formals.get(i).typeid, actuals.get(i).type)) {
                        ErrorHandler.reportError(currClass.filename, static_dispatch_node.lineNo,
                            "Type mismatch for arg " + formals.get(i).name + 
                            ". Formal : " + formals.get(i).typeid +
                            ", Actual : " + actuals.get(i).type);
                    }
                }
            }
            static_dispatch_node.type = method.typeid;
        }
    }

}