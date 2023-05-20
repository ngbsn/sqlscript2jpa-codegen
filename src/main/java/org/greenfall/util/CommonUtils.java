package org.greenfall.util;

import org.apache.commons.text.WordUtils;

public class CommonUtils {

    public static String tableNameToEntityClassName(String tableName){
        return WordUtils.capitalizeFully(tableName, '_').replaceAll("_", "");
    }
}
