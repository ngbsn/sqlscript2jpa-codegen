package io.github.ngbsn.util;

import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;

public class Util {

    private Util() {
    }

    /**
     * Converts the package name to a folder structure where the sources will be created
     * @param packageName packageName
     * @return Folder path
     */
    public static String packageNameToFolderStructure(final String packageName) {
        return packageName.replace(".", "/") + "/";
    }

    /**
     * Converts table and columns names to class and field names
     * @param word Table or column name
     * @param captializeFirstChar Whether the first char is upper case. This is applicable for Table names
     * @return Field names and class names
     */
    public static String convertSnakeCaseToCamelCase(String word, boolean captializeFirstChar) {
        word = captializeFirstChar ? WordUtils.capitalize(word) : WordUtils.uncapitalize(word);
        return word.contains("_") ? CaseUtils.toCamelCase(word, captializeFirstChar, '_') : word;
    }
}
