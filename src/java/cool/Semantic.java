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


	private AST.method abort = new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.expression(), 1);
	private AST.method type_name = new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.expression(), 1);
	private AST.method copy = new AST.method("copy", new ArrayList<AST.formal>(), "SELF_TYPE", new AST.expression(), 1);
	AST.class_ object = new AST.class_("Object", "BASIC_CLASS", "", Arrays.asList(abort, type_name, copy), 1);

	int numClasses = 0;
	HashMap<String, AST.class_> classnameToClass = new HashMap<String, AST.class_>();
	HashMap<String, Integer> classnameToInt = new HashMap<String, Integer>();
	HashMap<Integer, String> intToClassname = new HashMap<Integer, String>();

	ArrayList<ArrayList<Integer>> classGraph = new ArrayList<ArrayList<Integer>>();

	public Semantic(AST.program program){
		//Write Semantic analyzer code here

		// Check inheritance and populate classGraph
		analyzeInheritance(program);

		if(getErrorFlag()) {
			System.exit(0);
		}

	}


	ArrayList<ArrayList<Integer>> analyzeInheritance(AST.program program) {
		// Add the basic classes - IO, Int, String and Bool to program
		addBasicClasses(program);

		List<String> noInherit = Arrays.asList("Int", "String", "Bool");
		List<String> basicClasses = Arrays.asList("Object", "IO", "Int", "String", "Bool");
		List<String> noRedef = new ArrayList<String>();
		noRedef.add("Object");

		if(numClasses > 0) {
			// Reset all maps
			classnameToClass.clear();
			classnameToInt.clear();
			intToClassname.clear();
			classGraph.clear();
			numClasses = 0;
		}
		
		// Add object to the maps
		classnameToClass.put("Object", object);
		classnameToInt.put("Object", numClasses);
		intToClassname.put(numClasses++, "Object");

		for(AST.class_ iclass : program.classes) {
			if(noRedef.contains(iclass.name)) {
				// Class already defined
				if(basicClasses.contains(iclass.name)) {
					reportError(iclass.filename, iclass.lineNo, "Redefinition of basic class " + iclass.name);
				}
				else {
					reportError(iclass.filename, iclass.lineNo, "Class " + iclass.name + " was previously defined.");
				}
			}
			else {
				// Adding a new valid class to the maps
				classnameToClass.put(iclass.name, iclass);
				classnameToInt.put(iclass.name, numClasses);
				intToClassname.put(numClasses++, iclass.name);
				noRedef.add(iclass.name);
			}
			if(noInherit.contains(iclass.parent)) {
				// Uninheritable class
				reportError(iclass.filename, iclass.lineNo, "Class " + iclass.name + " cannot inherit class " + iclass.parent);
			}
		}

		ArrayList<ArrayList<Integer>> graph = new ArrayList<ArrayList<Integer>>();

		while(graph.size() < numClasses) {
			graph.add(new ArrayList<Integer>());
		}

		// Populate the graph : parent -> [child]
		for(AST.class_ iclass : program.classes) {
			if(!classnameToClass.containsKey(iclass.parent) && !iclass.parent.equals("Object")) {
				reportError(iclass.filename, iclass.lineNo, "Class " + iclass.name + " inherits from an undefined class " + iclass.parent + ".");
				continue;
			}
			graph.get(classnameToInt.get(iclass.parent)).add(classnameToInt.get(iclass.name));
		}

		// Check for cycles
		boolean hasCycles = false;
		boolean[] visited = new boolean[numClasses];
		for(int i = 0; i < numClasses; i++) visited[i] = false;
		Queue<Integer> q = new LinkedList<Integer>();
		q.offer(0);

		while(!q.isEmpty()) {
			int u = q.poll();
			if(visited[u]) {
				hasCycles = true;
				AST.class_ errorClass = classnameToClass.get(intToClassname.get(u));
				reportError(errorClass.name, errorClass.lineNo, "Class " + errorClass.name + ", or an ancestor of " + errorClass.name + ", is involved in an inheritance cycle.");
				break;
			}
			else {
				visited[u] = true;
			}
			for(Integer i : graph.get(u)) {
				q.offer(i);
			}
			if(q.isEmpty()) {
				// Check for any unvisited nodes
				int i = 0;
				while(i < visited.length && visited[i]) i++;
				if(i < visited.length) q.offer(i);
			}
		}

		if(hasCycles) {
			System.exit(0);
		}

		return graph;
	}
	// Utility function for recursively checking cycles in graph

	void addBasicClasses(AST.program program) {
		
		AST.method out_string = new AST.method("out_string", Arrays.asList(new AST.formal("x", "String", 1)), "SELF_TYPE", new AST.expression(), 1);
		AST.method out_int = new AST.method("out_int", Arrays.asList(new AST.formal("x", "Int", 1)), "SELF_TYPE", new AST.expression(), 1);
		AST.method in_string = new AST.method("in_string", new ArrayList<AST.formal>(), "String", new AST.expression(), 1);
		AST.method in_int = new AST.method("in_int", new ArrayList<AST.formal>(), "Int", new AST.expression(), 1);
		AST.class_ io = new AST.class_("IO", "BASIC_CLASS", "Object", Arrays.asList(out_string, out_int, in_string, in_int), 1);

		AST.class_ int_ = new AST.class_("Int", "BASIC_CLASS", "Object", new ArrayList<feature>(), 1);
		
		AST.method length = new AST.method("length", new ArrayList<AST.formal>(), "Int", new AST.expression(), 1);
		AST.method concat = new AST.method("concat", Arrays.asList(new AST.formal("s", "String", 1)), "String", new AST.expression(), 1);
		AST.method sub_str = new AST.method("sub_str", Arrays.asList(new AST.formal("i", "Int", 1), new AST.formal("l", "Int", 1)), "String", new AST.expression(), 1);
		AST.class_ string = new AST.class_("String", "BASIC_CLASS", "Object", Arrays.asList(length, concat, sub_str), 1);

		AST.class_ bool = new AST.class_("Bool", "BASIC_CLASS", "Object", new ArrayList<AST.feature>(), 1);

		program.classes.addAll(0, Arrays.asList(io, int_, string, bool));
	}
}
