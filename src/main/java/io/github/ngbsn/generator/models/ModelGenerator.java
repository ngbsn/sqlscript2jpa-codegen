package io.github.ngbsn.generator.models;

import io.github.ngbsn.exception.SQLParsingException;
import io.github.ngbsn.generator.associations.AssociationMappingsGenerator;
import io.github.ngbsn.model.*;
import io.github.ngbsn.model.annotations.entity.EntityAnnotation;
import io.github.ngbsn.model.annotations.entity.TableAnnotation;
import io.github.ngbsn.model.annotations.field.ColumnAnnotation;
import io.github.ngbsn.model.annotations.field.EnumeratedAnnotation;
import io.github.ngbsn.model.annotations.field.NotNullAnnotation;
import io.github.ngbsn.util.SQLTypeToJpaTypeMapping;
import io.github.ngbsn.util.Util;
import jakarta.persistence.EnumType;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will parse the SQL script and generate the Table models for each table in the script
 * It will also extract all the columns from the script and add to the Table model
 */
public class ModelGenerator {

    public static final String REGEX_ALL_QUOTES = "[\"']";
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);
    private static final Map<String, Table> tablesMap = new HashMap<>();
    private ModelGenerator() {
    }

    /**
     * Clears the tables map. This is needed to have a clean start between JUnits
     */
    public static void clearTablesMap() {
        tablesMap.clear();
    }

    /**
     * Returns the tables map.
     */
    public static Map<String, Table> getTablesMap() {
        return tablesMap;
    }


    /**
     * Parse the DDL statements using JSQL parser
     *
     * @param sqlScript The input DDL commands used for generating the models
     * @return List of Tables models
     */
    public static List<Table> parse(final String sqlScript) throws SQLParsingException {
        String extractedStatementWithoutDefaultConstraint = null;
        try {
            String[] extractedStatementsArray = sqlScript.split(";");
            List<String> extractedStatementsList = Arrays.stream(extractedStatementsArray).filter(s -> {
                String regexCreateTable = "CREATE\\s+TABLE";
                Pattern patternCreateTable = Pattern.compile(regexCreateTable);
                Matcher matcherCreateTable = patternCreateTable.matcher(s);

                String regexAlterTable = "ALTER\\s+TABLE";
                Pattern patternAlterTable = Pattern.compile(regexAlterTable);
                Matcher matcherAlterTable = patternAlterTable.matcher(s);
                return matcherCreateTable.find() || matcherAlterTable.find();
            }).toList();
            List<Statement> statements = new ArrayList<>();
            for (String extractedStatement : extractedStatementsList) {
                extractedStatementWithoutDefaultConstraint = Util.removeDefaultConstraint(extractedStatement);
                statements.add(CCJSqlParserUtil.parse(extractedStatementWithoutDefaultConstraint));
            }
            processCreateTableStatements(statements);
            processAlterTableStatements(statements);
            AssociationMappingsGenerator.generateMappings();
            return tablesMap.values().stream().toList();
        } catch (JSQLParserException e) {
            logger.error("Error occurred {}", e.getMessage());
            logger.error("Statement having issue with parsing {}", extractedStatementWithoutDefaultConstraint);
            throw new SQLParsingException("Statement having issue with parsing");
        }
    }

    /**
     * Iterate over all the JSQL Create Table  statements and prepare the list of Table Model
     *
     * @param statements List of JSQL statements
     */
    private static void processCreateTableStatements(final List<Statement> statements) {
        statements.forEach(statement -> {
            //Iterating over all Tables
            if (statement instanceof CreateTable parsedTable) {
                Table table = new Table();
                table.setTableName(parsedTable.getTable().getName().replaceAll(REGEX_ALL_QUOTES, ""));
                tablesMap.put(table.getTableName(), table);
                String className = Util.convertSnakeCaseToCamelCase(table.getTableName(), true);
                className = SourceVersion.isKeyword(className) ? className + "Entity" : className;
                table.setClassName(className);

                List<String> tableAnnotations = new ArrayList<>();
                table.setAnnotations(tableAnnotations);
                //Adding @Entity
                tableAnnotations.add(new EntityAnnotation().toString());
                //Adding @Table
                tableAnnotations.add(TableAnnotation.builder().tableName(table.getTableName()).build().toString());

                List<Column> columns = new ArrayList<>();
                table.setColumns(columns);

                //extract columns
                extractColumns(table, parsedTable, columns);

                if (parsedTable.getIndexes() != null) {
                    //extract primary keys
                    Optional<Index> optionalIndex = parsedTable.getIndexes().stream().filter(index -> index.getType() != null &&
                            index.getType().equals("PRIMARY KEY")).findFirst();
                    extractPrimaryKeys(optionalIndex.orElse(null), table);

                    //extract foreign keys
                    List<Index> foreignKeyIndexes = parsedTable.getIndexes().stream().filter(ForeignKeyIndex.class::isInstance).toList();
                    extractForeignKeys(foreignKeyIndexes, table);
                }

            }
        });
    }

    /**
     * Iterate over all the JSQL Alter Table statements and prepare the list of Table Model
     *
     * @param statements List of JSQL statements
     */
    private static void processAlterTableStatements(final List<Statement> statements) {
        statements.forEach(statement -> {
            //Iterating over all Tables
            // Look for primary and foreign keys in ALTER TABLE constraints
            if (statement instanceof Alter alterTable) {
                Table table = tablesMap.get(alterTable.getTable().getName().replaceAll(REGEX_ALL_QUOTES, ""));
                List<Index> foreignKeyIndexes = new ArrayList<>();
                alterTable.getAlterExpressions().forEach(alterExpression -> {
                    if (alterExpression.getIndex() instanceof ForeignKeyIndex) {
                        //case: ALTER TABLE FOREIGN KEY
                        foreignKeyIndexes.add(alterExpression.getIndex());
                    } else if (alterExpression.getIndex() != null && alterExpression.getIndex().getType().equals("PRIMARY KEY")) {
                        //case: ALTER TABLE PRIMARY KEY
                        extractPrimaryKeys(alterExpression.getIndex(), table);
                    } else if (alterExpression.getOperation().name().equals("DROP")){
                        table.getColumns().removeIf(column -> column.getColumnName().equals(alterExpression.getColumnName()));

                    } else if (alterExpression.getOperation().name().equals("ADD")){
                        String cName = alterExpression.getColDataTypeList().get(0) != null ? alterExpression.getColDataTypeList().get(0).getColumnName() : null;
                        String columnName = alterExpression.getColumnName() != null ? alterExpression.getColumnName() : cName;
                        String type = alterExpression.getColDataTypeList().get(0) != null ?
                                SQLTypeToJpaTypeMapping.getTypeMapping(alterExpression.getColDataTypeList().get(0).getColDataType().getDataType()) : null;
                        Column column = new Column();
                        column.setColumnName(columnName);
                        column.setType(type);
                        String fieldName = Util.convertSnakeCaseToCamelCase(column.getColumnName(), false);
                        fieldName = SourceVersion.isKeyword(fieldName) ? fieldName + table.getClassName() : fieldName;
                        column.setFieldName(fieldName);

                        //Check for NOT NULL
                        if (alterExpression.getColDataTypeList().get(0) != null && alterExpression.getColDataTypeList().get(0).getColumnSpecs() != null) {
                            List<String> fieldAnnotations = new ArrayList<>();

                            String constraints = String.join(" ", alterExpression.getColDataTypeList().get(0).getColumnSpecs());
                            if (constraints.contains("NOT NULL")) {
                                fieldAnnotations.add(NotNullAnnotation.builder().build().toString());
                            }
                            column.setAnnotations(fieldAnnotations);
                        }
                        table.getColumns().add(column);
                    }
                });
                extractForeignKeys(foreignKeyIndexes, table);
            }
        });
    }


    /**
     * Looking for all JSQL foreign keys in this table and adding it to our model
     *
     * @param foreignKeyIndexes List of JSQL foreign key indexes
     * @param table             Table model
     */
    private static void extractForeignKeys(final List<Index> foreignKeyIndexes, final Table table) {
        if (!foreignKeyIndexes.isEmpty()) {
            foreignKeyIndexes.forEach(index -> {
                if (index instanceof ForeignKeyIndex foreignKeyIndex) {
                    ForeignKeyConstraint foreignKeyConstraint = new ForeignKeyConstraint();
                    foreignKeyConstraint.setColumns(foreignKeyIndex.getColumnsNames()
                            .stream().map(s -> s.replaceAll(REGEX_ALL_QUOTES, "")).toList());
                    foreignKeyConstraint.setReferencedColumns(foreignKeyIndex.getReferencedColumnNames()
                            .stream().map(s -> s.replaceAll(REGEX_ALL_QUOTES, "")).toList());
                    foreignKeyConstraint.setReferencedTableName(foreignKeyIndex.getTable().getName()
                            .replaceAll(REGEX_ALL_QUOTES, ""));
                    table.getForeignKeyConstraints().add(foreignKeyConstraint);

                }
            });
        }
    }

    /**
     * Extracting primary key information from the JSQL parsed data and setting them into the Table model
     *
     * @param primaryKeyIndex JSQL primaryKeyIndex
     * @param table           Table model
     */
    private static void extractPrimaryKeys(final Index primaryKeyIndex, final Table table) {
        List<Index.ColumnParams> columnParamsList = primaryKeyIndex != null ? primaryKeyIndex.getColumns() : null;
        if (columnParamsList != null) {
            List<Column> primaryKeyColumns = table.getColumns().stream().
                    filter(column -> columnParamsList.stream().anyMatch(columnParams -> columnParams.getColumnName().replaceAll(REGEX_ALL_QUOTES, "").equals(column.getColumnName()))).toList();

            if (columnParamsList.size() > 1) {
                table.setNumOfPrimaryKeyColumns(columnParamsList.size());
                //create a embeddedId within Table
                EmbeddableClass embeddedId = new EmbeddableClass();
                embeddedId.setClassName(table.getClassName() + "PK");
                embeddedId.setFieldName(Util.convertSnakeCaseToCamelCase(table.getTableName(), false) + "PK");
                table.setEmbeddedId(embeddedId);

                //remove the primary keys columns from table and add inside EmbeddedId
                primaryKeyColumns.forEach(column -> {
                    table.getColumns().remove(column);
                    embeddedId.getColumns().add(column);
                });
            }
            primaryKeyColumns.forEach(column -> column.setPrimaryKey(true));
        }
    }

    /**
     * Generate column models for the JSQL parsed table
     *
     * @param parsedTable JSQL CreateTable
     * @param columns     Set of generated column models
     */
    private static void extractColumns(final Table table, final CreateTable parsedTable, final List<Column> columns) {
        parsedTable.getColumnDefinitions().forEach(columnDefinition -> {
            Column column = new Column();
            columns.add(column);
            List<String> fieldAnnotations = new ArrayList<>();
            column.setAnnotations(fieldAnnotations);
            column.setColumnName(columnDefinition.getColumnName().replaceAll(REGEX_ALL_QUOTES, ""));
            //Adding @Column
            fieldAnnotations.add(ColumnAnnotation.builder().columnName(column.getColumnName()).build().toString());
            String fieldName = Util.convertSnakeCaseToCamelCase(column.getColumnName(), false);
            fieldName = SourceVersion.isKeyword(fieldName) ? fieldName + table.getClassName() : fieldName;
            column.setFieldName(fieldName);
            if (columnDefinition.getColDataType().getDataType().equals("ENUM")) {
                TableEnum tableEnum = new TableEnum();
                table.getTableEnums().add(tableEnum);
                tableEnum.setEnumName(WordUtils.capitalize(column.getColumnName()) + "Enum");
                List<String> values = tableEnum.getValues();
                for (String s : columnDefinition.getColDataType().getArgumentsStringList()) {
                    values.add(s.replaceAll(REGEX_ALL_QUOTES, ""));
                }
                column.setType(tableEnum.getEnumName());
                fieldAnnotations.add(EnumeratedAnnotation.builder().value(EnumType.STRING).build().toString());
            } else {
                String mappedJavaType = SQLTypeToJpaTypeMapping.getTypeMapping(columnDefinition.getColDataType().getDataType());
                column.setType(Objects.requireNonNullElse(mappedJavaType, "Object"));
            }

            //Check for NOT NULL
            if (columnDefinition.getColumnSpecs() != null) {
                String constraints = String.join(" ", columnDefinition.getColumnSpecs());
                if (constraints.contains("NOT NULL")) {
                    fieldAnnotations.add(NotNullAnnotation.builder().build().toString());
                }
            }
        });
    }
}
