package org.graalphp.benchmark;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.develnext.jphp.core.syntax.SyntaxAnalyzer;
import org.develnext.jphp.core.tokenizer.Tokenizer;
import org.develnext.jphp.core.tokenizer.token.Token;
import org.eclipse.php.core.PHPVersion;
import org.eclipse.php.core.ast.error.BailoutErrorListener;
import org.eclipse.php.core.ast.error.ConsoleErrorListener;
import org.eclipse.php.core.ast.nodes.ASTParser;
import org.eclipse.php.core.ast.nodes.Program;
import org.graalphp.benchmark.antrl.PhpLexer;
import org.graalphp.benchmark.antrl.PhpParser;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import php.runtime.common.LangMode;

import php.runtime.env.Context;
import php.runtime.env.Environment;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Parser Benchmark
 *
 * @author abertschi
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkParser {

    @Param({
            "phpoffice/Xls.php",
            "phpoffice/Comment.php",
            "symfony/Frameworkextension.php",
            "symfony/RequestTest.php",
            "benchmarkgame/fasta.php-2.php",
            "benchmarkgame/fannkuchredux.php-1.php",
    })
    public String filename;
    private String codeWithTags;
    private String codeWithoutTags;

    @Setup()
    public void setup() {
        codeWithTags = readFile(filename);
        codeWithoutTags = codeWithTags.replace("<?php", "");
        System.out.println(filename);
    }

    @Benchmark
    public void benchmarkJPHP(Blackhole sink) throws IOException {
        Environment environment = new Environment();
        Tokenizer tokenizer = new Tokenizer(new Context(codeWithoutTags));
        environment.scope.setLangMode(LangMode.DEFAULT);
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer(environment, tokenizer);
        sink.consume(analyzer);
    }

//    @Benchmark
    public void benchmarkANTRL(Blackhole sink) throws IOException {
        PhpLexer lexer = new PhpLexer(new ANTLRInputStream(codeWithTags));
        PhpParser parser = new PhpParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        PhpParser.PhpBlockContext unit = parser.phpBlock();
        sink.consume(unit);
    }

    @Benchmark
    public void benchmarkGraalPHP(Blackhole sink) throws Exception {
        ASTParser parser = ASTParser.newParser(PHPVersion.PHP7_4);
        parser.setSource(codeWithTags.toCharArray());
//        parser.addErrorListener(new ConsoleErrorListener());
        parser.addErrorListener(new BailoutErrorListener());
        Program pgm = parser.parsePhpProgram();
        sink.consume(pgm);
    }

    private String readFile(String path) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        try {
            return new Scanner(resource.openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            return null;
        }
    }
}
