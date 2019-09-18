package cool;

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

	boolean isBasicClass(String classname) {
		if(classname.equals("IO") || classname.equals("Object") || classname.equals("Int") || classname.equals("String") || classname.equals("Bool")) {
			return true;
		}
		return false;
	}

	AST.class_ findClass(AST.program program, String name) {
		for(AST.class_ iclass : program.classes) {
			if(iclass.name.equals(name)) {
				return iclass;
			}
		}
		return null;
	}

	boolean noClassRedefinition(AST.program program) {
		boolean flag = true;
		for(int i = 0; i < program.classes.size(); i++) {
			AST.class_ one = program.classes.get(i);
			if( isBasicClass(one.name) ) {
				flag = false;
				reportError(one.filename, one.lineNo, "Redefinition of basic class " + one.name);		// Checked - testBasicClassRedef.cl
			}
			for(int j = i + 1; j < program.classes.size(); j++) {
				AST.class_ two = program.classes.get(j);
				if(one.name.equals(two.name)) {
					reportError(one.filename, one.lineNo, "Class " + two.name + " was previously defined.");		// Checked - testMultipleClassDef.cl
					flag = false;
				}
			}
		}
		if(flag == false) {
			System.exit(0);
		}
		return true;
	}

	void validFeatureInheritance(AST.program program, AST.class_ parent, AST.class_ child) {
		for(AST.feature childFeature : child.features) {
			for(AST.feature parentFeature : parent.features) {
				if(childFeature instanceof AST.attr && parentFeature instanceof AST.attr) {
					AST.attr cattr = (AST.attr)childFeature, pattr = (AST.attr)parentFeature;
					if(cattr.name.equals(pattr.name)) {
						reportError(child.filename, cattr.lineNo, "Attribute " + cattr.name + " is an attribute of an inherited class.");			// Checked - testAttrRedefChildClass.cl
						System.exit(0);
					}
				}
				else if(childFeature instanceof AST.method && parentFeature instanceof AST.method) {
					AST.method cmethod = (AST.method)childFeature, pmethod = (AST.method)parentFeature;
					// Function overloading is not allowed, so just matching names is sufficient
					if(cmethod.name.equals(pmethod.name) && !cmethod.typeid.equals(pmethod.typeid)) {
						reportError(child.filename, cmethod.lineNo, "In redefined method " + cmethod.name + ", return type " + cmethod.typeid + " is different from original return type " + pmethod.typeid);
						// Checked - testMethodOverloading.cl
						System.exit(0);
					}
				}
			}
		}
		if( !isBasicClass(parent.parent) ) {
			AST.class_ prevParent = findClass(program, parent.parent);
			if(prevParent != null) {
				validFeatureInheritance(program, prevParent, child);
			}
		}
	}

	void validFeatureInheritance(AST.program program) {
		for(AST.class_ child : program.classes) {
			if( isBasicClass(child.parent) ) continue;
			AST.class_ parent = findClass(program, child.parent);
			if(parent != null) {
				validFeatureInheritance(program, parent, child);
			}
		}
	}

	void noAttrRedefinitionSameClass(AST.program program) {
		for(AST.class_ iclass : program.classes) {
			for(int i = 0; i < iclass.features.size(); i++) {
				for(int j = i + 1; j < iclass.features.size(); j++) {
					if(iclass.features.get(i) instanceof AST.attr && iclass.features.get(j) instanceof AST.attr) {
						AST.attr attr1 = (AST.attr) iclass.features.get(i), attr2 = (AST.attr) iclass.features.get(j);
						if(attr1.name.equals(attr2.name)) {
							reportError(iclass.filename, attr1.lineNo, "Attribute " + attr1.name + " is multiply defined in class.");
							System.exit(0);
						}
					}
				}
			}
		}
	}

	void noFormalRedef(AST.program program) {
		for(AST.class_ iclass : program.classes) {
			for(AST.feature ifeature : iclass.features) {
				if(ifeature instanceof AST.method) {
					AST.method method = (AST.method) ifeature;
					for(int i = 0; i < method.formals.size(); i++) {
						for(int j = i + 1; j < method.formals.size(); j++) {
							if(method.formals.get(i).name.equals(method.formals.get(j).name)) {
								// 2 formals with same name
								// Check - testFormalRedef.cl
								reportError(iclass.filename, method.lineNo, "Formal parameter " + method.formals.get(i).name + " is multiply defined.");
								System.exit(0);
							}
						}
					}
				}
			}
		}
	}

	void noCyclicInheritance(AST.program program) {
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
	}

	boolean checkInheritanceAndRedefinitions(AST.program program) {
		noClassRedefinition(program);
		noAttrRedefinitionSameClass(program);
		noFormalRedef(program);
		noCyclicInheritance(program);
		validFeatureInheritance(program);
		return true;
	}

	void checkMainDefined(AST.program program) {
		for(AST.class_ iclass : program.classes) {
			if(iclass.name.equals("Main")) {
				for(AST.feature feature : iclass.features) {
					if(feature instanceof AST.method){
						AST.method method = (AST.method) feature;
						if(method.name.equals("main")) {
							if(method.formals.size() == 0) {
								return;
							}
							// This means that arguments are not valid
							// Check - testMainMethodArgInvalid.cl
							reportError(iclass.filename, method.lineNo, "'main' method in class Main should have no arguments.");
							System.exit(0);
							// break;
						}
					}
				}
				// This means no main method in Main class
				// Check - testNoMainMethod.cl
				reportError(iclass.filename, iclass.lineNo, "No 'main' method in class Main.");
				System.exit(0);
				// break;
			}
		}
		// No main class
		// Check - testNoMainClass.cl
		reportError("", 1, "Class Main is not defined.");
		System.exit(0);
	}

	public Semantic(AST.program program){
		//Write Semantic analyzer code here

		checkInheritanceAndRedefinitions(program);

		checkMainDefined(program);

	}
}
