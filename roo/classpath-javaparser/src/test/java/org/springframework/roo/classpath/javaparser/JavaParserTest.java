package org.springframework.roo.classpath.javaparser;

import static org.junit.Assert.assertTrue;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test of JavaParser
 * 
 * @author DiSiD Technologies
 * @since 1.2.1
 */
@RunWith(PowerMockRunner.class)
public class JavaParserTest {

    private static final String SIMPLE_INTERFACE_FILE_PATH = "SimpleInterface.java.test";
    private static final String SIMPLE_CLASS_FILE_PATH = "SimpleClass.java.test";
    private static final String FOO_FILE_PATH = "Foo.java.test";


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSimpleInterface() throws Exception {

        // Set up
        final File file = getResource(SIMPLE_INTERFACE_FILE_PATH);
        final String fileContents = getResourceContents(file);

        // invoke
        final CompilationUnit compilationUnit = JavaParser
                .parse(new ByteArrayInputStream(fileContents.getBytes()));

        // result
        final String result = compilationUnit.toString();

        saveResult(file, result);

        NewUpdateCompilationUnitTest.checkSimpleInterface(result, true);

    }

    @Test
    public void testSimpleClass() throws Exception {

        // Set up
        final File file = getResource(SIMPLE_CLASS_FILE_PATH);
        final String fileContents = getResourceContents(file);

        // invoke
        final CompilationUnit compilationUnit = JavaParser
                .parse(new ByteArrayInputStream(fileContents.getBytes()));

        // result
        final String result = compilationUnit.toString();

        saveResult(file, result);

        NewUpdateCompilationUnitTest.checkSimpleClass(result);

    }

    // @Test
    public void testFoo() throws Exception {

        // Set up
        final File file = getResource(FOO_FILE_PATH);
        final String fileContents = getResourceContents(file);

        // invoke
        final CompilationUnit compilationUnit = JavaParser
                .parse(new ByteArrayInputStream(fileContents.getBytes()));

        // result
        final String result = compilationUnit.toString();

        saveResult(file, result);

        assertTrue(result.contains("// Comment inline2"));

    }

    private void saveResult(File orgininalFile, String result)
            throws IOException {
        final File resultFile = new File(orgininalFile.getParentFile(),
                FilenameUtils.getName(orgininalFile.getName())
                        + ".parser.result");
        FileUtils.write(resultFile, result);
    }

    private File getResource(String pathname) {
        URL res = this.getClass().getClassLoader().getResource(pathname);
        return new File(res.getPath());
    }

    private String getResourceContents(String pathName) throws IOException {
        return getResourceContents(getResource(pathName));
    }

    private String getResourceContents(File file) throws IOException {
        return FileUtils.readFileToString(file);
    }
}
