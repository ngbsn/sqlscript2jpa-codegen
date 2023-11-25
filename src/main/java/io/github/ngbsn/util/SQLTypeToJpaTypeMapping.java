package io.github.ngbsn.util;

import java.util.HashMap;
import java.util.Map;

public class SQLTypeToJpaTypeMapping {

    public static final String STRING = "String";
    public static final String BYTE_ARRAY = "Byte[]";
    public static final String DOUBLE = "Double";
    public static final String INT = "Integer";
    public static final String FLOAT = "Float";
    public static final String BIG_DECIMAL = "java.math.BigDecimal";
    private static final Map<String, String> sqlToJavaMap = new HashMap<>();

    public static final String LONG = "Long";

    public static final String BOOLEAN = "Boolean";

    public static final String JAVA_SQL_TIMESTAMP = "java.sql.Timestamp";

    public static final String JAVA_SQL_DATE = "java.sql.Date";

    public static final String JAVA_SQL_TIME = "java.sql.Time";

    public static final String SHORT = "Short";

    public static final String BYTE = "Byte";

    static {
        //Id
        sqlToJavaMap.put("UUID", "java.util.UUID");

        // Numeric Types
        sqlToJavaMap.put("NUMERIC", BIG_DECIMAL);
        sqlToJavaMap.put("DECIMAL", BIG_DECIMAL);
        sqlToJavaMap.put("TINYINT", BYTE);
        sqlToJavaMap.put("SMALLINT", SHORT);
        sqlToJavaMap.put("MEDIUMINT", INT);
        sqlToJavaMap.put("INTEGER", INT);
        sqlToJavaMap.put("INT", INT);
        sqlToJavaMap.put("BIGINT", LONG);
        sqlToJavaMap.put("LONG", LONG);
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
        sqlToJavaMap.put("BIT", BOOLEAN);
        sqlToJavaMap.put("BOOLEAN", BOOLEAN);

        //Date and Time Types
        sqlToJavaMap.put("DATE", JAVA_SQL_DATE);
        sqlToJavaMap.put("TIME", JAVA_SQL_TIME);
        sqlToJavaMap.put("TIMESTAMP", JAVA_SQL_TIMESTAMP);
        sqlToJavaMap.put("DATETIME", JAVA_SQL_TIMESTAMP); // Use @Temporal

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
        sqlToJavaMap.put("CLOB", STRING);
        sqlToJavaMap.put("NCLOB", STRING);
        sqlToJavaMap.put("TINYTEXT", STRING);
        sqlToJavaMap.put("TEXT", STRING);
        sqlToJavaMap.put("MEDIUMTEXT", STRING);
        sqlToJavaMap.put("LONGTEXT", STRING);

        sqlToJavaMap.put("TINYBLOB", BYTE_ARRAY);
        sqlToJavaMap.put("BLOB", BYTE_ARRAY);
        sqlToJavaMap.put("MEDIUMBLOB", BYTE_ARRAY);
        sqlToJavaMap.put("LONGBLOB", BYTE_ARRAY);

        sqlToJavaMap.put("RAW", BYTE_ARRAY);
        sqlToJavaMap.put("LONG RAW", BYTE_ARRAY);
        sqlToJavaMap.put("BINARY", BYTE_ARRAY);
        sqlToJavaMap.put("VARBINARY", BYTE_ARRAY);
        sqlToJavaMap.put("LONGVARBINARY", BYTE_ARRAY);

    }

    private SQLTypeToJpaTypeMapping() {
    }

    /**
     * Return the JPA type mapping for a SQL type
     *
     * @param dataType SQL type
     * @return JPA type
     */
    public static String getTypeMapping(final String dataType) {
        return sqlToJavaMap.get(dataType.toUpperCase());
    }

}
