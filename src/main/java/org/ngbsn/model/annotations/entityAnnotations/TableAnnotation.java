package org.ngbsn.model.annotations.entityAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class TableAnnotation implements Annotation {
    private String tableName;

    @Override
    public String toString() {
        return "@Table(name = \"" + tableName + "\")";
    }
}
