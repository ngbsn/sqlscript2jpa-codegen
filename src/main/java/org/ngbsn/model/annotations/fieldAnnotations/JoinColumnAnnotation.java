package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class JoinColumnAnnotation implements Annotation {
    private String name;
    private String referencedColumnName;

    @Override
    public String toString() {
        String nameAttr = name != null ? "name = \"" + name + "\"" : "";
        String referencedColumnNameAttr = referencedColumnName != null ? ", referencedColumnName = \"" + referencedColumnName + "\"" : "";
        return "@JoinColumn(" + nameAttr + referencedColumnNameAttr + ")";
    }
}
