package org.ngbsn.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.ngbsn.schema.model.Table;
import org.ngbsn.schema.parser.SQLParser;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.ngbsn.util.CommonUtils.tableNameToEntityClassName;

public class JPACodeGenerator {

    public static void generateCode(final String sqlScript) throws IOException, TemplateException {
        List<Table> tables = generateModels(sqlScript);
        processTemplate(tables);
    }

    private static List<Table> generateModels(final String sqlScript) {
        return SQLParser.parse(sqlScript);
    }

    private static void processTemplate(final List<Table> tables) throws IOException, TemplateException {
        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        Path resourceDirectory = Paths.get("src","test","resources","templates");
        File file = resourceDirectory.toFile();
        cfg.setDirectoryForTemplateLoading(file);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("entity.ftl");

        for (Table table : tables) {
            /* Create a data-model */
            Map<String, Object> root = new HashMap<>();
            root.put("table", table);
            root.put("package", "com.ngbsn");
            List<String> imports = new ArrayList<>();
            imports.add("java.util.*");
            root.put("imports", imports);

            List<String> classAnnotations = new ArrayList<>();
            classAnnotations.add("@Entity");
            root.put("classAnnotations", classAnnotations);

            String className = tableNameToEntityClassName(table.getName());
            root.put("className", className);

            /* Merge data-model with template */
            OutputStream outputStream = new FileOutputStream("target/generated-sources/" + className + ".java");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            temp.process(root, outputStreamWriter);
            outputStreamWriter.close();
        }
    }
}
