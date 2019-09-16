package cool;



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
	public Semantic(AST.program program){
		//Write Semantic analyzer code here
		
		// Check for cycles in inheritance graph
		Graph<String> iGraph = new Graph<String>();
		for(AST.class_ iclass : program.classes) {
			iGraph.addEdge(iclass.name, iclass.parent, false);
		}

		if(iGraph.isCyclic()) {
			System.err.println("ERROR: Cyclic Inheritance (Parent graph): \n" + iGraph.toString(" -> "));
			System.err.println("Aborting.");
			System.exit(0);
		}

		// Valid Inheritance graph
		// Add other checks now
	}
}
