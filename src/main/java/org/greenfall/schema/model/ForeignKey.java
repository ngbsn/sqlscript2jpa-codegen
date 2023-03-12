package org.greenfall.schema.model;

public class ForeignKey {

    private String column;
    private String referenceTable;
    private String referenceColumn;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    public String getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(String referenceColumn) {
        this.referenceColumn = referenceColumn;
    }
}
