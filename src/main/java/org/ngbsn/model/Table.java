package org.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class Table {

    private String name;
    private String className;
    private List<Column> columns = new ArrayList<>();
    private int numOfPrimaryKeyColumns;
}
