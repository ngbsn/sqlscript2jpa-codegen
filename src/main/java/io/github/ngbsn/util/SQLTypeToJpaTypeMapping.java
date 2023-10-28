package io.github.ngbsn.util;

import java.util.HashMap;
import java.util.Map;

public class SQLTypeToJpaTypeMapping {

    private SQLTypeToJpaTypeMapping() {
    }

    private static final Map<String, String> sqlToJavaMap = new HashMap<>();

    public static final String STRING = "String";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String DOUBLE = "double";
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String BIG_DECIMAL = "java.math.BigDecimal";

    static {
        // Numeric Types
        sqlToJavaMap.put("NUMERIC", BIG_DECIMAL);
        sqlToJavaMap.put("DECIMAL", BIG_DECIMAL);
        sqlToJavaMap.put("TINYINT", "byte");
        sqlToJavaMap.put("SMALLINT", "short");
        sqlToJavaMap.put("MEDIUMINT", INT);
        sqlToJavaMap.put("INTEGER", INT);
        sqlToJavaMap.put("INT", INT);
        sqlToJavaMap.put("BIGINT", "long");
        sqlToJavaMap.put("LONG", "long");
        sqlToJavaMap.put("REAL", FLOAT);
        sqlToJavaMap.put("BINARY_FLOAT", DOUBLE);
        sqlToJavaMap.put("BINARY_DOUBLE", DOUBLE);
        sqlToJavaMap.put("FLOAT", DOUBLE);
        sqlToJavaMap.put("DOUBLE", DOUBLE);
        sqlToJavaMap.put("DOUBLE PRECISION", DOUBLE);
        sqlToJavaMap.put("SMALLSERIAL", INT);
        sqlToJavaMap.put("SERIAL", INT);
        sqlToJavaMap.put("BIGSERIAL", INT);

        //Boolean types
        sqlToJavaMap.put("BIT", "boolean");
        sqlToJavaMap.put("BOOLEAN", "boolean");

        //Date and Time Types
        sqlToJavaMap.put("DATE", "java.sql.Date");
        sqlToJavaMap.put("TIME", "java.sql.Time");
        sqlToJavaMap.put("TIMESTAMP", "java.sql.Timestamp");
        sqlToJavaMap.put("DATETIME", "java.sql.Timestamp"); // Use @Temporal

        //String Types
        sqlToJavaMap.put("CHARACTER", STRING);
        sqlToJavaMap.put("CHAR", STRING);
        sqlToJavaMap.put("VARYING", STRING);
        sqlToJavaMap.put("CHARACTER VARYING", STRING);
        sqlToJavaMap.put("CHAR VARYING", STRING);
        sqlToJavaMap.put("NATIONAL CHARACTER", STRING);
        sqlToJavaMap.put("NATIONAL CHAR", STRING);
        sqlToJavaMap.put("NCHAR", STRING);
        sqlToJavaMap.put("NATIONAL CHARACTER VARYING", STRING);
        sqlToJavaMap.put("NATIONAL CHAR VARYING", STRING);
        sqlToJavaMap.put("NCHAR VARYING", STRING);
        sqlToJavaMap.put("BPCHAR", STRING);
        sqlToJavaMap.put("VARCHAR", STRING);
        sqlToJavaMap.put("VARCHAR2", STRING);
        sqlToJavaMap.put("NVARCHAR2", STRING);
        sqlToJavaMap.put("LONGVARCHAR", STRING);
        sqlToJavaMap.put("LONG VARCHAR", STRING);

        // Use @Lob
        sqlToJavaMap.put("BYTEA", STRING);
        sqlToJavaMap.put("CLOB ", STRING);
        sqlToJavaMap.put("NCLOB ", STRING);
        sqlToJavaMap.put("TINYTEXT ", STRING);
        sqlToJavaMap.put("TEXT ", STRING);
        sqlToJavaMap.put("MEDIUMTEXT ", STRING);
        sqlToJavaMap.put("LONGTEXT ", STRING);

        sqlToJavaMap.put("TINYBLOB ", BYTE_ARRAY);
        sqlToJavaMap.put("BLOB ", BYTE_ARRAY);
        sqlToJavaMap.put("MEDIUMBLOB ", BYTE_ARRAY);
        sqlToJavaMap.put("LONGBLOB ", BYTE_ARRAY);

        sqlToJavaMap.put("RAW", BYTE_ARRAY);
        sqlToJavaMap.put("LONG RAW", BYTE_ARRAY);
        sqlToJavaMap.put("BINARY", BYTE_ARRAY);
        sqlToJavaMap.put("VARBINARY", BYTE_ARRAY);
        sqlToJavaMap.put("LONGVARBINARY", BYTE_ARRAY);

    }

    /**
     * Return the JPA type mapping for a SQL type
     * @param dataType SQL type
     * @return JPA type
     */
    public static String getTypeMapping(final String dataType) {
        return sqlToJavaMap.get(dataType.toUpperCase());
    }

}
