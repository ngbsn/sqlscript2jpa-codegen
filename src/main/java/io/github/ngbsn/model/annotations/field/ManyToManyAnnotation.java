package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class ManyToManyAnnotation implements Annotation {

    private String mappedBy;

    @Override
    public String toString() {

        return mappedBy == null ? "@ManyToMany" : "@ManyToMany(mappedBy = \"" + mappedBy + "\")";
    }
}
