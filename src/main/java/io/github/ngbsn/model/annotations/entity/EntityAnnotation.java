package io.github.ngbsn.model.annotations.entity;

import io.github.ngbsn.model.annotations.Annotation;

public class EntityAnnotation implements Annotation {
    @Override
    public String toString() {
        return "@Entity";
    }
}
