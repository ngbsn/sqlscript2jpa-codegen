package io.github.ngbsn.maven;

import io.github.ngbsn.generator.ModelGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.github.ngbsn.generator.JPACodeGenerator.generateCode;


//TODO write assertions
@Slf4j
public class CodeGenTest {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenTest.class);


    @Before
    public void beforeTest() {
        ModelGenerator.clearTablesMap();
    }

    @Test
    public void testMySql() {
        try {
            logger.info("Starting testMySql");
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("sql/mysql.sql")).getFile());
            String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String packageName = "io.github.ngbsn.models.mysql";
            generateCode(sqlScript, packageName);
        } catch (Exception e) {
            log.error("testMySql failed", e);
            Assert.fail();
        }
    }

    @Test
    public void testPostgres() {
        try {
            logger.info("Starting testPostgres");
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("sql/postgres.sql")).getFile());
            String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String packageName = "io.github.ngbsn.models.postgres";
            generateCode(sqlScript, packageName);
        } catch (Exception e) {
            log.error("testPostgres failed", e);
            Assert.fail();
        }
    }

    @Test
    public void testPostgres2() {
        try {
            logger.info("Starting testPostgres");
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("sql/postgres2.sql")).getFile());
            String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String packageName = "io.github.ngbsn.models.postgres2";
            generateCode(sqlScript, packageName);
        } catch (Exception e) {
            log.error("testPostgres failed", e);
            Assert.fail();
        }
    }
}
