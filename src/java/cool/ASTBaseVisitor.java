package cool;


public class ASTBaseVisitor<T> implements ASTVisitor<T> {

    public T visit(AST.program program_node) {
        for (AST.class_ cl : program_node.classes)
            cl.accept(this);
        return null;
    }

    public T visit(AST.class_ class__node) {
        for (AST.feature f : class__node.features)
            f.accept(this);
        return null;
    }

    public T visit(AST.attr attr_node) {
        attr_node.value.accept(this);
        return null;
    }

    public T visit(AST.method method_node) {
        method_node.body.accept(this);
        return null;
    }

    public T visit(AST.branch branch_node) {
        branch_node.value.accept(this);
        return null;
    }

    public T visit(AST.formal formal_node) {
        return null;
    }

    public T visit(AST.no_expr no_expr_node) {
        return null;
    }

    public T visit(AST.bool_const bool_const_node) {
        return null;
    }

    public T visit(AST.string_const string_const_node) {
        return null;
    }

    public T visit(AST.int_const int_const_node) {
        return null;
    }

    public T visit(AST.object object_node) {
        return null;
    }

    public T visit(AST.comp comp_node) {
        comp_node.e1.accept(this);
        return null;
    }

    public T visit(AST.eq eq_node) {
        eq_node.e1.accept(this);
        eq_node.e2.accept(this);
        return null;
    }

    public T visit(AST.leq leq_node) {
        leq_node.e1.accept(this);
        leq_node.e2.accept(this);

        return null;
    }

    public T visit(AST.lt lt_node) {
        lt_node.e1.accept(this);
        lt_node.e2.accept(this);
        return null;
    }

    public T visit(AST.neg neg_node) {
        neg_node.e1.accept(this);
        return null;
    }

    public T visit(AST.divide divide_node) {
        divide_node.e1.accept(this);
        divide_node.e2.accept(this);
        return null;
    }

    public T visit(AST.mul mul_node) {
        mul_node.e1.accept(this);
        mul_node.e2.accept(this);
        return null;
    }

    public T visit(AST.sub sub_node) {
        sub_node.e1.accept(this);
        sub_node.e2.accept(this);
        return null;
    }

    public T visit(AST.plus plus_node) {
        plus_node.e1.accept(this);
        plus_node.e2.accept(this);
        return null;
    }

    public T visit(AST.isvoid isvoid_node) {
        isvoid_node.e1.accept(this);
        return null;
    }

    public T visit(AST.new_ new__node) {
        return null;
    }

    public T visit(AST.assign assign_node) {
        assign_node.e1.accept(this);
        return null;
    }

    public T visit(AST.block block_node) {
        for (AST.expression exp : block_node.l1)
            exp.accept(this);
        return null;
    }

    public T visit(AST.loop loop_node) {
        loop_node.predicate.accept(this);
        loop_node.body.accept(this);
        return null;
    }

    public T visit(AST.cond cond_node) {
        cond_node.predicate.accept(this);
        cond_node.ifbody.accept(this);
        cond_node.elsebody.accept(this);
        return null;
    }

    public T visit(AST.let let_node) {
        let_node.value.accept(this);
        let_node.body.accept(this);
        return null;
    }

    public T visit(AST.dispatch dispatch_node) {
        dispatch_node.caller.accept(this);
        for (AST.expression exp : dispatch_node.actuals)
            exp.accept(this);
        return null;
    }

    public T visit(AST.typcase typcase_node) {
        typcase_node.predicate.accept(this);
        for (AST.branch br : typcase_node.branches)
            br.accept(this);
        return null;
    }

    public T visit(AST.static_dispatch static_dispatch_node) {
        static_dispatch_node.caller.accept(this);
        for (AST.expression exp : static_dispatch_node.actuals)
            exp.accept(this);
        return null;
    }
}