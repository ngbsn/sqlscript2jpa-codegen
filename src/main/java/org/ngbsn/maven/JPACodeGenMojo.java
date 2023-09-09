package org.ngbsn.maven;

import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.ngbsn.generator.JPACodeGenerator.generateCode;

@Mojo(name = "parse-schema")
public class JPACodeGenMojo extends AbstractMojo {
    private static final Logger logger = LoggerFactory.getLogger(JPACodeGenMojo.class);

    /**
     * The path to Schema file.
     */
    @Parameter(property = "sqlFilePath")
    private String sqlFilePath;

    /**
     * The package name for generated code
     */
    @Parameter(property = "packageName")
    private String packageName;

    @SneakyThrows
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Executing the goal parse-schema {} {}", sqlFilePath, packageName);

        String sqlScript = new BufferedReader(
                new InputStreamReader(new FileInputStream(sqlFilePath), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        generateCode(sqlScript, packageName);
    }
}
