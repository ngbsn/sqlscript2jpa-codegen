package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class Column {

    private String columnName;
    private String fieldName;
    private String type;
    private boolean primaryKey;
    private boolean sharedPrimaryKey;
    private List<String> annotations = new ArrayList<>();
}
