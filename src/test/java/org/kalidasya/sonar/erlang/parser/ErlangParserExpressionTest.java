package org.kalidasya.sonar.erlang.parser;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kalidasya.sonar.erlang.ErlangConfiguration;
import org.kalidasya.sonar.erlang.api.ErlangGrammar2;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.events.ExtendedStackTrace;
import com.sonar.sslr.impl.events.ExtendedStackTraceStream;

public class ErlangParserExpressionTest {
	ExtendedStackTrace listener = new ExtendedStackTrace();
	Parser<ErlangGrammar2> p = ErlangParser2.create(new ErlangConfiguration(Charsets.UTF_8),
			listener);

	ErlangGrammar2 g = p.getGrammar();

	@Before
	public void init() {
		p.setRootRule(g.expression);
	}

	@Test
	public void simpleExpression() {
		assertThat(p, parse(code("1+3")));
		assertThat(p, parse(code("true")));
		assertThat(p, parse(code("6 + 5 * 4 - 3 / 2")));
		assertThat(p, parse(code("ok")));
	}

	@Test
	public void simpleExpressionWithP() {
		assertThat(p, parse(code("(1+3)")));
		assertThat(p, parse(code("(6 + 5) * ((4 - 3) / 2)")));

	}

	@Test
	public void varMatch() {
		assertThat(p, parse(code("A=2")));
		assertThat(p, parse(code("B=[2,3]")));
		assertThat(p, parse(code("B={2,3}")));

	}

	@Test
	public void listExpression() {
		assertThat(p, parse(code("[asd,ore,[ow,2,3],[hello,23]]")));
		assertThat(p, parse(code("[]")));
		assertThat(p, parse(code("[d|T]")));
		assertThat(p, parse(code("[c|[]]")));
		assertThat(p, parse(code("[a|[b|[c|[]]]]")));
		assertThat(p, parse(code("[a,2,{c,4}]")));
		assertThat(
				p,
				parse(code("[Name,proplists:get_value(description,Spec,[])|proplists:get_value(keywords,Spec,[])]")));

	}

	@Test
	public void tupleExpression() {
		assertThat(p, parse(code("{asd,ore,{ow,[2,23,as],3},[hello,{23,as}]}")));

	}

	@Test
	public void listComprehensionExpressin() {
		assertThat(p, parse(code("[X*2 || X <- [1,2,3]]")));
		assertThat(p, parse(code("[X*2 || X <- [1,2,3]] ++ [7,8,9]")));
		assertThat(p, parse(code("[X*2 || X <- [1,2,3]] -- [7,8,9]")));
		assertThat(p, parse(code("[10, 23] -- [X*2 || X <- [1,2,3]] ++ [7,8,9]")));
		assertThat(p, parse(code("[756, 877] ++ [X*2 || X <- [1,2,3]] -- [7,8,9]")));
	}

	@Test
	public void logicalExpressions() {
		assertThat(p, parse(code("not true")));
		assertThat(p, parse(code("true and false")));
		assertThat(p, parse(code("true xor false")));
		assertThat(p, parse(code("true or A")));
		assertThat(p, parse(code("A orelse B")));
		assertThat(p, parse(code("A andalso B")));
		assertThat(p, parse(code("not A andalso B or false")));
		assertThat(p, parse(code("(not (A andalso B)) or false")));
	}

	@Test
	public void binaryExpressions() {
		assertThat(p, parse(code("<<1,17,42>>")));
		assertThat(p, parse(code("<<1,17,42:16>>")));
		assertThat(p, parse(code("<<1024/utf8>>")));
		assertThat(p, parse(code("<<1024:16/utf8>>")));
		assertThat(p, parse(code("<<$a,$b,$c>>")));
		assertThat(p, parse(code("<<\"hello\">>")));
		assertThat(p, parse(code("<<A,B,C:16>> = <<1,17,42:16>>")));
		assertThat(p, parse(code("<<D:16,E,F>> = <<1,17,42:16>>")));
		assertThat(p, parse(code("<<G,H/binary>> = <<1,17,42:16>>")));
		assertThat(p, parse(code("<<G,H/bitstring>> = <<1,17,42:12>>")));
		assertThat(p, parse(code("<< << (X*2) >> || <<X>> <= << 1,2,3 >> >>")));
	}

	@Test
	public void functionCall() {
		assertThat(p, parse(code("method(\"hello\")")));
		assertThat(p, parse(code("method(12)")));
		assertThat(p, parse(code("method(\"hello\",234234)")));
		assertThat(p, parse(code("haho:method(\"hello\")")));
		assertThat(p, parse(code("io:format(\"assert error in module ~p on line ~p~n\")")));
	}

	@Test
	public void catchExpressions() {
		assertThat(p, parse(code("catch 1+2")));
		assertThat(p, parse(code("catch 1+a")));
		assertThat(p, parse(code("A = (catch 1+2)")));
		assertThat(p, parse(code("catch throw(hello)")));
	}

	@Test
	public void recordCreate() {
		assertThat(p, parse(code("#Name{Field1=Expr1,Field2=Expr2,FieldK=ExprK}")));
		assertThat(p, parse(code("#person{name=Name, _='_'}")));
		assertThat(p, parse(code("A = #Name{Field1=Expr1,Field2=Expr2,FieldK=ExprK}")));
		assertThat(p, parse(code("S = #person{name=Name, _='_'}")));
		assertThat(p, parse(code("User#user{ibuttons = User#user.ibuttons ++ [IButton]}")));
	}

	@Test
	public void recordAccess() {
		assertThat(p, parse(code("#person.name")));
		assertThat(p, parse(code("Expr#Name.Field")));
		assertThat(p, parse(code("N2#nrec2.nrec1#nrec1.nrec0.nrec00#nrec0.name.first")));
		assertThat(p, parse(code("N2#nrec2.nrec1#nrec1.nrec0#nrec0{name = \"nested0a\"}")));

	}

	@Test
	public void macroUse() {
		assertThat(p, parse(code("?TIMEOUT")));
		assertThat(p, parse(code("?MACRO1(a, b)")));
		assertThat(p, parse(code("?MACRO1(X, 123)")));
		assertThat(p, parse(code("server:call(refserver, Request, ?TIMEOUT)")));
		assertThat(p, parse(code("server:call(refserver, Request, ?MACRO1(a, b))")));

	}

	
	private static String code(String... lines) {
		return Joiner.on("\n").join(lines);
	}

	@After
	public void log() {
		try {
			ExtendedStackTraceStream.print(listener, System.out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
