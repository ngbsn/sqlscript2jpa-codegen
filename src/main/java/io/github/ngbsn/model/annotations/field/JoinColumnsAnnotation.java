package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class JoinColumnsAnnotation implements Annotation {
    private List<JoinColumnAnnotation> joinColumns;

    @Override
    public String toString() {
        return "@JoinColumns({"
                + joinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining(", "))
                + "})";
    }
}
