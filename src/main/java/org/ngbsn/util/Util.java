package org.ngbsn.util;

import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;

public class Util {


    public static String packageNameToFolderStructure(final String packageName) {
        return packageName.replaceAll("\\.", "/") + "/";
    }

    public static String convertSnakeCaseToCamelCase(String word, boolean captializeFirstChar){
        word = captializeFirstChar ? WordUtils.capitalize(word) : WordUtils.uncapitalize(word);
        return word.contains("_") ? CaseUtils.toCamelCase(word, captializeFirstChar, '_') : word;
    }
}
