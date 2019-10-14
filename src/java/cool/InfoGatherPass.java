package cool;


// import cool.AST.program;

public class InfoGatherPass extends ASTBaseVisitor {

    ClassGraph graph;

    public InfoGatherPass(ClassGraph graph) {
        this.graph = graph;
    }

    @Override
    public void visit(AST.program program_node) {
        super.visit(program_node);
        graph.analyze();
    }

    @Override
    public void visit(AST.class_ class__node) {
        graph.addClass(class__node);
    }

}