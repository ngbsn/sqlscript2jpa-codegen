package org.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class Column {

    private String columnName;
    private String fieldName;
    private String type;
    private boolean primaryKey;
    private Set<String> annotations = new HashSet<>();
}
