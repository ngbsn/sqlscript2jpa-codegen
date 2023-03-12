package org.greenfall.schema.model;

import java.util.List;

public class PrimaryKey {
    private List<String> columns;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
