package casper.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import polyglot.ast.Expr;

public class CallNode extends CustomASTNode {
	
	public ArrayList<CustomASTNode> arguments;
	
	public CallNode(String n, ArrayList<CustomASTNode> args) {
		super(n);
		arguments = args;
	}

	@Override
	public CustomASTNode replaceAll(String lhs, CustomASTNode rhs){
		ArrayList<CustomASTNode> newArgs = new ArrayList<CustomASTNode>();
		for(CustomASTNode arg : arguments){
			newArgs.add(arg.replaceAll(lhs, rhs));
		}
		return new CallNode(name,newArgs);
	}
	
	public String toString(){
		String args = "";
		for(int i=0; i<arguments.size(); i++){
			CustomASTNode arg = arguments.get(i);
			args +=  arg.toString();
			if(i<arguments.size()-1)
				args += ",";
		}
		return name + "(" + args + ")";
	}

	@Override
	public boolean contains(String exp) {
		if(name.equals(exp))
			return true;
		
		for(CustomASTNode arg : arguments){
			if(arg.contains(exp))
				return true;
		}
		
		return false; 
	}

	@Override
	public void getIndexes(String arrname, Map<String, List<CustomASTNode>> indexes) {
		for(CustomASTNode arg : arguments){
			arg.getIndexes(arrname, indexes);
		}
	}
	
}