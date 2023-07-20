package org.ngbsn.schema.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Column {

    private String name;
    private String type;
    private boolean unique;
    private boolean nullable;
    private boolean primaryKey;
    private boolean foreignKey;
}
