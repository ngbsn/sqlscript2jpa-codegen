package io.github.ngbsn.util;

import java.util.HashMap;
import java.util.Map;

public class SQLToJavaMapping {

    private SQLToJavaMapping() {
    }

    private static final Map<String, String> sqlToJavaMap = new HashMap<>();

    static {
        sqlToJavaMap.put("CHAR", "String");
        sqlToJavaMap.put("VARCHAR", "String");
        sqlToJavaMap.put("LONGVARCHAR", "String");
        sqlToJavaMap.put("NUMERIC", "java.math.BigDecimal");
        sqlToJavaMap.put("DECIMAL", "java.math.BigDecimal");
        sqlToJavaMap.put("BIT", "boolean");
        sqlToJavaMap.put("TINYINT", "byte");
        sqlToJavaMap.put("SMALLINT", "short");
        sqlToJavaMap.put("INTEGER", "int");
        sqlToJavaMap.put("INT", "int");
        sqlToJavaMap.put("BIGINT", "long");
        sqlToJavaMap.put("REAL", "float");
        sqlToJavaMap.put("FLOAT", "double");
        sqlToJavaMap.put("DOUBLE", "double");
        sqlToJavaMap.put("BINARY", "byte[]");
        sqlToJavaMap.put("VARBINARY", "byte[]");
        sqlToJavaMap.put("LONGVARBINARY", "byte[]");
        sqlToJavaMap.put("DATE", "java.sql.Date");
        sqlToJavaMap.put("TIME", "java.sql.Time");
        sqlToJavaMap.put("TIMESTAMP", "java.sql.Timestamp");
    }

    public static Map<String, String> getSqlToJavaMap(){
        return sqlToJavaMap;
    }

}
