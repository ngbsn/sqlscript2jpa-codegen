package org.greenfall.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "parse-schema")
public class JPACodeGenMojo extends AbstractMojo {

    /**
     * The path to Schema file.
     */
    @Parameter( property = "parse-schema.filepath", defaultValue = "/temp/schema.sql" )
    private String filepath;

    /**
     * Database vendor.
     */
    @Parameter( property = "parse-schema.database", defaultValue = "postgresql" )
    private String database;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Hello, world." );
    }
}
