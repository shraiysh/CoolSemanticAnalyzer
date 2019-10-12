package cool;

import cool.AST.class_;
import cool.AST.method;

// import cool.AST.program;

public class InfoGatherPass extends ASTBaseVisitor {

    ClassGraph graph;

    public InfoGatherPass(ClassGraph graph) {
        // graph = new ClassGraph();
        this.graph = graph;
    }

    @Override
    public void visit(AST.program program_node) {
        // TODO Auto-generated method stub
        super.visit(program_node);
        graph.analyze();
    }

    @Override
    public void visit(class_ class__node) {
        graph.addClass(class__node);
        super.visit(class__node);
    }

    @Override
    public void visit(method method_node) {
        // TODO Auto-generated method stub
        // return super.visit(method_node);
    }

}