package io.github.ngbsn.model.annotations.fieldAnnotations;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class OneToManyAnnotation implements Annotation {
    private String mappedBy;

    @Override
    public String toString() {
        return "@OneToMany(mappedBy = \"" + mappedBy + "\")";
    }
}
