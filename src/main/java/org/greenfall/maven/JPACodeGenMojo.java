package org.greenfall.maven;

import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static org.greenfall.generator.JPACodeGenerator.generateCode;

@Mojo(name = "parseSchema")
public class JPACodeGenMojo extends AbstractMojo {

    /**
     * The path to Schema file.
     */
    @Parameter( property = "parseSchema.filepath", defaultValue = "/temp/schema.sql" )
    private String filepath;

    @SneakyThrows
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateCode(filepath);
    }
}
