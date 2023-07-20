package org.ngbsn.schema.parser;


import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.ngbsn.schema.model.Column;
import org.ngbsn.schema.model.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SQLParser {

    public static List<Table> parse(final String sqlScript) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sqlScript);
            List<Table> tables = new ArrayList<>();
            statements.getStatements().forEach(statement -> {
                if (statement instanceof CreateTable parsedTable) {
                    Table table = new Table();
                    tables.add(table);
                    table.setName(parsedTable.getTable().getName());
                    List<Column> columns = new ArrayList<>();
                    parsedTable.getColumnDefinitions().forEach(columnDefinition -> {
                        Column column = new Column();
                        columns.add(column);
                        column.setName(columnDefinition.getColumnName());
                        column.setType(columnDefinition.getColDataType().getDataType());
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
                    Set<String> parsedColumnsWithPrimaryKeyIndex = parsedTable.getIndexes().stream().filter(index -> index.getType().equals("PRIMARY KEY")).map(Index::getName).collect(Collectors.toSet());
                    Set<String> parsedColumnsWithForeignKeyIndex = parsedTable.getIndexes().stream().filter(index -> index instanceof ForeignKeyIndex).map(Index::getName).collect(Collectors.toSet());

                    table.getColumns().forEach(column -> {
                        if (parsedColumnsWithPrimaryKeyIndex.contains(column.getName())) {
                            column.setPrimaryKey(true);
                        }
                    });
                    table.getColumns().forEach(column -> {
                        if (parsedColumnsWithForeignKeyIndex.contains(column.getName())) {
                            column.setForeignKey(true);
                        }
                    });

                }
            });

            return tables;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
