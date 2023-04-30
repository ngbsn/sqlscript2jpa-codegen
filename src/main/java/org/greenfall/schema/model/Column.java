package org.greenfall.schema.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Column {

    private String name;
    private String dataType;
    private boolean unique;
    private boolean nullable;
}
