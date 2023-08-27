package org.ngbsn.generator;


import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.apache.commons.text.CaseUtils;
import org.ngbsn.model.Column;
import org.ngbsn.model.Table;
import org.ngbsn.util.SQLToJavaMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);

    public static List<Table> parse(final String sqlScript) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sqlScript);
            List<Table> tables = new ArrayList<>();
            statements.getStatements().forEach(statement -> {
                if (statement instanceof CreateTable parsedTable) {
                    Table table = new Table();
                    tables.add(table);
                    table.setName(parsedTable.getTable().getName());
                    table.setClassName(CaseUtils.toCamelCase(table.getName(), true, '_'));
                    List<Column> columns = new ArrayList<>();
                    parsedTable.getColumnDefinitions().forEach(columnDefinition -> {
                        Column column = new Column();
                        columns.add(column);
                        column.setName(columnDefinition.getColumnName());
                        column.setFieldName(CaseUtils.toCamelCase(column.getName(), false, '_'));
                        column.setType(SQLToJavaMapping.sqlToJavaMap.get(columnDefinition.getColDataType().getDataType()));
                        column.setNullable(true);
                        if (columnDefinition.getColumnSpecs() != null) {
                            String constraints = String.join(" ", columnDefinition.getColumnSpecs());
                            if (constraints.contains("UNIQUE")) {
                                column.setUnique(true);
                            }
                            if (constraints.contains("NOT NULL")) {
                                column.setNullable(false);
                            }
                        }
                    });
                    table.setColumns(columns);
                    Optional<Index> optionalIndex = parsedTable.getIndexes().stream().filter(index -> index.getType().equals("PRIMARY KEY")).findFirst();
                    List<Index.ColumnParams> columnParamsList;
                    columnParamsList = optionalIndex.map(Index::getColumns).orElse(null);
                    if(columnParamsList != null){
                        if(columnParamsList.size() > 1){
                            table.setNumOfPrimaryKeyColumns(columnParamsList.size());
                        }
                        List<Column> primaryKeyColumns = table.getColumns().stream().filter(column -> columnParamsList.stream().anyMatch(columnParams -> columnParams.getColumnName().equals(column.getName()))).toList();
                        primaryKeyColumns.forEach(column -> column.setPrimaryKey(true));
                    }
                }
            });

            return tables;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
