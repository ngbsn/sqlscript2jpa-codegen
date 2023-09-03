package org.ngbsn.model.annotations.fieldAnnotations;

import org.ngbsn.model.annotations.Annotation;

import java.util.List;

public class JoinTableAnnotation implements Annotation {
    private String tableName;
    private List<JoinColumnAnnotation> joinColumns;
    private List<JoinColumnAnnotation> inverseJoinColumns ;
}
