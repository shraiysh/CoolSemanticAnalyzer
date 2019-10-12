package cool;


interface ASTVisitor<T> {
    
    public T visit(AST.program      program_node);
    public T visit(AST.class_       class__node);
    
    public T visit(AST.attr         attr_node);
    public T visit(AST.method       method_node);
    
    public T visit(AST.branch       branch_node);
    public T visit(AST.formal       formal_node);

    
    public T visit(AST.no_expr      no_expr_node);
    public T visit(AST.bool_const   bool_const_node);
    public T visit(AST.string_const string_const_node);
    public T visit(AST.int_const    int_const_node);
    public T visit(AST.object       object_node);
    public T visit(AST.comp         comp_node);
    public T visit(AST.eq           eq_node);
    public T visit(AST.leq          leq_node);
    public T visit(AST.lt           lt_node);
    public T visit(AST.neg          neg_node);
    public T visit(AST.divide       divide_node);
    public T visit(AST.mul          mul_node);
    public T visit(AST.sub          sub_node);
    public T visit(AST.plus         plus_node);
    public T visit(AST.isvoid       isvoid_node);
    public T visit(AST.new_         new__node);
    public T visit(AST.assign       assign_node);
    public T visit(AST.block        block_node);
    public T visit(AST.loop         loop_node);
    public T visit(AST.cond         cond_node);
    public T visit(AST.let          let_node);
    public T visit(AST.dispatch     dispatch_node);
    public T visit(AST.typcase      typcase_node);
    public T visit(AST.static_dispatch static_dispatch_node);

}