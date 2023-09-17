package org.ngbsn.maven;

import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.ngbsn.generator.JPACodeGenerator.generateCode;

public class CodeGenTest {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenTest.class);

    @Test
    public void testMySql() throws TemplateException, IOException {
        logger.info("Starting test");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql/mysql.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String packageName = "com.ngbsn.models.mysql";
        generateCode(sqlScript, packageName);
    }

    @Test
    public void testPostgres() throws TemplateException, IOException {
        logger.info("Starting test");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql/postgres.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String packageName = "com.ngbsn.models.postgres";
        generateCode(sqlScript, packageName);
    }
}
