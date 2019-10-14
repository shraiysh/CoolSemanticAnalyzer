package cool;


interface ASTVisitor {
    
    public void visit(AST.program      program_node);
    public void visit(AST.class_       class__node);
    
    public void visit(AST.attr         attr_node);
    public void visit(AST.method       method_node);
    
    public void visit(AST.branch       branch_node);
    public void visit(AST.formal       formal_node);

    
    public void visit(AST.no_expr      no_expr_node);
    public void visit(AST.bool_const   bool_const_node);
    public void visit(AST.string_const string_const_node);
    public void visit(AST.int_const    int_const_node);
    public void visit(AST.object       object_node);
    public void visit(AST.comp         comp_node);
    public void visit(AST.eq           eq_node);
    public void visit(AST.leq          leq_node);
    public void visit(AST.lt           lt_node);
    public void visit(AST.neg          neg_node);
    public void visit(AST.divide       divide_node);
    public void visit(AST.mul          mul_node);
    public void visit(AST.sub          sub_node);
    public void visit(AST.plus         plus_node);
    public void visit(AST.isvoid       isvoid_node);
    public void visit(AST.new_         new__node);
    public void visit(AST.assign       assign_node);
    public void visit(AST.block        block_node);
    public void visit(AST.loop         loop_node);
    public void visit(AST.cond         cond_node);
    public void visit(AST.let          let_node);
    public void visit(AST.dispatch     dispatch_node);
    public void visit(AST.typcase      typcase_node);
    public void visit(AST.static_dispatch static_dispatch_node);

}