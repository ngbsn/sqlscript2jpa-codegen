package org.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Column {

    private String name;
    private String fieldName;
    private String type;
    private boolean unique;
    private boolean nullable;
    private boolean primaryKey;
    private boolean foreignKey;
}
