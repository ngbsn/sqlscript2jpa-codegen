package org.greenfall.schema.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForeignKey {

    private List<String> columns;
    private String referenceTable;
    private List<String> refColumns;
}
