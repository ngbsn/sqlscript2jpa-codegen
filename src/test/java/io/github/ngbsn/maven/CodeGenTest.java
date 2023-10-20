package io.github.ngbsn.maven;

import freemarker.template.TemplateException;
import io.github.ngbsn.generator.ModelGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.github.ngbsn.generator.JPACodeGenerator.generateCode;

public class CodeGenTest {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenTest.class);


    @Before
    public void beforeTest() {
        ModelGenerator.clearTablesMap();
    }

    @Test
    public void testMySql() throws TemplateException, IOException {
        logger.info("Starting testMySql");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql/mysql.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String packageName = "io.github.ngbsn.models.mysql";
        generateCode(sqlScript, packageName);
    }

    @Test
    public void testPostgres() throws TemplateException, IOException {
        logger.info("Starting testPostgres");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql/postgres.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String packageName = "io.github.ngbsn.models.postgres";
        generateCode(sqlScript, packageName);
    }
}
