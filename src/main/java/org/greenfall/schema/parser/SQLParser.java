package org.greenfall.schema.parser;


import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import org.apache.commons.io.IOUtils;
import org.greenfall.schema.model.Column;
import org.greenfall.schema.model.ForeignKey;
import org.greenfall.schema.model.PrimaryKey;
import org.greenfall.schema.model.Table;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SQLParser {

    public static List<Table> parse() {
        try{
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream is = loader.getResourceAsStream("sql_scripts/organization.sql");
            assert is != null;
            String result = IOUtils.toString(is, StandardCharsets.UTF_8);

            Statements statements = CCJSqlParserUtil.parseStatements(result);
            List<Table> tables = new ArrayList<>();
            statements.getStatements().forEach(statement -> {
                if(statement instanceof CreateTable createTable){
                    Table table = new Table();
                    tables.add(table);
                    table.setName(createTable.getTable().getName());
                    List<ColumnDefinition> jsqlColumnDefinitions = createTable.getColumnDefinitions();
                    List<Column> columns = new ArrayList<>();
                    jsqlColumnDefinitions.forEach(columnDefinition -> {
                        Column column = new Column();
                        columns.add(column);
                        column.setName(columnDefinition.getColumnName());
                        column.setDataType(columnDefinition.getColDataType().getDataType());
                        column.setNullable(true);
                        if (columnDefinition.getColumnSpecs() != null) {
                            String constraints = String.join(" ", columnDefinition.getColumnSpecs());
                            if(constraints.contains("UNIQUE")){
                                column.setUnique(true);
                            }
                            if(constraints.contains("NOT NULL")){
                                column.setNullable(false);
                            }
                        }
                    });
                    table.setColumns(columns);
                    List<ForeignKey> foreignKeys = new ArrayList<>();
                    table.setForeignKeys(foreignKeys);
                    createTable.getIndexes().forEach(index -> {
                        if(index.getType().equals("PRIMARY KEY")){
                            PrimaryKey primaryKey = new PrimaryKey();
                            primaryKey.setColumns(index.getColumnsNames());
                            table.setPrimaryKey(primaryKey);
                        }
                        else if (index instanceof ForeignKeyIndex){
                            ForeignKey foreignKey = new ForeignKey();
                            foreignKey.setColumns(index.getColumnsNames());
                            foreignKey.setRefColumns(((ForeignKeyIndex) index).getReferencedColumnNames());
                            foreignKey.setReferenceTable(((ForeignKeyIndex) index).getTable().getName());
                            foreignKeys.add(foreignKey);
                        }
                    });
                }
            });

            return tables;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }
}
