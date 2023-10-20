package io.github.ngbsn.model.annotations.fieldAnnotations;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

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
