package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class ColumnAnnotation implements Annotation {
    private String columnName;

    @Override
    public String toString() {
        return "@Column(name = \"" + columnName + "\")";
    }
}
