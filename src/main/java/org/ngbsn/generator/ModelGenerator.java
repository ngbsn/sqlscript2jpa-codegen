package org.ngbsn.generator;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.apache.commons.text.CaseUtils;
import org.ngbsn.model.Column;
import org.ngbsn.model.EmbeddableClass;
import org.ngbsn.model.ForeignKeyConstraint;
import org.ngbsn.model.Table;
import org.ngbsn.model.annotations.entityAnnotations.EntityAnnotation;
import org.ngbsn.model.annotations.entityAnnotations.TableAnnotation;
import org.ngbsn.model.annotations.fieldAnnotations.ColumnAnnotation;
import org.ngbsn.model.annotations.fieldAnnotations.NotNullAnnotation;
import org.ngbsn.util.SQLToJavaMapping;
import org.ngbsn.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.ngbsn.generator.AssociationMappingsGenerator.generateMappings;

/**
 * Ignoring these annotations as they are useful only in DDL generation:
 * UNIQUE
 */
public class ModelGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);
    protected static Map<String, Table> tablesMap = new HashMap<>();

    public static List<Table> parse(final String sqlScript) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sqlScript);
            statements.getStatements().forEach(statement -> {
                //Iterating over all Tables
                if (statement instanceof CreateTable parsedTable) {
                    Table table = new Table();
                    table.setTableName(parsedTable.getTable().getName().replaceAll("[\"']", ""));
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
                    extractColumns(parsedTable, columns);

                    extractPrimaryKeys(parsedTable, table);

                    extractForeignKeys(parsedTable, table);

                    generateMappings(table);

                }
            });
            return tablesMap.values().stream().toList();
        } catch (Exception e) {
            logger.error("Error occurred", e);
        }
        return null;
    }

    /**
     * Looking for all foreign keys in this table and adding it to our model
     *
     * @param parsedTable The SQL script parsed table
     * @param table       Table model
     */
    private static void extractForeignKeys(CreateTable parsedTable, Table table) {
        List<Index> foreignKeyIndexes = parsedTable.getIndexes().stream().filter(index -> index instanceof ForeignKeyIndex).toList();
        if (!foreignKeyIndexes.isEmpty()) {
            foreignKeyIndexes.forEach(index -> {
                if (index instanceof ForeignKeyIndex foreignKeyIndex) {
                    ForeignKeyConstraint foreignKeyConstraint = new ForeignKeyConstraint();
                    foreignKeyConstraint.setColumns(foreignKeyIndex.getColumnsNames());
                    foreignKeyConstraint.setReferencedColumns(foreignKeyIndex.getReferencedColumnNames());
                    foreignKeyConstraint.setReferencedTableName(foreignKeyIndex.getTable().getName());
                    table.getForeignKeyConstraints().add(foreignKeyConstraint);
                }
            });
        }
    }

    /**
     * Looking for all primary keys in this table
     *
     * @param parsedTable The SQL script parsed table
     * @param table       Table model
     */
    private static void extractPrimaryKeys(CreateTable parsedTable, Table table) {
        Optional<Index> optionalIndex = parsedTable.getIndexes().stream().filter(index -> index.getType().equals("PRIMARY KEY")).findFirst();
        List<Index.ColumnParams> columnParamsList = optionalIndex.map(Index::getColumns).orElse(null);
        if (columnParamsList != null) {
            Set<Column> primaryKeyColumns = table.getColumns().stream().
                    filter(column -> columnParamsList.stream().anyMatch(columnParams -> columnParams.getColumnName().equals(column.getColumnName()))).collect(Collectors.toSet());

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
     * Generate column models for all the parsed table
     *
     * @param parsedTable Parsed table
     * @param columns     List of generated column models
     */
    private static void extractColumns(CreateTable parsedTable, Set<Column> columns) {
        parsedTable.getColumnDefinitions().forEach(columnDefinition -> {
            Column column = new Column();
            columns.add(column);
            Set<String> columnAnnotations = new HashSet<>();
            column.setAnnotations(columnAnnotations);
            column.setColumnName(columnDefinition.getColumnName().replaceAll("[\"']", ""));
            //Adding @Column
            columnAnnotations.add(ColumnAnnotation.builder().columnName(column.getColumnName()).build().toString());
            column.setFieldName(Util.convertSnakeCaseToCamelCase(column.getColumnName(), false));
            column.setType(SQLToJavaMapping.sqlToJavaMap.get(columnDefinition.getColDataType().getDataType()));

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
