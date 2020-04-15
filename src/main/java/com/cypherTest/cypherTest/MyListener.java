package com.cypherTest.cypherTest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

public class MyListener extends xpathBaseListener {

	private StringBuilder query = new StringBuilder();
	private Object appliesTo;
    private Object lastAttribute;
    private boolean lastNodeInPath;
    private boolean endPathStep;
	private boolean attribute;
	private String attributeName;
	private boolean attributeOnly = false;
    private boolean insidePredicate = false;
    private boolean insidePredicateFunction = false;
    private Object predicateValue;
    private boolean isNode = true;
    private boolean addPath = false;
    private int pathIndex = 0;
    
    private boolean firstStep = true;
    
    private String functionName;
    private Stack<String> functionNames = new Stack<String>();
    private Stack<Boolean> isNodeStack = new Stack<Boolean>();
    private Stack<Boolean> isLastNodeInPathStack = new Stack<Boolean>();
    private Stack<Boolean> hasEdges = new Stack<Boolean>();
    
    private String axis = "";
    private String startEdge;
    private String endEdge;
    
    public StringBuilder priorityQuery = new StringBuilder();
    public Stack<StringBuilder> priorityQueries = new Stack<StringBuilder>();
    private StringBuilder whereQuery = new StringBuilder();
    private String returnValue;
    private Stack<String> returnValues = new Stack<String>();
    public StringBuilder cypherQuery = new StringBuilder();
    
    
    private Stack<String> paths = new Stack<String>();
    private Stack<String> appliesToStack = new Stack<String>(); 
    
    private int aliasIndex = 0;

	public MyListener() {
        this.query = new StringBuilder();
    }

    public void setQuery(Object s) {
    	this.query.append(s);
    }

    public Object getQuery() {
    	return query;
    }

    @Override
    public void exitMain(xpathParser.MainContext ctx) {
    	System.out.println("Cypher query");
    	System.out.println(this.query);
    }
    
    @Override
    public void exitUnaryExprNoRoot(xpathParser.UnaryExprNoRootContext ctx) {
    	
    }
    
    @Override
    public void exitUnionExprNoRoot(xpathParser.UnionExprNoRootContext ctx) {
    	
    	
    }
    
    @Override
    public void exitPathExprNoRoot(xpathParser.PathExprNoRootContext ctx) {
    	
    }
    
    @Override
    public void exitLocationPath(xpathParser.LocationPathContext ctx) {
    	if (!this.insidePredicate) {
    		if (this.query.length() > 0) {
        		this.query.append(" UNION ");
        	}
        	this.query.append("MATCH " + this.priorityQuery);
        	if (this.whereQuery.length() > 0) {
        		this.query.append(" WHERE " + this.whereQuery);
        	}
        	this.query.append(" RETURN " + this.returnValue);
        	this.returnValue = "";
        	this.priorityQuery = new StringBuilder();
        	this.whereQuery = new StringBuilder();
    	}
    	
    	//
    	
    }

    @Override
    public void enterAbsoluteLocationPathNoroot(xpathParser.AbsoluteLocationPathNorootContext ctx) {
    }
    
    @Override
    public void enterFunctionCall(xpathParser.FunctionCallContext ctx) {
    }
    
    @Override
    public void exitFunctionCall(xpathParser.FunctionCallContext ctx) {
    	if (this.insidePredicate) {
    		this.whereQuery.append(this.returnValue + " " + this.functionNames.pop() + " " + this.predicateValue);
    		this.insidePredicateFunction = false;
    	} else {
    		this.returnValue = this.functionName + "(" + this.returnValue + ")";
        	this.functionNames.pop();
    	}
    	
    }
    
    @Override
    public void exitFunctionName(xpathParser.FunctionNameContext ctx) {
    	if (ctx.getChildCount() > 0) {
    		StringBuilder sb = new StringBuilder();
    		sb.append(ctx.getChild(0));
    		String cypherFunctionName = "";
    		if (this.insidePredicate) {
    			if (sb.toString().equals("contains")) {
        			cypherFunctionName = "CONTAINS";
        		} else if (sb.toString().equals("starts-with")) {
        			cypherFunctionName = "STARTS WITH";
        		} else if (sb.toString().equals("ends-with")) {
        			cypherFunctionName = "ENDS WITH";
        		} else {
        			throw new IllegalArgumentException("Function name inside predicate invalid!");
        		}
    			this.insidePredicateFunction = true;
    		} else {
    			if (sb.toString().equals("avg")) {
    			    cypherFunctionName = "avg";
    		    } else if (sb.toString().equals("min")) {
    			    cypherFunctionName = "min";
    		    } else if (sb.toString().equals("max")) {
    		 	    cypherFunctionName = "max";
    		    } else if (sb.toString().equals("sum")) {
    			    cypherFunctionName = "sum";
    		    } else if (sb.toString().equals("count")) {
    			    cypherFunctionName = "count";
    		    } else if (sb.toString().equals("ceil")) {
    			    cypherFunctionName = "ceil";
    		    } else if (sb.toString().equals("floor")) {
    			    cypherFunctionName = "floor";
    		    } else if (sb.toString().equals("round")) {
    			    cypherFunctionName = "round";
    		    } else if (sb.toString().equals("not")) {
    			    cypherFunctionName = "not";
    		    } else {
    			   throw new IllegalArgumentException("Function name is invalid!");
    		    }
    		}
    		this.functionName = cypherFunctionName;
    		this.functionNames.push(cypherFunctionName);
    	}
    }

    @Override
    public void exitAbsoluteLocationPathNoroot(xpathParser.AbsoluteLocationPathNorootContext ctx) {
        if (this.insidePredicate) {
        	
        }
    }

    @Override
    public void exitNCName(xpathParser.NCNameContext ctx) {
    	StringBuilder sb = new StringBuilder();
    	String ncName = sb.append(ctx.getChild(0)).toString();
    	
    	if (this.firstStep && this.axis.equals("attribute")) {
    		if (this.addPath) {
    			this.returnValue = ncName;
    		} else if (this.insidePredicate) {
    			this.returnValue = this.appliesTo + "." + ncName;
    		} else {

    			this.priorityQuery.append("(a"+ this.aliasIndex +")");
        		this.returnValue = "a" + this.aliasIndex + "." + ncName;
    		}
    		this.aliasIndex++;
    		this.attributeOnly = true;
    		this.attribute = true;
    		this.attributeName = ncName;
    	} else if(this.axis.equals("attribute")) {
    		this.attribute = true;
    		this.attributeName = ncName;
    		if (this.appliesToStack.size() > 0) {
    			this.returnValue = this.appliesTo + "." + ncName;
    		} else {
    			this.returnValue = this.returnValue + "." + ncName;
    		}
    		
    	} else if (this.isNode) {
    		this.priorityQuery.append("(a" + this.aliasIndex + ":" + ncName + ")");
    		this.returnValue = "a" + this.aliasIndex;
    		this.aliasIndex++;
    	} else if (!this.isNode) {
    		if (this.priorityQueries.size() > 0 && this.firstStep) {
    			System.out.println(this.addPath);
    			if (this.addPath) {
    				this.priorityQuery.append("(" + this.appliesToStack.peek() + ")");
    			} else {
    				System.out.println("kkk");
    				this.priorityQuery.append(", (" + this.appliesToStack.peek() + ")");
    			}
    			
    		}
    		
    		//If transitive axis, then ignore the alias name
    		if (this.axis.equals("ancestor") || this.axis.equals("descendant") || this.axis.equals("ancestor-or-self") || this.axis.equals("descendant-or-self")) {
    			this.priorityQuery.append(this.startEdge + ":" + ncName.toUpperCase() + "" + this.endEdge);
    			
    		//If parent or child, add alias name and the edge label.
    		} else if (this.axis.equals("parent") || this.axis.equals("child")) {
    			this.priorityQuery.append(this.startEdge + "a" + this.aliasIndex + ":" + ncName.toUpperCase() + "" + this.endEdge);
    			
    		//If no axis, this defaults to child
    		} else {
    			this.priorityQuery.append("-[a" + this.aliasIndex + ":" + ncName.toUpperCase() + "]->");
    		}
    		this.aliasIndex++;
    		
    	}
    	this.firstStep = false;
    }
    
    @Override
    public void enterRelativeLocationPath(xpathParser.RelativeLocationPathContext ctx) {
    	
    }

    @Override
    public void exitRelativeLocationPath(xpathParser.RelativeLocationPathContext ctx) {
    	if (this.isNode && this.attribute && !this.attributeOnly) {
    		this.priorityQuery.append("()");
    	} else if (!this.isNode && !this.attribute) {
    		this.priorityQuery.append("()");
    	}
    }

    @Override
    public void enterStep(xpathParser.StepContext ctx) {
    	
    	int indexOfCurrentChildNode = ctx.getParent().children.indexOf(ctx);
    	if (indexOfCurrentChildNode > 0) {
    		if (ctx.parent.getChild(indexOfCurrentChildNode - 1).toString().equals("/")) {
    			this.isNode = !this.isNode;
    		} else if (ctx.parent.getChild(indexOfCurrentChildNode - 1).toString().equals("//")) {
    			this.priorityQuery.append("-[*]->");
    		}
    		if (this.lastNodeInPath && !this.isNode) {
    			
    			this.priorityQuery.append(", (" + this.returnValue + ")");
    		}
    		
    	}
    }

    @Override
    public void exitStep(xpathParser.StepContext ctx) {
        
    }

    @Override
    public void exitPrimaryExpr(xpathParser.PrimaryExprContext ctx) {
    	this.predicateValue = ctx.getChild(0);
    }

    @Override
    public void exitAxisSpecifier(xpathParser.AxisSpecifierContext ctx) {
    	this.axis = "";
    	StringBuilder sb = new StringBuilder();
    	sb.append(ctx.getChild(0));
    	if (sb.toString().equals("parent")) {
    		this.axis = "parent";
    		this.startEdge = "<-[";
    		this.endEdge = "]-";
    	} else if (sb.toString().equals("ancestor")) {
    		this.startEdge = "<-[";
    		this.endEdge = "*]-";
    		this.axis = "ancestor";
    	} else if (sb.toString().equals("child")) {
    		this.startEdge = "-[";
    		this.endEdge = "]->";
    		this.axis = "child";
    	} else if (sb.toString().equals("descendant")) {
    		this.startEdge = "-[";
    		this.endEdge = "*]->";
    		this.axis = "descendant";
    	} else if (sb.toString().equals("ancestor-or-self")) {
    		this.startEdge = "<-[";
    		this.endEdge = "*0..]-";
    		this.axis = "ancestor-or-self";
    	} else if (sb.toString().equals("descendant-or-self")) {
    		this.startEdge = "-[";
    		this.endEdge = "*0..]->";
    		this.axis = "descendant-or-self";
    	} else if (sb.toString().equals("attribute") || sb.toString().equals("@")) {
    		this.axis = "attribute";
    	}
    }

    @Override
    public void exitEqualityExpr(xpathParser.EqualityExprContext ctx) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(ctx.getChild(1));
        //Jos kyseessä on attribuutti, lisätään operaattori ja attribuutin arvo.
    	if (ctx.getChildCount() > 1) {
    	    if (sb.toString().equals("=")) {
			    StringBuilder s = this.priorityQueries.pop();
			    if (s.charAt(s.length() - 1) == ')') {
				    if (s.charAt(s.length() - 2) == '}') {
					    s.insert(s.length() - 2, ", " + this.attributeName + ":" + this.predicateValue);
				    } else {
					    s.insert(s.length() - 1, " {" + this.attributeName + ":" + this.predicateValue + "}");
				    }
			    } else if (s.charAt(s.length() - 1) == '>') {
				    if (s.charAt(s.length() - 4) == '}') {
					    s.insert(s.length() - 4, ", " + this.attributeName + ":" + this.predicateValue);
				    } else {
					    s.insert(s.length() - 3, " {" + this.attributeName + ":" + this.predicateValue + "}");
				    }
			    } else if (s.charAt(s.length() - 1) == '-') {
				    if (s.charAt(s.length() - 3) == '}') {
					    s.insert(s.length() - 3, ", " + this.attributeName + ":" + this.predicateValue);
				    } else {
					    s.insert(s.length() - 2, " {" + this.attributeName + ":" + this.predicateValue + "}");
				    }  
			    }
			    this.priorityQueries.push(s);
		    } else {
                if (this.attribute && this.insidePredicate && ctx.getChildCount() == 1) {
        	        if (this.whereQuery.length() > 0) {
        		        this.whereQuery.append(" AND ");
         	        }
        	
        	        if (this.addPath) {
        		        this.whereQuery.append("ALL(rel in relationships(" + this.paths.peek() + ") WHERE EXISTS(rel." + this.returnValue + "))");
        	        } else {
        		        if (!this.insidePredicateFunction) {
        			        this.whereQuery.append("EXISTS(" + this.returnValue + ")");
                	        this.attribute = false;
        		        }	
        	        }
                } else if (this.attribute && this.insidePredicate && ctx.getChildCount() > 1) {
        	        if (this.whereQuery.length() > 0) {
        		        this.whereQuery.append(" AND ");
        	        }
        	        if (this.addPath) {
        		        this.whereQuery.append("ALL(rel in relationships(" + this.paths.peek() + ") WHERE rel." + this.returnValue + "" + ctx.getChild(1) + "" + this.predicateValue + ")");	
        	        } else {
        		        this.whereQuery.append(this.returnValue + "" + ctx.getChild(1) + "" + this.predicateValue);
            	        this.attribute = false;
        	        }	
                }
		    }
    	}
        /*
        if (this.insidePredicate) {
        	this.returnValue = this.returnValues.peek();
        }*/
        
    }

    @Override
    public void exitRelationalExpr(xpathParser.RelationalExprContext ctx) {
        //Jos kyseessä on attribuutti, lisätään operaattori ja attribuutin arvo.
    	if (this.attribute && this.insidePredicate && ctx.getChildCount() > 1) {
        	if (this.whereQuery.length() > 0) {
        		this.whereQuery.append(" AND ");
        	}
        	this.whereQuery.append(this.returnValue + "" + ctx.getChild(1) + "" + this.predicateValue);
        	this.attribute = false;
        }
    }

    @Override
    public void enterAndExpr(xpathParser.AndExprContext ctx) {
        //And-operaattorila on aina pariton määrä lapsia, joista parittomat luvut ovat "and"-operaattoreita
    }


    @Override
    public void exitAndExpr(xpathParser.AndExprContext ctx) {
        
    }

    @Override
    public void enterOrExpr(xpathParser.OrExprContext ctx) {
        
    }


    @Override
    public void exitOrExpr(xpathParser.OrExprContext ctx) {
        
    }


    @Override
    public void enterNodeTest(xpathParser.NodeTestContext ctx) {
    }

    @Override
    public void exitExpr(xpathParser.ExprContext ctx) {
        if (this.attribute && this.insidePredicate) {
            this.attribute = false;
        }
    }


    @Override
    public void exitNameTest(xpathParser.NameTestContext ctx) {
    	
        StringBuilder g = new StringBuilder();
        g.append(ctx.getChild(0));
        if (g.toString().equals("*")) {
        	if (this.isNode) {
        		this.priorityQuery.append("(a" + this.aliasIndex + ")");
        	} else {
        		this.priorityQuery.append("-[a" + this.aliasIndex + "]->");
        	}
        	this.returnValue = "a" + this.aliasIndex;
        	this.aliasIndex++;
        }
        
    }

    @Override
    public void enterPredicate(xpathParser.PredicateContext ctx) {
    	if (this.axis.equals("ancestor") || this.axis.equals("descendant") || this.axis.equals("ancestor-or-self") || this.axis.equals("descendant-or-self")) {
			this.addPath = true;
			if (this.insidePredicate) {
				this.priorityQuery.insert(2, "p" + this.pathIndex + "=");
			} else {
				this.priorityQuery.insert(0, "p" + this.pathIndex + "=");
			}
			this.paths.push("p" + this.pathIndex);
			this.pathIndex++;
		}
    	this.isLastNodeInPathStack.push(this.lastNodeInPath);
    	this.lastNodeInPath = false;
    	this.appliesToStack.push(this.returnValue);
    	this.isNodeStack.push(this.isNode);
    	this.returnValues.push(this.returnValue);
    	this.priorityQueries.push(this.priorityQuery);
    	this.priorityQuery = new StringBuilder();
    	
    	//First step in predicate
    	this.firstStep = true;
    	this.isNode = false;
    	this.appliesTo = this.returnValue;
    	this.insidePredicate = true;
        	
    }

    @Override
    public void exitPredicate(xpathParser.PredicateContext ctx) {
    	this.lastNodeInPath = this.isLastNodeInPathStack.pop();
    	if (this.addPath) {
    		this.lastNodeInPath = true;
    	}
    	this.addPath = false;
    	this.priorityQuery = this.priorityQueries.pop().append(this.priorityQuery);
    	this.isNode = this.isNodeStack.pop();
    	this.returnValue = this.returnValues.pop();
    	this.appliesTo = this.appliesToStack.pop();
    	
    	if (this.appliesToStack.size() == 0) {
    		this.insidePredicate = false;
    		this.axis = "";
    	}
    }

    @Override
    public void exitAbbreviatedStep(xpathParser.AbbreviatedStepContext ctx) {
    	StringBuilder g = new StringBuilder();
        g.append(ctx.getChild(0));
        if (g.toString().equals("..")) {
        	if (this.isNode) {
        		this.priorityQuery.append("(a" + this.aliasIndex + ")");
        	} else {
        		this.priorityQuery.append("<-[a" + this.aliasIndex + "]-");
        	}
        	this.returnValue = "a" + this.aliasIndex;
        	this.aliasIndex++;
        }
    }
}