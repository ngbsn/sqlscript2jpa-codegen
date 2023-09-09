package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class OneToManyAnnotation implements Annotation {
    private String mappedBy;

    @Override
    public String toString() {
        return "@OneToMany(mappedBy = \"" + mappedBy + "\")";
    }
}
