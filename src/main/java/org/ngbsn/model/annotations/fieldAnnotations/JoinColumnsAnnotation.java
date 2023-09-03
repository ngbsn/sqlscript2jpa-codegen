package org.ngbsn.model.annotations.fieldAnnotations;

import org.ngbsn.model.annotations.Annotation;

import java.util.Set;

public class JoinColumnsAnnotation implements Annotation {
    private Set<JoinColumnAnnotation> joinColumns;
}
