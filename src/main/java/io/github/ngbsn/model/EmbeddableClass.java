package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class EmbeddableClass {
    private String fieldName;
    private String className;
    private List<Column> columns = new ArrayList<>();
    private boolean embeddedId;
}
