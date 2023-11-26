package io.github.ngbsn.generator.code;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.github.ngbsn.exception.SQLParsingException;
import io.github.ngbsn.generator.models.ModelGenerator;
import io.github.ngbsn.model.Table;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.ngbsn.util.Util.packageNameToFolderStructure;

@Slf4j
public class JPACodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JPACodeGenerator.class);

    public static void main(final String[] args) throws SQLParsingException, TemplateException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8))) {
            String sqlScript = bufferedReader
                    .lines()
                    .collect(Collectors.joining("\n"));
            generateCode(sqlScript, args[1]);
        } catch (Exception e) {
            log.error("Error occurred while running the tool", e);
            throw e;
        }
    }

    /**
     * @param sqlScript   The input DDL commands used for generating the models
     * @param packageName package name for the entities sources to be generated
     * @throws IOException       Thrown if template couldn't be read
     * @throws TemplateException Thrown if template couldn't be processed
     */
    public static void generateCode(final String sqlScript, final String packageName) throws IOException, TemplateException, SQLParsingException {
        logger.debug("sql script {}", sqlScript);
        List<Table> tables = generateModels(sqlScript);
        processTemplate(tables, packageName);
    }

    /**
     * Generate the models needed to generate the sources
     *
     * @param sqlScript The input DDL commands used for generating the models
     * @return List of Table models
     */
    private static List<Table> generateModels(final String sqlScript) throws SQLParsingException {
        return ModelGenerator.parse(sqlScript);
    }

    /**
     * This method processes the Apache FreeMarker entity template using the generated models
     *
     * @param tables      Generated models
     * @param packageName package name for the entities sources to be generated
     * @throws IOException       Thrown if template couldn't be read
     * @throws TemplateException Thrown if template cannot be processed
     */
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
            imports.add("lombok.Getter");
            imports.add("lombok.Setter");
            imports.add("lombok.Builder");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
            root.put("imports", imports);

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
