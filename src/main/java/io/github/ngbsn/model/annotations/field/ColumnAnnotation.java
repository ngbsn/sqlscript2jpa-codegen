package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class ColumnAnnotation implements Annotation {
    private String columnName;
    private Boolean insertable;
    private Boolean updatable;

    @Override
    public String toString() {
        String nameAttr = columnName != null ? "name = \"" + columnName + "\"" : "";
        String insertableAttr = insertable != null ? ", insertable = " + insertable : "";
        String updatableAttr = updatable != null ? ", updatable = " + updatable : "";
        return "@Column(" + nameAttr + insertableAttr + updatableAttr + ")";
    }
}
