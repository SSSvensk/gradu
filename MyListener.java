package com.cypherTest.cypherTest;

import java.util.ArrayList;

public class MyListener extends xpathBaseListener {

	private String query;
	private Object lastElement;
	private Object conditionAppliesTo;
	private Object returnItem;
    private Object returnAttribute;
	private boolean attribute;
    private Object primExp;
    private boolean isNode;
    private boolean firstNode;
    private boolean someText;
    private boolean insidePredicate;
    private Object predicateKey;
    private Object predicateValue;
    private boolean stepClosed;
    private boolean firstPredicateItem;
    private boolean allesOK;
    private boolean visitAbbStep = false;
    private boolean visitNameTest = false;
    public StringBuilder cypherQuery = new StringBuilder();
    private Object secondLastElement;

    private ArrayList andSteps = new ArrayList(); 
    private StringBuilder andQuery = new StringBuilder();
    private ArrayList andParts = new ArrayList();

    private ArrayList orSteps = new ArrayList();
    private StringBuilder whereQuery = new StringBuilder();

    private int aliasIndex = 0;
    private int randomIndex = 0;
    private String lastRandom = "";

	public MyListener() {
        this.query = "";
    }

    public void setQuery(Object s) {
    	this.query = this.query + s;
    }

    public Object getQuery() {
    	return query;
    }

    @Override
    public void exitMain(xpathParser.MainContext ctx) {
    	if (whereQuery.length() > 0) {
    		cypherQuery.append(" WHERE " + whereQuery);
    	}
        StringBuilder sb = new StringBuilder();
        sb.append(this.lastElement);

        if (sb.toString().equals("*")) {
            cypherQuery.append(" RETURN (" + this.lastRandom + ")");
        } else {
            if (this.returnAttribute != null) {
                cypherQuery.append(" RETURN (" + this.lastElement + "." + this.returnAttribute + ")");
            } else {
                cypherQuery.append(" RETURN (" + this.lastElement + ")");
            }
        }
        
        
    }

    @Override
    public void enterAbsoluteLocationPathNoroot(xpathParser.AbsoluteLocationPathNorootContext ctx) {
        cypherQuery.append("MATCH ");
        this.isNode = true;
        this.firstNode = true;
    }

    @Override
    public void exitAbsoluteLocationPathNoroot(xpathParser.AbsoluteLocationPathNorootContext ctx) {
        char c = cypherQuery.charAt(cypherQuery.length() - 1);
        if (c == '-') {
            cypherQuery.setLength(cypherQuery.length() - 1);
        }
    }

    @Override
    public void exitNCName(xpathParser.NCNameContext ctx) {

        //Jos kyseessä on attribuutti...
        if (this.attribute && this.insidePredicate) {
            this.andSteps.add(ctx.getChild(0));
            this.orSteps.add(ctx.getChild(0));

            //REMEMBER!!!
            if (this.andSteps.size() < 2 && this.orSteps.size() < 2) {
                this.isNode = !this.isNode;
            }
            
            //cypherQuery.append(ctx.getChild(0));
        } else if (this.attribute && !this.insidePredicate) {
            this.returnAttribute = ctx.getChild(0);
        } else if (!this.attribute && this.insidePredicate && this.firstPredicateItem) {
            if (this.isNode) {
                cypherQuery.append("]");
            } else {
                cypherQuery.append(")");
            }

            //Attribuutti kertomaan, että on jo suljettu
            this.allesOK = true;
            this.firstPredicateItem = false;
        } else if (this.attribute && !this.insidePredicate) {
            cypherQuery.append(")");
        }

        //Jos ollaan ensimmäisessä kyselyn elemetissä
        if (this.isNode && this.firstNode) {
            cypherQuery.append("(alias" + aliasIndex + ":" + ctx.getChild(0));
            //this.isNode = false;
            this.firstNode = false;
        } else {

            if (this.isNode && !this.attribute && !this.visitAbbStep) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.lastElement);

                if (sb.toString().equals("*")) {
                    cypherQuery.setLength(cypherQuery.length() - 12);
                    cypherQuery.append("-->");
                }
                cypherQuery.append("(alias" + aliasIndex + ":" + ctx.getChild(0));
                //this.isNode = false;
            } else if (!this.isNode && !this.attribute) {
                //-[
                char c = cypherQuery.charAt(cypherQuery.length() - 1);
                if (c == '-') {
                   cypherQuery.setLength(cypherQuery.length() - 1);
                }
                cypherQuery.append("-[alias" + aliasIndex + ":" + ctx.getChild(0));
                //this.isNode = true;
            }
        }

        if (!this.insidePredicate && !this.attribute) {
            this.lastElement = "alias" + aliasIndex;
            aliasIndex++;
            //this.lastElement = ctx.getChild(0);
        }

        
    }

    @Override
    public void exitRelativeLocationPath(xpathParser.RelativeLocationPathContext ctx) {
        if (this.isNode && !this.insidePredicate) {
            cypherQuery.append("()");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getChild(1));
        System.out.println(sb);
        
        if (sb.toString().equals("//")) {
        	System.out.println("momomo");
        }
    }

    @Override
    public void enterStep(xpathParser.StepContext ctx) {
        this.stepClosed = false;
    }

    @Override
    public void exitStep(xpathParser.StepContext ctx) {

        boolean tr = false;

        if (this.visitAbbStep) {
            cypherQuery.setLength(cypherQuery.length() - 1);
            cypherQuery.append("<--");
            this.visitAbbStep = false;
            this.isNode = !this.isNode;
            this.stepClosed = true;
            tr = true;
        } else if (this.visitNameTest) {
            cypherQuery.setLength(cypherQuery.length() - 1);
            if (this.firstNode) {
                this.firstNode = false;
                cypherQuery.append(" (random" + this.randomIndex + ")");
                this.lastRandom = "random" + this.randomIndex;
                this.randomIndex++;
            } else if (!this.isNode) {
                StringBuilder sb = new StringBuilder();
                StringBuilder ssb = new StringBuilder();
                ssb.append(this.secondLastElement);
                sb.append(this.lastElement);
                if (ssb.toString().equals("*")) {
                    cypherQuery.append(")");
                }
                cypherQuery.append("-[random"+this.randomIndex+"]->");
                this.lastRandom = "random" + this.randomIndex;
                this.randomIndex++;
            } else if(this.isNode) {
                cypherQuery.append(">(random"+this.randomIndex+")");
                this.lastRandom = "random" + this.randomIndex;
                this.randomIndex++;
            }
            
            this.visitNameTest = false;
            this.isNode = !this.isNode;
            this.stepClosed = true;
            tr = true;
        }

        //Jos askelta ei olla vielä suljettu, eikä olla predikaatin sisällä suljetaan solmu.
        if (!this.attribute && !this.stepClosed && !this.insidePredicate && this.isNode && !this.allesOK) {
            cypherQuery.append(")");
            this.isNode = !this.isNode;
            this.stepClosed = true;
            if (!tr) {
                cypherQuery.append("-");
            }
        } else if (!this.attribute && !this.stepClosed && !this.insidePredicate && !this.isNode && !this.allesOK) {
            cypherQuery.append("]");
            this.isNode = !this.isNode;
            this.stepClosed = true;
            if (!tr) {
                cypherQuery.append("->");
            }
        } else if (!this.attribute && !this.stepClosed && this.insidePredicate && !this.isNode) {
            cypherQuery.append("]");
            this.isNode = !this.isNode;
            this.stepClosed = true;
            if (!tr) {
                cypherQuery.append("->");
            }
        } else if (!this.attribute && !this.stepClosed && this.insidePredicate && this.isNode) {
            cypherQuery.append(")");
            this.isNode = !this.isNode;
            this.stepClosed = true;
            if (!tr) {
                cypherQuery.append("-");
            }
        }
    }

    @Override
    public void exitPrimaryExpr(xpathParser.PrimaryExprContext ctx) {
    	this.predicateValue = ctx.getChild(0);
    }

    @Override
    public void exitAxisSpecifier(xpathParser.AxisSpecifierContext ctx) {

        //Jos lapsi ei ole null, on havaittu @-merkki, jolloin elementti on attribuutti.
    	if (ctx.getChild(0) != null) {
    		this.attribute = true;
    		/*
            if (this.andSteps.size() < 1 && this.orSteps.size() < 1 && this.insidePredicate) {
                cypherQuery.append(" {");
            }
            */
            
    	}
    }

    @Override
    public void exitEqualityExpr(xpathParser.EqualityExprContext ctx) {
        //Jos kyseessä on attribuutti, lisätään operaattori ja attribuutin arvo.
        if (this.attribute) {
            StringBuilder eq = new StringBuilder();
            eq.append(ctx.getChild(1));
            String equalOrInEqual = eq.toString();
            if (equalOrInEqual.equals("=")) {
                //cypherQuery.append("=" + this.predicateValue);
                this.andSteps.set(this.andSteps.size()-1, this.andSteps.get(this.andSteps.size()-1) + " = " + this.predicateValue);
            } else if (equalOrInEqual.equals("!=")) {
                //cypherQuery.append("<>" + this.predicateValue);
                this.andSteps.set(this.andSteps.size()-1, this.andSteps.get(this.andSteps.size()-1) + "<>" + this.predicateValue);
            }
        }
    }

    @Override
    public void exitRelationalExpr(xpathParser.RelationalExprContext ctx) {
        //Jos kyseessä on attribuutti, lisätään operaattori ja attribuutin arvo.
        if (this.attribute) {
            StringBuilder eq = new StringBuilder();
            eq.append(ctx.getChild(1));
            String equalOrInEqual = eq.toString();
            if (equalOrInEqual.equals(">")) {
                //cypherQuery.append(">" + this.predicateValue);
                this.andSteps.set(this.andSteps.size()-1, this.andSteps.get(this.andSteps.size()-1) + " > " + this.predicateValue);
            } else if (equalOrInEqual.equals("<")) {
                //cypherQuery.append("<" + this.predicateValue);
                this.andSteps.set(this.andSteps.size()-1, this.andSteps.get(this.andSteps.size()-1) + " < " + this.predicateValue);
            }
        }
    }

    @Override
    public void enterAndExpr(xpathParser.AndExprContext ctx) {
        //And-operaattorila on aina pariton määrä lapsia, joista parittomat luvut ovat "and"-operaattoreita
        andSteps = new ArrayList();
    }


    @Override
    public void exitAndExpr(xpathParser.AndExprContext ctx) {
        //And-operaattorila on aina pariton määrä lapsia, joista parittomat luvut ovat "and"-operaattoreita
        if (this.andSteps.size() > 1) {
            for (int i = 0; i < this.andSteps.size(); i++) {
                if (i > 0) {
                    andQuery.append(" AND ");
                }
                andQuery.append(this.conditionAppliesTo + "." + this.andSteps.get(i));

            }
        } else if (this.andSteps.size() == 1) {
            andQuery.append(this.conditionAppliesTo + "." + this.andSteps.get(0));
        }
        this.andSteps.clear();
        andParts.add(andQuery);
        andQuery = new StringBuilder();
    }

    @Override
    public void enterOrExpr(xpathParser.OrExprContext ctx) {
        //And-operaattorila on aina pariton määrä lapsia, joista parittomat luvut ovat "and"-operaattoreita
        orSteps = new ArrayList();
    }


    @Override
    public void exitOrExpr(xpathParser.OrExprContext ctx) {
        //And-operaattorila on aina pariton määrä lapsia, joista parittomat luvut ovat "and"-operaattoreita
        if (this.andParts.size() > 1) {
            for (int i = 0; i < this.andParts.size(); i++) {
                if (i > 0) {
                    whereQuery.append(" OR ");
                }
                whereQuery.append(this.andParts.get(i));

            }
        } else if (this.andParts.size() == 1) {
            whereQuery.append(this.andParts.get(0));
        }
        this.orSteps.clear();
        this.andParts.clear();
    }


    @Override
    public void enterNodeTest(xpathParser.NodeTestContext ctx) {
        this.someText = false;
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
        //System.out.println(this.firstNode);
        g.append(ctx.getChild(0));
        if (g.toString().equals("*")) {
            this.secondLastElement = this.lastElement;
            this.lastElement = ctx.getChild(0);
            this.visitNameTest = true;

        }
        
    }

    @Override
    public void enterPredicate(xpathParser.PredicateContext ctx) {
    	this.conditionAppliesTo = this.lastElement;
        this.isNode = !this.isNode;
        this.insidePredicate = true;
        this.firstPredicateItem = true;
    }

    @Override
    public void exitPredicate(xpathParser.PredicateContext ctx) {
    	this.lastElement = this.conditionAppliesTo;
        this.insidePredicate = false;
        this.stepClosed = false;
        //this.isNode = !this.isNode;
    }

    @Override
    public void exitAbbreviatedStep(xpathParser.AbbreviatedStepContext ctx) {
    	this.visitAbbStep = true;
    }
}
