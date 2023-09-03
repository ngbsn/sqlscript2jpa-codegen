package org.ngbsn.model.annotations.entityAnnotations;

import org.ngbsn.model.annotations.Annotation;

import java.util.Set;

public class UniqueConstraintAnnotation implements Annotation {
    private Set<String> columnNames;
}
