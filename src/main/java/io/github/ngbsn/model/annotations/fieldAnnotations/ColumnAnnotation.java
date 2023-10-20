package io.github.ngbsn.model.annotations.fieldAnnotations;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class ColumnAnnotation implements Annotation {
    private String columnName;

    @Override
    public String toString() {
        return "@Column(name = \"" + columnName + "\")";
    }
}
