package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class JoinColumnsAnnotation implements Annotation {
    private Set<JoinColumnAnnotation> joinColumns;

    @Override
    public String toString() {
        return "@JoinColumns({"
                + joinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining(", "))
                + "})";
    }
}
