package cool;

import java.util.*;

import cool.AST.feature;

public class Semantic{
	private boolean errorFlag = false;
	public void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

/*
	Don't change code above this line
*/


	// private AST.method abort = new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.expression(), 1);
	// private AST.method type_name = new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.expression(), 1);
	// private AST.method copy = new AST.method("copy", new ArrayList<AST.formal>(), "SELF_TYPE", new AST.expression(), 1);
	// AST.class_ object = new AST.class_("Object", "BASIC_CLASS", "", Arrays.asList(abort, type_name, copy), 1);

	// int numClasses = 0;
	// HashMap<String, AST.class_> classnameToClass = new HashMap<String, AST.class_>();
	// HashMap<String, Integer> classnameToInt = new HashMap<String, Integer>();
	// HashMap<Integer, String> intToClassname = new HashMap<Integer, String>();

	// ArrayList<ArrayList<Integer>> classGraph = new ArrayList<ArrayList<Integer>>();

	public Semantic(AST.program program){
		//Write Semantic analyzer code here

		ClassGraph graph = new ClassGraph();

		ASTVisitor pass1 = new InfoGatherPass(graph);
		
		program.accept(pass1);
		
		ASTVisitor pass2 = new SemanticCheckPass(graph);

		program.accept(pass2);
		// Check inheritance and populate classGraph
		// analyzeInheritance(program);

		if(getErrorFlag()) {
			System.exit(1);
		}

	}

}
