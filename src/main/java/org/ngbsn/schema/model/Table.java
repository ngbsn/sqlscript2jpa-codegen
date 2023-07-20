package org.ngbsn.schema.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class Table {

    private String name;
    private List<Column> columns = new ArrayList<>();
}
