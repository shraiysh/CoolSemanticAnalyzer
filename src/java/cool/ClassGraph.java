package cool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ClassGraph {

    private Node rootNode;
    private HashMap<String, Node> classNameToNode;

    private List<String> noInheritList;
    int timer;

    /**
     * Constructor
     */
    public ClassGraph() {
        addBasicClasses();
        classNameToNode = new HashMap<>();
        noInheritList   = Arrays.asList("Int", "String", "Bool");
    }


    public List<Node> getNodeList() {
        return new ArrayList<Node>(classNameToNode.values());
    }

    public Node getNode(String className) {
        return classNameToNode.get(className);
    }

    /**
     * Preprocesses and analyzes the class graph.
     * @return  true if successful else false one rror
     */
    public boolean analyze() {

        if (!updateEdges()) return false;

        // @check for main here? continue or not?
        // if( ! hasClass("Main")) {
        //     ;
        // }

        timer = 1;
        runDFS(rootNode, 0);
        
        for(Node nd : getNodeList()) {
            if (nd.inTime == 0) {
                // @error
                // this node is in a cycle.
                return false;
            }
        }
        return true;
    }

    private void runDFS(Node nd, int d) {
        nd.inTime = timer++;
        nd.depth = d;

        for(Node ch : nd.getChildNodes()) {

            // if(ch.inTime < nd.inTime) { // Not possible here. No need to check.
            //     // @error BackEdge "Classes Cyclic Dependency"

            //     return false;
            // }
            runDFS(ch, d+1);
        }
        nd.outTime = timer++;
    }

    private boolean hasClass(String className) {
        return classNameToNode.containsKey(className);
    }

    private boolean updateEdges() {
        for (Node nd : classNameToNode.values()) {

            String parentName = nd.getAstClass().parent;
// @check no parent is null or Object
            if(hasClass(parentName)) {
                Node parentNode = classNameToNode.get(parentName);
                nd.setParentNode(parentNode);
                parentNode.addChild(nd);
            } else {
                // Parent class was not defined.
                // @error
                return true;
            }
        }
        return false;
    }

    private boolean isRestrictedInheritance(AST.class_ astClass) {
        return noInheritList.contains(astClass.parent);
    }

    public boolean isAncestor(Node anc, Node nd) {
        return anc.inTime < nd.inTime && nd.outTime < anc.outTime;
    }

    public boolean isAncestor(String anc, String nd) {
        return isAncestor(getNode(anc), getNode(nd));
    }

    public Node getLCA(Node a, Node b) {
        while(!isAncestor(a, b))
            a = a.getParentNode();
        return a;
    }
    public Node getLCANode(String a, String b) {
        return getLCA(getNode(a), getNode(b));
    }
    

    public void addClass(AST.class_ astClass) {
        if(classNameToNode.containsKey(astClass.name)) {
            // @error
        } else if (isRestrictedInheritance(astClass)) {
            // @error
        } else {
            classNameToNode.put(astClass.name, new Node(astClass));
        }
    }

    private void addBasicClasses() {
        addObject();
        addIO();
        addString();
        addBool();
        addInt();
    }

    private void addObject() {
        AST.method abort     = new AST.method("abort", new ArrayList<>(), "Object", null, -1);
        AST.method type_name = new AST.method("type_name", new ArrayList<>(), "String", null, -1);
        AST.method copy      = new AST.method("copy", new ArrayList<>(), "Object", null, -1); // selftype instead of object
        
        List<AST.feature> obj_features = Arrays.asList(abort, type_name, copy);
        AST.class_ obj = new AST.class_("Object", null, null, obj_features, -1);
        
        rootNode = new Node(obj);
    }

    private void addIO() {
        // AST.method out_string 	= new AST.method("out_string", Arrays.asList(new AST.formal("x", "String", 1)), "SELF_TYPE", new AST.expression(), 1);
		// AST.method out_int 		= new AST.method("out_int", Arrays.asList(new AST.formal("x", "Int", 1)), "SELF_TYPE", new AST.expression(), 1);
		AST.method out_string 	= new AST.method("out_string", Arrays.asList(new AST.formal("x", "String", 1)), "IO", null, 1);
		AST.method out_int 		= new AST.method("out_int", Arrays.asList(new AST.formal("x", "Int", 1)), "IO", null, 1);
		AST.method in_string 	= new AST.method("in_string", new ArrayList<>(), "String", null, 1);
        AST.method in_int 		= new AST.method("in_int", new ArrayList<>(), "Int", null, 1);
        
        List<AST.feature> io_features = Arrays.asList(out_string, out_int, in_string, in_int);
		AST.class_ io = new AST.class_("IO", null, "Object", io_features, -1);
        
        addClass(io);
    }
    
    private void addString() {
		AST.method length  = new AST.method("length", new ArrayList<>(), "Int", null, -1);
		AST.method concat  = new AST.method("concat", Arrays.asList(new AST.formal("s", "String", 1)), "String", null, -1);
        AST.method sub_str = new AST.method("sub_str", Arrays.asList( new AST.formal("i", "Int", -1) 
                                                                    , new AST.formal("l", "Int", -1)), "String", null, -1);

        List<AST.feature> string_features = Arrays.asList(length, concat, sub_str);
        AST.class_ string  = new AST.class_("String", null, "Object", string_features, -1);

        addClass(string);
    }

    private void addBool() {
        AST.class_ bool = new AST.class_("Bool", null, "Object", new ArrayList<>(), -1);
        addClass(bool);
    }
    
    private void addInt() {
        AST.class_ int_ = new AST.class_("Int", null, "Object", new ArrayList<>(), -1);
        addClass(int_);
    }



    public static class Node {
        
        private AST.class_ astClass;
        private Node parent;
        private List<Node> children;

        private int inTime, outTime;
        private int depth;

        public Node(AST.class_ astClass) {
            this.astClass = astClass;
            this.parent = null;
            this.inTime  = 0;
            this.outTime = 0;
            children = new ArrayList<>();
        }

        public String name() {
            return astClass.name;
        }

        public AST.class_ getAstClass() {
            return astClass;
        }

        public void addChild(Node child) {
            children.add(child);
        }
        
        public List<Node> getChildNodes() {
            return children;
        }
        
        public void setParentNode(Node parent) {
            this.parent = parent;
        }

        public Node getParentNode() {
            return parent;
        }

    }

}