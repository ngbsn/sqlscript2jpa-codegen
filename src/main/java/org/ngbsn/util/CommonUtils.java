package org.ngbsn.util;

import org.apache.commons.text.WordUtils;

public class CommonUtils {

    public static String tableNameToEntityClassName(final String tableName){
        return WordUtils.capitalizeFully(tableName, '_').replaceAll("_", "");
    }

    public static String packageNameToFolderStructure(final String packageName){
        return packageName.replaceAll("\\.","/") + "/";
    }
}
