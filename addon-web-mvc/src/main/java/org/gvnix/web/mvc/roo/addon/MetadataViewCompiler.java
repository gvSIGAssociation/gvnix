package org.gvnix.web.mvc.roo.addon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class MetadataViewCompiler {

    private final JavaCompiler compiler;
    private final DiagnosticCollector<JavaFileObject> diagnosticsCollector;
    private final StandardJavaFileManager fileManager;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    // private JavaFileObject javaObjectFromString;
    // private Iterable<? extends JavaFileObject> fileObjects;
    // private CompilationTask task;

    public MetadataViewCompiler() {
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
        fileManager = compiler.getStandardFileManager(diagnosticsCollector,
                null, null);
        diagnostics = diagnosticsCollector.getDiagnostics();
        // javaObjectFromString = getJavaFileContentsAsString();
    }

    public boolean compileFromSrc(String javaSrcFile) {
        try {
            ArrayList<File> jars = new ArrayList<File>();
            jars.add(new File(
                    "/home/orovira/.m2/repository/com/vaadin/vaadin/6.7.3/vaadin-6.7.3.jar"));
            jars.add(new File(
                    "/home/orovira/.m2/repository/com/google/gwt/gwt-user/2.4.0/gwt-user-2.4.0.jar"));

            jars.add(new File(
                    "/home/orovira/.m2/repository/org/gvnix/org.gvnix.weblayer.roo.addon/0.8.2/org.gvnix.weblayer.roo.addon-0.8.2.jar"));
            fileManager.setLocation(StandardLocation.CLASS_PATH, jars);
            ArrayList<File> outfolder = new ArrayList<File>();
            File targetDir = new File("target/gvnix-metadata");
            boolean targetDirExists = targetDir.exists();
            if (!targetDirExists) {
                targetDirExists = targetDir.mkdir();
            }
            if (targetDirExists) {
                outfolder.add(new File("target/gvnix-metadata"));
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                        outfolder);
            } else {
                System.err
                        .println("No se puede crear el directorio para dejar las clases compiladas");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Iterable<? extends JavaFileObject> filesToCompile = fileManager
                .getJavaFileObjectsFromStrings(Arrays.asList(javaSrcFile));
        CompilationTask task = compiler.getTask(null, fileManager,
                diagnosticsCollector, null, null, filesToCompile);
        return task.call();
    }

    public final List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return this.diagnostics;
    }

}
