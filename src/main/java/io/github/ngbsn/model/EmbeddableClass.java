package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class EmbeddableClass {
    private String fieldName;
    private String className;
    private Set<Column> columns = new HashSet<>();
    private boolean embeddedId;
}
