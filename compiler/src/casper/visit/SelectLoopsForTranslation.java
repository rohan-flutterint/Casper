/* 
 * This class implements a single compiler pass. The pass is executed 
 * after the code has been loop-normalized and expression-flattened.
 * The goal of the pass is to identify all loops that we should attempt 
 * to parallelize.
 * 
 * Currently all loops are considered 'interesting' unless they contain
 * either an unrecognized function call (from an external library we 
 * do not support) or have branching points.
 *    
 * - Maaz
 */

package casper.visit;

import java.util.ArrayList;
import java.util.Stack;

import casper.JavaLibModel;
import casper.ast.JavaExt;
import casper.extension.MyWhileExt;
import polyglot.ast.Block;
import polyglot.ast.Branch;
import polyglot.ast.Call;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.While;
import polyglot.visit.NodeVisitor;

public class SelectLoopsForTranslation extends NodeVisitor{
	ArrayList<MyWhileExt> extensions;
	int currDepth;
	Stack<Integer> branchCounts;
	int currBranchCount;
   	boolean debug;
   	Node currParent;
   
   	public SelectLoopsForTranslation(){
   		this.currDepth = 0;
   		this.currBranchCount = 0;
	   	this.debug = false;
	   	this.extensions = new ArrayList<MyWhileExt>();
	   	this.branchCounts = new Stack<Integer>();
	   	this.currParent = null;
   	}
   
   	@Override
	public NodeVisitor enter(Node parent, Node n){
   		
   		// If the node is a block of method
   		if(n instanceof Block && parent instanceof MethodDecl){
   			currParent = n;
   		}
		// If the node is a loop
   		else if(n instanceof While){
			// Increment current loop depth
			this.currDepth = this.currDepth + 1;

			// Get extension of loop node
			MyWhileExt ext = (MyWhileExt) JavaExt.ext(n);
			
			// Save parent
			ext.parent = currParent;
			
			// Mark loop as interesting
			ext.interesting = true;
			
			// Save current branch count for outer loop
			branchCounts.push(this.currBranchCount);
			
			// Reset current branch count to 0
			this.currBranchCount = 0;
			
			this.extensions.add(ext);
		}
   		// If the node is a function call
		else if(n instanceof Call){
			// Do we recognize this function call?
			if(!JavaLibModel.recognizes((Call)n)){
				// We don't. So these loops cannot be optimized
				for(MyWhileExt ext : this.extensions)
					ext.interesting = false;
			}
		}
   		// If the node is a branching statement
		else if(n instanceof Branch){
			// Increase count of branch statements
			this.currBranchCount++;
		}
     
		return this;
	}
  
  
	@Override
	public Node leave(Node old, Node n, NodeVisitor v){
		// If the node is a loop
		if(n instanceof While){
			// Decrement depth
			this.currDepth--;
			
			// Get extension of loop node
			MyWhileExt ext = ((MyWhileExt)JavaExt.ext(n));
			
			// Were there more than one branches in the loop?
			if(this.currBranchCount > 1){
				// Yes, so we can't optimize this (currently not supported)
				ext.interesting = false;
			}
			else{
				if(debug)
				{
					System.err.println(n);
					System.err.println(((While) n).body());
				}
			}
			
			// Reset currBranchCount to outer loop branch count
			this.currBranchCount = this.branchCounts.pop();
			
			// We have exited the loop
			this.extensions.remove(ext);
		}
      
		return n;
	}
  
	@Override
	public void finish(){
		if(debug){
			System.err.println("\n************* Finished loop extraction complier pass *************");
		}
	}
}