package org.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Setter
@Getter
public class Table {

    private String tableName;
    private String className;
    private Set<Column> columns = new HashSet<>();
    private Set<String> annotations = new HashSet<>();
    private int numOfPrimaryKeyColumns;
    private List<ForeignKeyConstraint> foreignKeyConstraints = new ArrayList<>();
    private Set<EmbeddableClass> embeddableClasses = new HashSet<>();
}
