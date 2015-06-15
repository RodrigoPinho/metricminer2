/**
 * Copyright 2014 Maurício Aniche

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.metricminer2.metric.java8.cc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;

import br.com.metricminer2.parser.java8.Java8AntlrMethods;
import br.com.metricminer2.parser.java8.Java8BaseListener;
import br.com.metricminer2.parser.java8.Java8Parser;
import br.com.metricminer2.parser.java8.Java8Parser.ExpressionContext;
import br.com.metricminer2.parser.java8.Java8Parser.StaticInitializerContext;

public class AntlrCCListener extends Java8BaseListener {

    private Map<String, Integer> ccPerMethod;
    private Stack<String> methodStack;

    public AntlrCCListener() {
        ccPerMethod = new HashMap<String, Integer>();
        methodStack = new Stack<String>();
    }
    
	@Override public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {
		String methodName = Java8AntlrMethods.fullMethodName(ctx);
		methodStack.push(methodName);
		increaseCc();
	}
	@Override public void exitConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) { 
		methodStack.pop();
	}

	@Override public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
		String methodName = Java8AntlrMethods.fullMethodName(ctx);
		methodStack.push(methodName);
		increaseCc();
	}
	
	@Override public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) { 
		methodStack.pop();
	}

	
	@Override public void enterForStatement(@NotNull Java8Parser.ForStatementContext ctx) {
		increaseCc();
	}
	
	@Override public void enterEnhancedForStatementNoShortIf(@NotNull Java8Parser.EnhancedForStatementNoShortIfContext ctx) {
		increaseCc();
	}

	@Override public void enterIfThenStatement(@NotNull Java8Parser.IfThenStatementContext ctx) {
		findAndOr(ctx.expression());
		increaseCc();
	}
	
	@Override public void enterStaticInitializer(StaticInitializerContext ctx) {
		methodStack.push("(static block)");
		increaseCc();
	};

	@Override public void exitStaticInitializer(StaticInitializerContext ctx) {
		methodStack.pop();
	};
	
	@Override public void enterConditionalExpression(@NotNull Java8Parser.ConditionalExpressionContext ctx) {
		if(ctx.ifTernaryExpression()!=null) increaseCc();
	}
	
	@Override public void enterIfThenElseStatementNoShortIf(@NotNull Java8Parser.IfThenElseStatementNoShortIfContext ctx) {
		findAndOr(ctx.expression());
		increaseCc();
	}
	
	@Override public void enterIfThenElseStatement(@NotNull Java8Parser.IfThenElseStatementContext ctx) {
		findAndOr(ctx.expression());
		increaseCc();
	}

	private void findAndOr(ExpressionContext exp) {
		if(exp==null) return;
		
		
		for(int i = 0; i < exp.getChildCount(); i++) {
			if(exp.getChild(i).getClass().equals(ExpressionContext.class)) {
				findAndOr((ExpressionContext)exp.getChild(i));
			}
			
			String expr = exp.getChild(i).getText().replace("&&", "&").replace("||", "|");
			int ands = StringUtils.countMatches(expr, "&");
			int ors = StringUtils.countMatches(expr, "|");
			
			increaseCc(ands + ors);
			
		}
	}
	
	@Override public void enterExpression(Java8Parser.ExpressionContext ctx) {
		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(ctx.getChild(i).getText().equals("?")) increaseCc();
		}
	}
	
	@Override public void enterWhileStatement(@NotNull Java8Parser.WhileStatementContext ctx) { 
		increaseCc();
	}
	
	@Override public void enterSwitchStatement(@NotNull Java8Parser.SwitchStatementContext ctx) {
		
		for(int i=0; i< ctx.switchBlock().switchBlockStatementGroup().size();i++) {
			if(!ctx.switchBlock().switchBlockStatementGroup().get(i).getText().startsWith("default:")) {
				increaseCc();
			}
		}
		
	}

	@Override public void enterDoStatement(@NotNull Java8Parser.DoStatementContext ctx) { 
		increaseCc();
		
	}
	@Override public void enterCatchClause(Java8Parser.CatchClauseContext ctx) { 
		increaseCc();
		
	}

    private void increaseCc() {
    	increaseCc(1);
    }

    private void increaseCc(int qtd) {
    	
    	String currentMethod = methodStack.peek();
    	if (!ccPerMethod.containsKey(currentMethod))
    		ccPerMethod.put(currentMethod, 0);
    	
    	ccPerMethod.put(currentMethod, ccPerMethod.get(currentMethod) + qtd);
    	
    }

    public int getCc() {
        int cc = 0;
        for (Entry<String, Integer> method : ccPerMethod.entrySet()) {
            cc += method.getValue();
        }
        return cc;
    }

    public int getCc(String method) {
        return ccPerMethod.get(method);
    }

    public double getAvgCc() {
        double avg = 0;

        for (Entry<String, Integer> method : ccPerMethod.entrySet()) {
            avg += method.getValue();
        }

        return avg / ccPerMethod.size();
    }

    public Map<String, Integer> getCcPerMethod() {
        return ccPerMethod;
    }

}
