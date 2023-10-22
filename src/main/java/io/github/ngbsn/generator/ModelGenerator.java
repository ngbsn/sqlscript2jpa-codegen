package io.github.ngbsn.generator;

import io.github.ngbsn.model.Column;
import io.github.ngbsn.model.EmbeddableClass;
import io.github.ngbsn.model.ForeignKeyConstraint;
import io.github.ngbsn.model.Table;
import io.github.ngbsn.model.annotations.entity.EntityAnnotation;
import io.github.ngbsn.model.annotations.entity.TableAnnotation;
import io.github.ngbsn.model.annotations.field.ColumnAnnotation;
import io.github.ngbsn.model.annotations.field.NotNullAnnotation;
import io.github.ngbsn.util.SQLToJavaMapping;
import io.github.ngbsn.util.Util;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class will parse the SQL script and generate the Table models for each table in the script
 * It will also extract all the columns from the script and add to the Table model
 */
public class ModelGenerator {

    public static final String REGEX_ALL_QUOTES = "[\"']";

    private ModelGenerator() {
    }

    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);
    private static final Map<String, Table> tablesMap = new HashMap<>();

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
     * @param sqlScript The input DDL commands used for generating the models
     * @return List of Tables models
     */
    static List<Table> parse(final String sqlScript) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sqlScript);
            processCreateTableStatements(statements);
            processAlterTableStatements(statements);
            AssociationMappingsGenerator.generateMappings();
            return tablesMap.values().stream().toList();
        } catch (Exception e) {
            logger.error("Error occurred", e);
        }
        return new ArrayList<>();
    }

    /**
     * Iterate over all the JSQL Create Table  statements and prepare the list of Table Model
     * @param statements Set of JSQL statements
     */
    private static void processCreateTableStatements(final Statements statements) {
        statements.getStatements().forEach(statement -> {
            //Iterating over all Tables
            if (statement instanceof CreateTable parsedTable) {
                Table table = new Table();
                table.setTableName(parsedTable.getTable().getName().replaceAll(REGEX_ALL_QUOTES, ""));
                tablesMap.put(table.getTableName(), table);
                table.setClassName(Util.convertSnakeCaseToCamelCase(table.getTableName(), true));

                Set<String> tableAnnotations = new HashSet<>();
                table.setAnnotations(tableAnnotations);
                //Adding @Entity
                tableAnnotations.add(new EntityAnnotation().toString());
                //Adding @Table
                tableAnnotations.add(TableAnnotation.builder().tableName(table.getTableName()).build().toString());

                Set<Column> columns = new HashSet<>();
                table.setColumns(columns);

                //extract columns
                extractColumns(parsedTable, columns);

                //extract primary keys
                Optional<Index> optionalIndex = parsedTable.getIndexes().stream().filter(index -> index.getType().equals("PRIMARY KEY")).findFirst();
                extractPrimaryKeys(optionalIndex.orElse(null), table);

                //extract foreign keys
                List<Index> foreignKeyIndexes = parsedTable.getIndexes().stream().filter(ForeignKeyIndex.class::isInstance).toList();
                extractForeignKeys(foreignKeyIndexes, table);
            }
        });
    }

    /**
     * Iterate over all the JSQL Alter Table statements and prepare the list of Table Model
     * @param statements Set of JSQL statements
     */
    private static void processAlterTableStatements(final Statements statements) {
        statements.getStatements().forEach(statement -> {
            //Iterating over all Tables
            // Look for primary and foreign keys in ALTER TABLE constraints
            if (statement instanceof Alter alterTable) {
                Table table = tablesMap.get(alterTable.getTable().getName().replaceAll(REGEX_ALL_QUOTES, ""));
                List<Index> foreignKeyIndexes = new ArrayList<>();
                alterTable.getAlterExpressions().forEach(alterExpression -> {
                    if (alterExpression.getIndex() instanceof ForeignKeyIndex) {
                        //case: ALTER TABLE FOREIGN KEY
                        foreignKeyIndexes.add(alterExpression.getIndex());
                    } else if (alterExpression.getIndex().getType().equals("PRIMARY KEY")) {
                        //case: ALTER TABLE PRIMARY KEY
                        extractPrimaryKeys(alterExpression.getIndex(), table);
                    }
                });
                extractForeignKeys(foreignKeyIndexes, table);
            }
        });
    }


    /**
     * Looking for all JSQL foreign keys in this table and adding it to our model
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
     * @param primaryKeyIndex JSQL primaryKeyIndex
     * @param table           Table model
     */
    private static void extractPrimaryKeys(final Index primaryKeyIndex, final Table table) {
        List<Index.ColumnParams> columnParamsList = primaryKeyIndex != null ? primaryKeyIndex.getColumns() : null;
        if (columnParamsList != null) {
            Set<Column> primaryKeyColumns = table.getColumns().stream().
                    filter(column -> columnParamsList.stream().anyMatch(columnParams -> columnParams.getColumnName().replaceAll(REGEX_ALL_QUOTES, "").equals(column.getColumnName()))).collect(Collectors.toSet());

            if (columnParamsList.size() > 1) {
                table.setNumOfPrimaryKeyColumns(columnParamsList.size());
                //create a embeddedId within Table
                EmbeddableClass embeddedId = new EmbeddableClass();
                embeddedId.setClassName(table.getClassName() + "PK");
                embeddedId.setFieldName(Util.convertSnakeCaseToCamelCase(table.getTableName(), false) + "PK");
                embeddedId.setEmbeddedId(true);
                table.getEmbeddableClasses().add(embeddedId);

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
     * @param parsedTable JSQL CreateTable
     * @param columns     Set of generated column models
     */
    private static void extractColumns(final CreateTable parsedTable, final Set<Column> columns) {
        parsedTable.getColumnDefinitions().forEach(columnDefinition -> {
            Column column = new Column();
            columns.add(column);
            Set<String> columnAnnotations = new HashSet<>();
            column.setAnnotations(columnAnnotations);
            column.setColumnName(columnDefinition.getColumnName().replaceAll(REGEX_ALL_QUOTES, ""));
            //Adding @Column
            columnAnnotations.add(ColumnAnnotation.builder().columnName(column.getColumnName()).build().toString());
            column.setFieldName(Util.convertSnakeCaseToCamelCase(column.getColumnName(), false));
            column.setType(SQLToJavaMapping.getSqlToJavaMap().get(columnDefinition.getColDataType().getDataType()));

            //Check for NOT NULL
            if (columnDefinition.getColumnSpecs() != null) {
                String constraints = String.join(" ", columnDefinition.getColumnSpecs());
                if (constraints.contains("NOT NULL")) {
                    columnAnnotations.add(NotNullAnnotation.builder().build().toString());
                }
            }
        });
    }
}
