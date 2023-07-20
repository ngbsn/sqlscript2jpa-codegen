package org.ngbsn.maven;

import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.ngbsn.generator.JPACodeGenerator.generateCode;

public class CodeGenTest {

    @Test
    public void testCodeGen() throws TemplateException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql_scripts/organization.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        generateCode(sqlScript);

    }
}
