package org.graalphp.benchmark;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.develnext.jphp.core.syntax.SyntaxAnalyzer;
import org.develnext.jphp.core.tokenizer.token.Token;
import org.eclipse.php.core.PHPVersion;
import org.eclipse.php.core.ast.error.BailoutErrorListener;
import org.eclipse.php.core.ast.error.ConsoleErrorListener;
import org.eclipse.php.core.ast.nodes.ASTParser;
import org.eclipse.php.core.ast.nodes.Program;
import org.graalphp.benchmark.antrl.PhpLexer;
import org.graalphp.benchmark.antrl.PhpParser;
import org.junit.Test;
import php.runtime.common.LangMode;
import php.runtime.env.Context;
import php.runtime.env.Environment;

import org.develnext.jphp.core.tokenizer.Tokenizer;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author abertschi
 */
public class BenchmarkTests {

    private String code0;
    private String code1;
    private String code2;
    private String code3;
    private String code4;
    private String code5;

    public BenchmarkTests() {
        code0 = readFile("phpoffice/Xls.php");
        code1 = readFile("phpoffice/Comment.php");
        code2 = readFile("symfony/FrameworkExtension.php");
        code3 = readFile("symfony/RequestTest.php");
        code4 = readFile("benchmarkgame/fasta.php-2.php");
        code5 = readFile("benchmarkgame/fannkuchredux.php-1.php");
    }

    @Test
    public void jphpParserTest() throws IOException {
        String code = code5.replace("<?php", "");
        long l1 = System.currentTimeMillis();
        Environment environment = new Environment();
        Tokenizer tokenizer = new Tokenizer(new Context(code));
        environment.scope.setLangMode(LangMode.DEFAULT);
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer(environment, tokenizer);

        long l2 = System.currentTimeMillis();

        System.out.println(analyzer);

        for (Token t : analyzer.getTree()) {
            System.out.println(t.toString());
        }

        System.out.println(l2 - l1 + "ms");
    }

    @Test
    public void antrlParseTest() throws IOException {
        String code = code1;
        System.out.println(code);
        long l1 = System.currentTimeMillis();

        PhpLexer lexer = new PhpLexer(new ANTLRInputStream(code));
        PhpParser parser = new PhpParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        PhpParser.PhpBlockContext unit = parser.phpBlock();
        long l2 = System.currentTimeMillis();

        System.out.println(unit);
        System.out.println(l2 - l1 + "ms");
    }

    @Test
    public void graalphpParseTest() throws Exception {
        String code = code0;

        long l1 = System.currentTimeMillis();
        ASTParser parser = ASTParser.newParser(PHPVersion.PHP7_4);
        parser.setSource(code.toCharArray());
        parser.addErrorListener(new ConsoleErrorListener());
        parser.addErrorListener(new BailoutErrorListener());
        Program pgm = parser.parsePhpProgram();
        long l2 = System.currentTimeMillis();
        System.out.println(pgm);

        System.out.println(l2 - l1 + "ms");
    }

    private String readFile(String path) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        try {
            return new Scanner(resource.openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            System.out.println(path);
            throw new RuntimeException(e);
        }
    }
}
