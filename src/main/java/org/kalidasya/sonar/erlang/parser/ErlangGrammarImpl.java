package org.kalidasya.sonar.erlang.parser;

import org.kalidasya.sonar.erlang.api.ErlangGrammar;
import org.kalidasya.sonar.erlang.api.ErlangKeyword;
import org.kalidasya.sonar.erlang.api.ErlangPunctator;
import org.kalidasya.sonar.erlang.api.ErlangTokenType;

import com.sonar.sslr.api.GenericTokenType;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.api.GenericTokenType.LITERAL;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.next;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;

public class ErlangGrammarImpl extends ErlangGrammar {

	

	public ErlangGrammarImpl() {
		module();
		functions();
		statements();
		dataTypes();
		operators();
		guards();
	}

	private void module() {
		module.is(
			one2n(
				or(
					moduleAttribute, 
					typeFunctionSpec
				)
			), 
			one2n(
				functionDeclaration
			), 
			EOF
		);
		moduleAttribute.is(
				ErlangPunctator.MINUS, 
				IDENTIFIER, 
				ErlangPunctator.LPARENTHESIS, 
				or(
					funcExport,
					listedTermsOrFunCalls,
					LITERAL,
					IDENTIFIER
				),
				ErlangPunctator.RPARENTHESIS, 
			ErlangPunctator.DOT
		);
		typeFunctionSpec.is(
			ErlangPunctator.MINUS, 
			IDENTIFIER,
			funcCall,
			or(
				and(
					ErlangPunctator.COLON,
					ErlangPunctator.COLON,
					funcCall,
					o2n(
						ErlangPunctator.PIPE,
						funcCall
					)
				),
				and(
					ErlangPunctator.ARROW,
					funcCall
				)
			),
			ErlangPunctator.DOT
		);
		//TODO: is it possible to have something like: -export().?
		funcExport.is(
			or(
				and(
					ErlangPunctator.LBRACKET,
					o2n(
						funcArity,
						o2n(
							ErlangPunctator.COMMA,
							funcArity
						)
					),
					ErlangPunctator.RBRACKET
				), 
				funcArity
			)
		);
	}

	private void statements(){
		arithmeticExp.is(
			expression,
			one2n(
				arithmeticOp,
				expression
			)
		);
		
		termCompareExp.is(termsOrFunCalls, termCompOp, termsOrFunCalls);
		
		booleanExp.is(termsOrFunCalls, booleanOp, termsOrFunCalls);
		
		shortcircuitExp.is(
			or(
				and(
					ErlangPunctator.LPARENTHESIS, 
					expression, 
					o2n(shortcircuitOp, shortcircuitExp), 
					ErlangPunctator.RPARENTHESIS
				),
				and(
					expression, 
					o2n(shortcircuitOp, shortcircuitExp) 
				)
			)
			
		);
		listExp.is(termsOrFunCalls, one2n(listOp, termsOrFunCalls));
		expression.is(
			or(
				and(
					ErlangPunctator.LPARENTHESIS,
					or(
						expression
					),
					ErlangPunctator.RPARENTHESIS
				),
				possibleExpressions
			)
		);
		possibleExpressions.is(
			or(
				arithmeticExp, listExp, matchExp, termCompareExp, booleanExp, funcCall, listCompExp, recordRef, funExpr, flowExp, term 
			)
		);
		flowExp.is(or(ifExp, caseExp, receiveExp));
		caseExp.is(
			ErlangKeyword.CASE, 
			or(
				and(
					ErlangPunctator.LPARENTHESIS,
					expression,
					ErlangPunctator.RPARENTHESIS
				),
				expression
			),
			ErlangKeyword.OF, 
			casePattern,
			o2n(ErlangPunctator.SEMI, casePattern),
			ErlangKeyword.END
		);
		
		casePattern.is(
			pattern,
			opt(guardSequenceStart),
			ErlangPunctator.ARROW,
			expression,
			o2n(
				ErlangPunctator.COMMA,
				expression
			)
		);
		
		ifExp.is(
			ErlangKeyword.IF, 
			one2n(
				branchPatternExp
			)
		);
		
		receiveExp.is(ErlangKeyword.RECEIVE, one2n(branchPatternExp), ErlangKeyword.END);
		
		branchExpression.is(
			expression,
			o2n(
				ErlangPunctator.COMMA,
				expression
			)
		);
		
		pattern.is(
			term
		);
		
		recordRef.is(
			or(
				recordSet,
				recordAcc
			)
		);
		
		recordSet.is(
			opt(IDENTIFIER),
			ErlangPunctator.NUMBERSIGN,
			IDENTIFIER,
			ErlangPunctator.LCURLYBRACE,
			matchExp,
			o2n(
				ErlangPunctator.COMMA,
				matchExp
			),
			ErlangPunctator.RCURLYBRACE
		);
		matchExp.is(
			term,
			ErlangPunctator.MATCHOP,
			expression
		);
		recordAcc.is(
			opt(IDENTIFIER),
			ErlangPunctator.NUMBERSIGN,
			IDENTIFIER,
			o2n(
				ErlangPunctator.DOT,
				IDENTIFIER
			)
		);
	
		listCompExp.is(
			ErlangPunctator.LBRACKET,
			expression,
			ErlangPunctator.LISTCOMP,
			one2n(qualifier),
			ErlangPunctator.RBRACKET
		);
		
		qualifier.is(
			or(
				and(
					expression,
					ErlangPunctator.ARROWBACK,
					expression
				),
				expression
			)
		);
	}
	
	private void guards(){
		guardSequenceStart.is(
				ErlangKeyword.WHEN, 
				guardSequence
			);
		guardSequence.is(
			guard,
			o2n(
				ErlangPunctator.SEMI,
				guard
			)
		);
		guard.is(
			guardExpression,
			o2n(
				ErlangPunctator.COMMA, 
				guardExpression
			)
		);
		guardExpression.is(
			or(
				termCompareExp, 
				arithmeticExp, 
				booleanExp, 
				shortcircuitExp, 
				funcCall,
				IDENTIFIER
			)
		);
	}
	
	private void dataTypes(){
		term.is(or(LITERAL, ErlangTokenType.NUMERIC_LITERAL, list, tuple, recordRef, IDENTIFIER));
		termsOrFunCalls.is(
			or(
				funcCall,
				term
			)
		);
		listedTermsOrFunCalls.is(
			and(
				termsOrFunCalls,
				o2n(
					ErlangPunctator.COMMA,
					termsOrFunCalls
				)
			)
		);
		tuple.is(
			ErlangPunctator.LCURLYBRACE, 
			o2n(
				listedTermsOrFunCalls
			),
			ErlangPunctator.RCURLYBRACE
		);
		list.is(
			ErlangPunctator.LBRACKET, 
			o2n(
				listedTermsOrFunCalls,
				o2n(
					ErlangPunctator.PIPE, 
					listedTermsOrFunCalls
				)
			),
			ErlangPunctator.RBRACKET
		);
	}
	
	private void operators(){
		termCompOp.is(
			or(
				ErlangPunctator.EQUAL, 
				ErlangPunctator.EQUAL2, 
				ErlangPunctator.NOTEQUAL, 
				ErlangPunctator.NOTEQUAL2, 
				ErlangPunctator.LT, 
				ErlangPunctator.GT, 
				ErlangPunctator.LE, 
				ErlangPunctator.GE
			)
		);
		booleanOp.is(or(
			ErlangKeyword.NOT, 
			ErlangKeyword.AND, 
			ErlangKeyword.OR, 
			ErlangKeyword.XOR
		));
		arithmeticOp.is(or(
			ErlangPunctator.PLUS, 
			ErlangPunctator.MINUS, 
			ErlangPunctator.STAR, 
			ErlangPunctator.DIV, 
			ErlangKeyword.BNOT, 
			ErlangKeyword.DIV, 
			ErlangKeyword.REM, 
			ErlangKeyword.BAND, 
			ErlangKeyword.BOR,
			ErlangKeyword.BXOR, 
			ErlangKeyword.BSL, 
			ErlangKeyword.BSR,
			ErlangPunctator.NUMBERSIGN
		));
		listOp.is(or(ErlangPunctator.PLUSPLUS, ErlangPunctator.MINUSMINUS));
		shortcircuitOp.is(or(ErlangKeyword.ANDALSO, ErlangKeyword.ORELSE));
	}
	
	private void functions() {
		functionDeclaration.is(
			functionClause, 
			o2n(
				ErlangPunctator.SEMI,
				functionClause
			), 
			
			ErlangPunctator.DOT
		);
		functionClause.is(clauseHead, ErlangPunctator.ARROW, clauseBody);
		clauseHead.is(
			funcDecl,
			opt(guardSequenceStart)
		);
		clauseBody.is(
			expression,
			o2n(
				ErlangPunctator.COMMA,
				expression
			)
		);
		
		funcArity.is(
			opt(
				IDENTIFIER,
				ErlangPunctator.COLON
			),
			IDENTIFIER,
			ErlangPunctator.DIV,
			ErlangTokenType.NUMERIC_LITERAL
		);
		/*
		 * What can be in a pattern?
		 */
		funcDecl.is(
			IDENTIFIER,
			ErlangPunctator.LPARENTHESIS, 
			opt(
				pattern,
				o2n(
					ErlangPunctator.COMMA,
					pattern
				)
			),
			ErlangPunctator.RPARENTHESIS
		);
		funcCall.is(
			opt(IDENTIFIER, ErlangPunctator.COLON),
			IDENTIFIER, 
			funcArgs 
		);
		funcArgs.is(
			ErlangPunctator.LPARENTHESIS, 
			opt(
				expression,
				o2n(
					ErlangPunctator.COMMA,
					expression
				)
			),
			ErlangPunctator.RPARENTHESIS
		);
		funExpr.is(
			ErlangKeyword.FUN,
			or(
				and(
					funcArgs,
					opt(guardSequenceStart),
					ErlangPunctator.ARROW,
					expression,
					o2n(
						ErlangPunctator.COMMA,
						expression
					),
					ErlangKeyword.END
				),
				funcArity
			)
		);
	}
}
