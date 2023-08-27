package org.ngbsn.util;

public class Util {


    public static String packageNameToFolderStructure(final String packageName){
        return packageName.replaceAll("\\.","/") + "/";
    }
}
