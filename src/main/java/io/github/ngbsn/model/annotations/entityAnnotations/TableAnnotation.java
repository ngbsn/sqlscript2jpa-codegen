package io.github.ngbsn.model.annotations.entityAnnotations;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class TableAnnotation implements Annotation {
    private String tableName;

    @Override
    public String toString() {
        return "@Table(name = \"" + tableName + "\")";
    }
}
