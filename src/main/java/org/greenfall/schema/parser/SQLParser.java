package org.greenfall.schema.parser;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.commons.io.IOUtils;
import org.greenfall.schema.model.Column;
import org.greenfall.schema.model.Table;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SQLParser {

    public static List<Table> parse() throws IOException, JSQLParserException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("sql_scripts/organization.sql");
        assert is != null;
        String result = IOUtils.toString(is, StandardCharsets.UTF_8);

        Statements statements = CCJSqlParserUtil.parseStatements(result);
        List<Table> tables = new ArrayList<>();
        statements.getStatements().forEach(statement -> {
            if(statement instanceof CreateTable){
                Table table = new Table();
                tables.add(table);
                table.setName(((CreateTable) statement).getTable().getName());
                List<ColumnDefinition> jsqlColumnDefinitions = ((CreateTable) statement).getColumnDefinitions();
                List<Column> columns = new ArrayList<>();
                jsqlColumnDefinitions.forEach(columnDefinition -> {
                    Column column = new Column();
                    columns.add(column);
                    column.setName(columnDefinition.getColumnName());
                    column.setDataType(columnDefinition.getColDataType().getDataType());
                    column.setSpecs(columnDefinition.getColumnSpecs());
                });
                table.setColumns(columns);
            }
        });

        return tables;
    }
}
