package org.ngbsn.model.annotations.entityAnnotations;

import org.ngbsn.model.annotations.Annotation;

public class EntityAnnotation implements Annotation {
    @Override
    public String toString() {
        return "@Entity";
    }
}
