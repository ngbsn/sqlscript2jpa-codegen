package io.github.ngbsn.maven;

import io.github.ngbsn.generator.JPACodeGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Mojo(name = "parse-schema")
@Slf4j
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

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(sqlFilePath), StandardCharsets.UTF_8))) {
            String sqlScript = bufferedReader
                    .lines()
                    .collect(Collectors.joining("\n"));
            JPACodeGenerator.generateCode(sqlScript, packageName);
        } catch (Exception e) {
            log.error("Error occurred while running the plugin", e);
        }


    }
}
