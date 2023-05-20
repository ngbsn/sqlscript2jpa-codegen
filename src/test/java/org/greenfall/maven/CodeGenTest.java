package org.greenfall.maven;

import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.greenfall.generator.JPACodeGenerator.generateCode;

public class CodeGenTest {

    @Test
    public void testCodeGen() throws TemplateException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("sql_scripts/organization.sql")).getFile());
        String sqlScript = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        generateCode(sqlScript);

    }
}
