package org.ngbsn.generator;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.ngbsn.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.ngbsn.util.Util.packageNameToFolderStructure;

public class JPACodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JPACodeGenerator.class);

    public static void generateCode(final String sqlScript, final String packageName) throws IOException, TemplateException {
        logger.info("sql script {}", sqlScript);
        List<Table> tables = generateModels(sqlScript);
        processTemplate(tables, packageName);
    }

    private static List<Table> generateModels(final String sqlScript) {
        return ModelGenerator.parse(sqlScript);
    }

    private static void processTemplate(final List<Table> tables, final String packageName) throws IOException, TemplateException {
        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        String source;
        try (InputStream is = JPACodeGenerator.class.getClassLoader().getResourceAsStream("templates/entity.ftl")) {
            assert is != null;
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
                source = buffer.lines().collect(Collectors.joining("\n"));
            }
        }
        templateLoader.putTemplate("template", source);
        cfg.setTemplateLoader(templateLoader);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        /* Get the template (uses cache internally) */
        Template template = cfg.getTemplate("template");

        for (Table table : tables) {
            /* Create a data-model */
            Map<String, Object> root = new HashMap<>();
            root.put("table", table);
            root.put("package", packageName);
            List<String> imports = new ArrayList<>();
            imports.add("java.util.*");
            imports.add("jakarta.persistence.*");
            imports.add("javax.validation.constraints.NotNull");
            imports.add("java.io.Serializable");
            root.put("imports", imports);

            List<String> classAnnotations = new ArrayList<>();
            classAnnotations.add("@Entity");
            root.put("classAnnotations", classAnnotations);


            /* Merge data-model with template */
            String dir = "target/generated-sources/sqlscript2jpa/src/main/java/" + packageNameToFolderStructure(packageName);
            Files.createDirectories(Paths.get(dir));
            OutputStream outputStream = new FileOutputStream(dir + table.getClassName() + ".java");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            template.process(root, outputStreamWriter);
            outputStreamWriter.close();
        }
    }
}
