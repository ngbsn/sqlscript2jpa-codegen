package org.ngbsn.model.annotations.entityAnnotations;

import org.ngbsn.model.annotations.Annotation;

import java.util.Set;

public class TableAnnotation implements Annotation {
    private String tableName;
    private Set<UniqueConstraintAnnotation> uniqueConstraints;
}
