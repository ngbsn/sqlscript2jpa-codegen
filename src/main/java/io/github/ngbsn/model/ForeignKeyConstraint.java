package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ForeignKeyConstraint {
    private List<String> columns = new ArrayList<>();
    private List<String> referencedColumns = new ArrayList<>();
    private String referencedTableName;

}
