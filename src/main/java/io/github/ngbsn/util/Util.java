package io.github.ngbsn.util;

import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private Util() {
    }

    /**
     * Converts the package name to a folder structure where the sources will be created
     *
     * @param packageName packageName
     * @return Folder path
     */
    public static String packageNameToFolderStructure(final String packageName) {
        return packageName.replace(".", "/") + "/";
    }

    /**
     * Converts table and columns names to class and field names
     *
     * @param word                Table or column name
     * @param captializeFirstChar Whether the first char is upper case. This is applicable for Table names
     * @return Field names and class names
     */
    public static String convertSnakeCaseToCamelCase(String word, boolean captializeFirstChar) {
        word = captializeFirstChar ? WordUtils.capitalize(word) : WordUtils.uncapitalize(word);
        return word.contains("_") ? CaseUtils.toCamelCase(word, captializeFirstChar, '_') : word;
    }

    /**
     * Removes DEFAULT constraint
     *
     * @param extractedStatement statement input
     * @return statement without DEFAULT constraint
     */
    public static String removeDefaultConstraint(String extractedStatement) {
        //does it contain "DEFAULT"
        Pattern patternParentheses = Pattern.compile("DEFAULT\\s+\\(", Pattern.CASE_INSENSITIVE);
        Pattern patternSingleQuote = Pattern.compile("DEFAULT\\s+'", Pattern.CASE_INSENSITIVE);
        Pattern patternDoubleQuote = Pattern.compile("DEFAULT\\s+\"", Pattern.CASE_INSENSITIVE);

        Matcher matcherParentheses = patternParentheses.matcher(extractedStatement);
        Matcher matcherSingleQuote = patternSingleQuote.matcher(extractedStatement);
        Matcher matcherDoubleQuote = patternDoubleQuote.matcher(extractedStatement);

        List<String> stringsToBeRemoved = new ArrayList<>();
        while (matcherParentheses.find()) {
            int index = matcherParentheses.start();
            Deque<Character> stack = new ArrayDeque<>();
            int startIndexOfOpenParentheses = index;
            boolean beginProcess = true;
            while (!stack.isEmpty() || beginProcess) {
                if (extractedStatement.charAt(index) == '(') {
                    stack.add(extractedStatement.charAt(index));
                    beginProcess = false;
                } else if (extractedStatement.charAt(index) == ')') {
                    stack.pop();
                }
                index++;
            }
            int endIndexOfOpenParentheses = index;
            stringsToBeRemoved.add(extractedStatement.substring(startIndexOfOpenParentheses, endIndexOfOpenParentheses));
        }

        while (matcherSingleQuote.find()) {
            int startIndex = matcherSingleQuote.start(); //This includes DEFAULT, needed to remove DEFAULT from statement
            int beginIndex = matcherSingleQuote.end(); //This is needed to begin the search for occurrence of closing quote
            int endIndex = extractedStatement.indexOf("'", beginIndex);
            stringsToBeRemoved.add(extractedStatement.substring(startIndex, endIndex + 1));
        }

        while (matcherDoubleQuote.find()) {
            int startIndex = matcherDoubleQuote.start(); //This includes DEFAULT, needed to remove DEFAULT from statement
            int beginIndex = matcherDoubleQuote.end(); //This is needed to begin the search for occurrence of closing quote
            int endIndex = extractedStatement.indexOf("\"", beginIndex);
            stringsToBeRemoved.add(extractedStatement.substring(startIndex, endIndex + 1));
        }

        for (String s : stringsToBeRemoved) {
            extractedStatement = extractedStatement.replace(s, "");
        }
        return extractedStatement;
    }
}
