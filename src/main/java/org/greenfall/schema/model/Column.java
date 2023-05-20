package org.greenfall.schema.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
