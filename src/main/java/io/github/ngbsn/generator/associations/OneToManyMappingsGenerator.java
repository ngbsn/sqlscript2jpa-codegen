package io.github.ngbsn.generator.associations;

import io.github.ngbsn.generator.models.ModelGenerator;
import io.github.ngbsn.model.Column;
import io.github.ngbsn.model.EmbeddableClass;
import io.github.ngbsn.model.ForeignKeyConstraint;
import io.github.ngbsn.model.Table;
import io.github.ngbsn.model.annotations.field.*;
import io.github.ngbsn.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generate UniDirectional Mappings (one-to-many and many-to-one) for a specific table
 */
public class OneToManyMappingsGenerator {

    private OneToManyMappingsGenerator() {
    }

    /**
     * Generate UniDirectional Mappings (one-to-many and many-to-one) for a specific table for each foreign key constraint
     * Note: Each foreign key constraint in itself can be a composite foreign key
     *
     * @param table                Table model
     * @param foreignKeyConstraint ForeignKeyConstraint model
     */
    public static void addBiDirectionalMappings(final Table table, final ForeignKeyConstraint foreignKeyConstraint) {
        Table parentTable = ModelGenerator.getTablesMap().get(foreignKeyConstraint.getReferencedTableName().replaceAll("[\"']", ""));

        //In the Child table, create a new column having field name as Parent(Referenced) Table, with @ManyTOne annotation
        Column parentTableField = new Column();
        parentTableField.setFieldName(
                Util.convertSnakeCaseToCamelCase(String.join("", foreignKeyConstraint.getColumns()), false)
                        + Util.convertSnakeCaseToCamelCase(parentTable.getTableName(), true));
        parentTableField.setType(parentTable.getClassName());
        parentTableField.getAnnotations().add(new ManyToOneAnnotation().toString());
        table.getColumns().add(parentTableField);

        //In the Parent(Referenced) table, create a new column having field name as child Table, with @OneToMany annotation
        Column childTableField = new Column();
        childTableField.setFieldName(Util.convertSnakeCaseToCamelCase(String.join("", foreignKeyConstraint.getColumns()), false)
                + Util.convertSnakeCaseToCamelCase(table.getTableName(), true) + "Set");
        childTableField.setType("Set<" + table.getClassName() + ">");
        childTableField.getAnnotations().add(OneToManyAnnotation.builder().mappedBy(parentTableField.getFieldName()).build().toString());
        parentTable.getColumns().add(childTableField);

        //Get EmbeddedId (composite Primary Key) for this table. We need this to check if there is a shared primary key
        EmbeddableClass embeddableId = table.getEmbeddedId();
        List<Column> allPrimaryKeyColumns = getAllPrimaryKeys(table, embeddableId); //get all primary keys in the embeddedId

        //Adding JoinColumn and MapsId annotations in below logic
        //Case: Composite Foreign key
        if (foreignKeyConstraint.getColumns().size() > 1) {
            handleCompositeForeignKey(table, parentTable, foreignKeyConstraint, parentTableField, embeddableId, allPrimaryKeyColumns);

        }
        //Case: Single Foreign Key
        else {
            handleSingleForeignKey(table, foreignKeyConstraint, parentTableField, allPrimaryKeyColumns);
        }
    }

    private static void handleSingleForeignKey(final Table table, final ForeignKeyConstraint foreignKeyConstraint,
                                               final Column parentTableField, final List<Column> allPrimaryKeyColumns) {

        //Get the foreign key column from the table.
        List<Column> listOfForeignKeyColumns = listOfForeignKeys(table, foreignKeyConstraint);
        Column foreignKeyColumn = !listOfForeignKeyColumns.isEmpty() ? listOfForeignKeyColumns.get(0) : null;
        if(foreignKeyColumn == null) throw new UnsupportedOperationException(); //some issue in the SQL

        //Case: Shared Single Primary Key
        //Check if foreign key is also a primary key, by iterating through the primary key list
        Optional<Column> optionalColumnPrimaryForeign = allPrimaryKeyColumns.stream()
                .filter(column -> column.getColumnName() != null && column.getColumnName().equals(foreignKeyColumn.getColumnName())).findFirst();
        optionalColumnPrimaryForeign.ifPresentOrElse(column -> {
                    //Case: Shared Primary key
                    //If foreign key is a primary key, don't remove it from table. Set SharedPrimaryKey as true
                    column.setSharedPrimaryKey(true);
                    //Add a @MapsId annotation to the referenced table field
                    parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(column.getFieldName()).build().toString());

                    //Remove existing column annotations and add again with updatable=false, insertable=false
                    //This is necessary as the column is inserted/updated through foreign key
                    column.getAnnotations().removeIf(s -> s.contains("@Column"));
                    column.getAnnotations().add(ColumnAnnotation.builder()
                            .columnName(column.getColumnName())
                            .updatable(false)
                            .insertable(false)
                            .build().toString());

                }
                , () ->
                        //If foreign key is not a primary key, then remove it from the table
                        table.getColumns().remove(foreignKeyColumn)
        );

        //Add a @JoinColumn annotation for the referenced table field
        parentTableField.getAnnotations().add(JoinColumnAnnotation.builder()
                .name(foreignKeyConstraint.getColumns().get(0))
                .referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(0))
                .build().toString());
    }

    private static void handleCompositeForeignKey(final Table table, final Table parentTable, final ForeignKeyConstraint foreignKeyConstraint,
                                                  final Column parentTableField, final EmbeddableClass embeddableId, final List<Column> allPrimaryKeyColumns) {
        List<Column> listOfForeignKeyColumns = listOfForeignKeys(table, foreignKeyConstraint);
        if(embeddableId != null && new HashSet<>(allPrimaryKeyColumns).equals(new HashSet<>(listOfForeignKeyColumns))){
            //Case: Shared Composite Primary Key
            //This case assumes there is a primary composite key
            //If composite foreign key has same columns as composite primary key, don't remove them from table.
            //Add a @MapsId annotation to the referenced table field
            handleCompositeForeignKeySameAsCompositePrimaryKey(table, parentTable, parentTableField);
        }
        else if (embeddableId != null && new HashSet<>(allPrimaryKeyColumns).containsAll(listOfForeignKeyColumns)) {
            //Case: Shared Composite Primary Key
            //This case assumes there is a primary composite key
            //If composite foreign key is inside the composite primary key, don't remove them from table.
            //Add a @MapsId annotation to the referenced table field
            handleCompositeForeignKeyInsideCompositePrimaryKey(parentTable, parentTableField, embeddableId, listOfForeignKeyColumns);
        } else {
            //Case1: There is no Composite primary key
            //TODO can part of Composite foreign key be a primary key. Is this applicable only to self referencing cases?
            //Case2: If composite foreign key is not inside the composite primary key, then remove it from the table
            listOfForeignKeyColumns.forEach(column -> table.getColumns().remove(column));
        }

        List<JoinColumnAnnotation> joinColumns = new ArrayList<>();
        //Create the @JoinColumns annotations for the parentTableField
        for (int i = 0; i < foreignKeyConstraint.getColumns().size(); i++) {
            JoinColumnAnnotation joinColumnAnnotation = JoinColumnAnnotation.builder()
                    .name(foreignKeyConstraint.getColumns().get(i))
                    .referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(i))
                    .build();
            joinColumns.add(joinColumnAnnotation);
        }
        parentTableField.getAnnotations().add(JoinColumnsAnnotation.builder().joinColumns(joinColumns).build().toString());
    }

    private static void handleCompositeForeignKeySameAsCompositePrimaryKey(final Table table, final Table parentTable, final Column parentTableField) {
        //Reuse the parent table embeddedId class to create a field for the shared primary key
        EmbeddableClass parentEmbeddedId = parentTable.getEmbeddedId();
        Column parentEmbeddedIdColumn = new Column();
        parentEmbeddedIdColumn.setType(parentTable.getClassName() + "." + parentEmbeddedId.getClassName());
        parentEmbeddedIdColumn.setFieldName(parentEmbeddedId.getFieldName());
        parentEmbeddedIdColumn.setEmbeddedId(true);
        table.getColumns().add(parentEmbeddedIdColumn);
        //Remove EmbeddedID from child table
        table.setEmbeddedId(null);

        //Add @MapsId annotation, as the shared primary key is not set explicitly in this table, rather managed through parent primary key
        parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(parentEmbeddedIdColumn.getFieldName()).build().toString());
    }

    private static void handleCompositeForeignKeyInsideCompositePrimaryKey(final Table parentTable, final Column parentTableField,
                                                                           final EmbeddableClass embeddableId, final List<Column> listOfForeignKeyColumns) {
        //remove all foreign key columns as they are tracked by the EmbeddedID field of parent
        listOfForeignKeyColumns.forEach(column ->
                embeddableId.getColumns().remove(column)
        );

        //Reuse the parent table embeddedId class to create a field for the shared primary key
        EmbeddableClass parentEmbeddedId = parentTable.getEmbeddedId();
        Column parentEmbeddedIdColumn = new Column();
        parentEmbeddedIdColumn.setType(parentTable.getClassName() + "." + parentEmbeddedId.getClassName());
        parentEmbeddedIdColumn.setFieldName(parentEmbeddedId.getFieldName());
        embeddableId.getColumns().add(parentEmbeddedIdColumn);

        //Add @MapsId annotation, as the shared primary key is not set explicitly in this table, rather managed through parent primary key
        parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(parentEmbeddedIdColumn.getFieldName()).build().toString());
    }


    /**
     * Convert column names in foreignKeyConstraint to List of Column models
     * @param table table
     * @param foreignKeyConstraint foreignKeyConstraint
     * @return List of Columns
     */
    private static List<Column> listOfForeignKeys(final Table table, final ForeignKeyConstraint foreignKeyConstraint) {
        Stream<Column> allColumnsFromTable = table.getColumns().stream();
        Stream<Column> allColumnsFromEmbeddedId = table.getEmbeddedId() != null ? table.getEmbeddedId().getColumns().stream() : Stream.empty();
        return Stream.concat(allColumnsFromTable, allColumnsFromEmbeddedId)
                .filter(column -> foreignKeyConstraint.getColumns().stream().anyMatch(s -> s.equals(column.getColumnName()))).toList();
    }

    private static List<Column> getAllPrimaryKeys(final Table table, final EmbeddableClass embeddableId) {
        //Get list of primary Keys
        if (embeddableId != null) {
            return embeddableId.getColumns().stream().filter(Column::isPrimaryKey).toList();
        } else {
            return table.getColumns().stream().filter(Column::isPrimaryKey).toList();
        }
    }

}
