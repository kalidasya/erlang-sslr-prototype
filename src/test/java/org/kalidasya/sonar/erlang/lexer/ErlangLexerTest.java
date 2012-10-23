package org.kalidasya.sonar.erlang.lexer;

import java.nio.charset.Charset;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kalidasya.sonar.erlang.ErlangConfiguration;
import org.kalidasya.sonar.erlang.api.ErlangKeyword;
import org.kalidasya.sonar.erlang.api.ErlangPunctator;
import org.kalidasya.sonar.erlang.api.ErlangTokenType;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.UnknownCharacterChannel;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasTokens;
import static org.junit.Assert.assertThat;

public class ErlangLexerTest {

	private static Lexer lexer;

	@BeforeClass
	public static void init() {
		lexer = ErlangLexer.create(new ErlangConfiguration(Charset.forName("UTF-8")));
	}

	@Test
	public void lexInlineComment() {
		assertThat(lexer.lex("% My Comment \n new line"), hasComment("% My Comment "));
		assertThat(lexer.lex("%"), hasComment("%"));
	}

	@Test
	  public void decimalLiteral() {
	    assertThat(lexer.lex("0"), hasToken("0", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("123"), hasToken("123", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("123.456"), hasToken("123.456", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("123.456e10"), hasToken("123.456e10", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("123.456e-10"), hasToken("123.456e-10", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("123.456E10"), hasToken("123.456E10", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("123.456E-10"), hasToken("123.456E-10", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("0.123"), hasToken("0.123", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("0.123e4"), hasToken("0.123e4", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("0.123e-4"), hasToken("0.123e-4", ErlangTokenType.NUMERIC_LITERAL));

	    assertThat(lexer.lex("0.123E4"), hasToken("0.123E4", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("0.123E-4"), hasToken("0.123E-4", ErlangTokenType.NUMERIC_LITERAL));
	    
	    assertThat(lexer.lex("2#12"), hasToken("2#12", ErlangTokenType.NUMERIC_LITERAL));
	    assertThat(lexer.lex("16#1f"), hasToken("16#1f", ErlangTokenType.NUMERIC_LITERAL));
	  }

	  @Test
	  public void hexAtomLiteral() {
	    assertThat(lexer.lex("'find/me'"), hasToken("'find/me'", ErlangTokenType.ATOM));
	    assertThat(lexer.lex("whatShallW3d0"), hasToken("whatShallW3d0", ErlangTokenType.ATOM));
	  }
	
	  @Test
	  public void stringLiteral() {
	    assertThat("empty", lexer.lex("\"\""), hasToken("\"\"", GenericTokenType.LITERAL));

	    assertThat(lexer.lex("\"hello world\""), hasToken("\"hello world\"", GenericTokenType.LITERAL));

	    assertThat("escaped double quote", lexer.lex("\"\\\"\""), hasToken("\"\\\"\"", GenericTokenType.LITERAL));

	    assertThat("multiline", lexer.lex("\"\\\n\""), hasToken("\"\\\n\"", GenericTokenType.LITERAL));
	  }

	  @Test
	  public void booleanLiteral() {
	    assertThat(lexer.lex("false"), hasToken("false", ErlangTokenType.ATOM));
	    assertThat(lexer.lex("true"), hasToken("true", ErlangTokenType.ATOM));
	  }

	  @Test
	  public void identifier() {
	    assertThat(lexer.lex("$"), hasToken("$", ErlangPunctator.DOLLAR));
	    assertThat(lexer.lex("'"), hasToken("'", ErlangPunctator.APOSTROPHE));
	  }

	  @Test
	  public void custom() {
		  //assertThat(lexer.lex("-module(m)."), hasTokens("-","module","(","m",")","."));
		  assertThat(lexer.lex("module"), hasToken("module", ErlangTokenType.ATOM));
		  assertThat(lexer.lex("m"), hasToken("m", ErlangTokenType.ATOM));
		  assertThat(lexer.lex("_"), hasToken("_", ErlangTokenType.ATOM));
		  assertThat(lexer.lex("ASDodule"), hasToken("ASDodule", GenericTokenType.IDENTIFIER));
		  assertThat(lexer.lex("A"), hasToken("A", GenericTokenType.IDENTIFIER));
		  assertThat(lexer.lex("Aodule"), hasToken("Aodule", GenericTokenType.IDENTIFIER));
	  }
	  
	  @Test
	  public void bom() {
	    assertThat(lexer.lex(Character.toString(UnknownCharacterChannel.BOM_CHAR)), hasTokens("EOF"));
	  }
	
}
