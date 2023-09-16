package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class JoinColumnAnnotation implements Annotation {
    private String name;
    private String referencedColumnName;
    private Boolean insertable;
    private Boolean updatable;


    @Override
    public String toString() {
        String nameAttr = name != null ? "name = \"" + name + "\"" : "";
        String referencedColumnNameAttr = referencedColumnName != null ? ", referencedColumnName = \"" + referencedColumnName + "\"" : "";
        String insertableAttr = insertable != null ? ", insertable = \"" + insertable + "\"" : "";
        String updatableAttr = updatable != null ? ", updatable = \"" + updatable + "\"" : "";

        return "@JoinColumn(" + nameAttr + referencedColumnNameAttr + insertableAttr + updatableAttr + ")";
    }
}
