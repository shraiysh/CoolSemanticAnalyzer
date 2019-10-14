Semantic Analyzer for Cool
==========================

This is an implementation of the semantic analyzer for Cool which checks its static semantics.
It uses Abstract Syntax Trees (AST) built by the parser to check whether a program conforms to 
the Cool specification. It returns with non zero exit status if the program fails to conform,
along with printing as many error messages as possible. It outputs an annotated AST for use by
the code generator in case the analysis is successful.

# Program Design
At a high level the program is broken down into two phases.
 1. Pass1 - InformationGatherPass
 2. Pass2 - SemanticCheckPass

which implement themselves using the **Visitor pattern** skeleton.

 Visitor Pattern
------------------
The AST provided is changed a little to accomodate the visitor pattern.
This allows for a much cleaner interface for writing passes on the AST and also handles
double-dispatch in a neat fasion. Two files contain the code :
1. ASTVisitor.java
2. ASTBaseVisitor.java

The `ASTVisitor` is an interfact and the AST nodes *accept* objects of this type. It lays down
 the requirements for being an ASTVisitor.

The `ASTBaseVisitor` is a base implementation of the visitor which runs an empty pass over the
entire AST. This is convenient since now we may inherit and override only the functions we 
require, without needing to write the entire pass everytime. This prevents redundancy of code 
and reduces chances of error.

Out of the two options, a decision was made to keep the `accept` calling ability with the 
Visitor implementor. This increases flexibility of the program manifold.

 InformationGatherPass
-----------------------
All this pass does is collect and gather information about the program which will be used in the
 next pass for analysis. It mainly focuses on class heirarchies.

#### Purpose

1. Build the class graph / inheritance graph
2. Detect cyclic dependency of classes
3. Check for Restricted inheritance
4. Check for other ill formed class hierarchical issues

It build the class graph or the inheritance graph based on the parent information associated with 
each class. It then detects cycles and if encountered terminates the analysis. It also checks 
for other ill-formed hierarchical issues, such as inheritance from restricted classes.

In case of error during this stage of the analysis, it becomes irrecoverable and halts the analysis 
returning an error status.

It mainly uses the **ClassGraph** for the heavylifting.

### ClassGraph
The ClassGraph maintains nodes for each class. These nodes contain information about its position 
in the tree. 

Contents of **Node** :

* AST node of class
* Parent Node
* List of Child Nodes
* HashMap for methods in class

The ClassGraph also maintains a HashMap pointing from class names to their corresponding nodes.

#### Algorithms
* Depth First Search
* Ancestral Analysis
* LCA finding Algorithm using in and out Times
* Cycle Finding Algorithm - optimized for special case.

Since each node has atmost one parent, a clever way of detecting cycles is to simply visit all 
nodes starting from the root. If any nodes have been left unvisited they must be in a loop since 
they weren't reachable from root Node.

SemanticCheckPass
------------------
This pass performs the actual analysis of the Cool program and also annotates the AST.

#### Purpose
1. Maintain Scopes and check for valid access
2. Validate method overriding
3. Invalidate illegal accesses or redfinitions
4. Check for type conformance
5. Generate types for expressions
6. Annotate the AST
7. Recover from errors to find more errors in a single run.

It receives the ClassGraph information from pass1 - the InformationGatherPass and uses it to 
simplify analysis. At this stage it is guaranteed that the class hierarchies are well formed.

* It maintains a ScopeTable for attributes and objects. This is used to check for redefinitions and illegal accesses.
* It also precomputes the methods each class has to be able to refer them when a dispatch occurs.

#### Scopetable
We make extensive use of DFS traversal in using the ScopeTable.
This way, the scopes are handled in an elegant manner since the unrequired entries are popped accordingly.
A new scope is added when we go further down the AST and is removed when backing up.

#### ClassGraph
Main use of the Classgraph here is to get the Ancestral Analysis information.
* Several times we need to check for conformance of types which are handled in O(1) using the
LCA precomputation. This saves both time and memory over other methods.
* This is also used for finding the *join* of nodes. This takes atmost O(h) time where h is the height of tree.

#### Recoveries
One of the main achievements of this pass is that it recovers from ALL errors.
Different bailing decisions were made for different scenarios.
Some of these Recovery decisions are : 
* If the id is illegally `self` : We replace the id by a generated id for now and hide it from rest of the program's view.
* If the method redefinition is illegal, this method's body is still analyzed but is hidden from others.
* If method has been redeclares in the same scope the first declaration/defintion is considered valid.
* Redefined attributes are dealt with similarly and the first is considered valid.



