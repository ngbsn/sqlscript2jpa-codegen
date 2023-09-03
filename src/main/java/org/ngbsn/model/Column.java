package org.ngbsn.model;

import lombok.Getter;
import lombok.Setter;
import org.ngbsn.model.annotations.Annotation;

import java.util.List;

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
    private List<Annotation> annotations;
}
