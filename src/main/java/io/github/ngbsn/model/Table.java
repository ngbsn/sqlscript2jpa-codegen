package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Setter
@Getter
public class Table {

    private String tableName;
    private String className;
    private List<Column> columns = new ArrayList<>();
    private List<String> annotations = new ArrayList<>();
    private int numOfPrimaryKeyColumns;
    private List<ForeignKeyConstraint> foreignKeyConstraints = new ArrayList<>();
    private List<EmbeddableClass> embeddableClasses = new ArrayList<>();
    private List<TableEnum> tableEnums = new ArrayList<>();
}
