package io.github.ngbsn.generator;

import io.github.ngbsn.model.Column;
import io.github.ngbsn.model.EmbeddableClass;
import io.github.ngbsn.model.ForeignKeyConstraint;
import io.github.ngbsn.model.Table;
import io.github.ngbsn.model.annotations.field.*;
import io.github.ngbsn.util.Util;
import org.apache.commons.text.WordUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate UniDirectional Mappings (one-to-many and many-to-one) for a specific table
 */
public class UniDirectionalMappingsGenerator {

    private UniDirectionalMappingsGenerator() {
    }

    /**
     * Generate UniDirectional Mappings (one-to-many and many-to-one) for a specific table
     *
     * @param table                Table model
     * @param foreignKeyConstraint ForeignKeyConstraint model
     */
    static void addBothSideUniDirectionalMappings(Table table, ForeignKeyConstraint foreignKeyConstraint) {
        Table referencedTable = ModelGenerator.getTablesMap().get(foreignKeyConstraint.getReferencedTableName().replaceAll("[\"']", ""));

        //In the Child table, create a new column having field name as Parent(Referenced) Table.
        Column parentTableField = new Column();
        parentTableField.setFieldName(
                Util.convertSnakeCaseToCamelCase(String.join("", foreignKeyConstraint.getColumns()), false)
                        + Util.convertSnakeCaseToCamelCase(referencedTable.getTableName(), true));
        parentTableField.setType(referencedTable.getClassName());
        parentTableField.getAnnotations().add(new ManyToOneAnnotation().toString());
        table.getColumns().add(parentTableField);

        //In the Parent(Referenced) table, create a new column having field name as child Table.
        Column childTableField = new Column();
        childTableField.setFieldName(Util.convertSnakeCaseToCamelCase(String.join("", foreignKeyConstraint.getColumns()), false)
                + Util.convertSnakeCaseToCamelCase(table.getTableName(), true) + "Set");
        childTableField.setType("Set<" + table.getClassName() + ">");
        childTableField.getAnnotations().add(OneToManyAnnotation.builder().mappedBy(parentTableField.getFieldName()).build().toString());
        referencedTable.getColumns().add(childTableField);

        //get EmbeddedId for this table
        Optional<EmbeddableClass> optionalEmbeddableId = table.getEmbeddableClasses().stream().filter(EmbeddableClass::isEmbeddedId).findFirst();
        EmbeddableClass embeddableId = optionalEmbeddableId.orElse(null);
        Set<Column> allPrimaryKeyColumns = getAllPrimaryKeys(table, embeddableId); //get all primary keys

        //Case: Composite Foreign key
        if (foreignKeyConstraint.getColumns().size() > 1) {
            handleCompositeForeignKey(table, foreignKeyConstraint, parentTableField, embeddableId, allPrimaryKeyColumns);

        }
        //Case: Single Foreign Key
        else {
            handleSingleForeignKey(table, foreignKeyConstraint, parentTableField, allPrimaryKeyColumns);
        }
    }

    private static void handleSingleForeignKey(Table table, ForeignKeyConstraint foreignKeyConstraint, Column parentTableField, Set<Column> allPrimaryKeyColumns) {
        //Get the foreign key column from the table
        Optional<Column> optionalColumn = table.getColumns().stream().filter(column -> column.getColumnName() != null && column.getColumnName().equals(foreignKeyConstraint.getColumns().get(0))).findFirst();
        if (optionalColumn.isPresent()) {
            Column foreignKeyColumn = optionalColumn.get();
            //Check if foreign key is also a primary key, by iterating through the primary key list
            Optional<Column> optionalColumnPrimaryForeign = allPrimaryKeyColumns.stream().filter(column -> column.getColumnName() != null && column.getColumnName().equals(foreignKeyColumn.getColumnName())).findFirst();
            optionalColumnPrimaryForeign.ifPresentOrElse(column -> {
                        //Case: Shared Primary key
                        //If foreign key is a primary key, don't remove it from table. Set SharedPrimaryKey as true
                        //Add a @MapsId annotation to the referenced table field
                        column.setSharedPrimaryKey(true);
                        parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(column.getFieldName()).build().toString());
                    }
                    , () ->
                            //If foreign key is not a primary key, then remove it from the table
                            optionalColumn.ifPresent(column -> table.getColumns().remove(column))
            );
        }
        //Add a @JoinColumn annotation for the referenced table field
        parentTableField.getAnnotations().add(JoinColumnAnnotation.builder().name(foreignKeyConstraint.getColumns().get(0)).referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(0)).build().toString());
    }

    private static void handleCompositeForeignKey(Table table, ForeignKeyConstraint foreignKeyConstraint, Column parentTableField, EmbeddableClass embeddableId, Set<Column> allPrimaryKeyColumns) {
        Set<Column> setOfForeignKeyColumns = setOfForeignKeys(table, foreignKeyConstraint);
        //Case: Shared Composite Primary Key
        //If composite foreign key is inside the composite primary key, don't remove them from table.
        //This case assumes there is a primary composite key
        //Add a @MapsId annotation to the referenced table field
        if (embeddableId != null && allPrimaryKeyColumns.containsAll(setOfForeignKeyColumns)) {
            handleSharedCompositePrimaryKey(table, parentTableField, embeddableId, setOfForeignKeyColumns);
        } else {
            //There is no primary Composite key
            //If composite foreign key is not inside the composite primary key, then remove it from the table
            setOfForeignKeyColumns.forEach(column -> table.getColumns().remove(column));
        }

        Set<JoinColumnAnnotation> joinColumns = new HashSet<>();
        //Create the @JoinColumn annotations for the parentTableField
        for (int i = 0; i < foreignKeyConstraint.getColumns().size(); i++) {
            JoinColumnAnnotation joinColumnAnnotation = JoinColumnAnnotation.builder().name(foreignKeyConstraint.getColumns().get(i)).referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(i)).build();
            joinColumns.add(joinColumnAnnotation);
        }
        parentTableField.getAnnotations().add(JoinColumnsAnnotation.builder().joinColumns(joinColumns).build().toString());
    }

    private static void handleSharedCompositePrimaryKey(Table table, Column parentTableField, EmbeddableClass embeddableId, Set<Column> setOfForeignKeyColumns) {
        EmbeddableClass foreignCompositeKeyEmbedded = new EmbeddableClass(); //Create a new embeddable for this foreign composite key
        String embeddableName = setOfForeignKeyColumns.stream().map(Column::getFieldName).collect(Collectors.joining());
        foreignCompositeKeyEmbedded.setClassName(WordUtils.capitalize(embeddableName));
        foreignCompositeKeyEmbedded.setFieldName(embeddableName);
        table.getEmbeddableClasses().add(foreignCompositeKeyEmbedded); //add new embeddable to the Table list of Embeddables
        setOfForeignKeyColumns.forEach(column -> {
            //add the individual foreign keys columns the newly created embeddable
            foreignCompositeKeyEmbedded.getColumns().add(column);
            //Remove the individual foreign keys from EmbeddedId and add the newly created embeddable into EmbeddedId
            embeddableId.getColumns().remove(column);
        });
        Column foreignCompositeField = new Column();
        foreignCompositeField.setType(foreignCompositeKeyEmbedded.getClassName());
        foreignCompositeField.setFieldName(foreignCompositeKeyEmbedded.getFieldName());
        embeddableId.getColumns().add(foreignCompositeField);

        if (embeddableId.getFieldName() != null)
            parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(foreignCompositeField.getFieldName()).build().toString());
    }

    private static Set<Column> setOfForeignKeys(Table table, ForeignKeyConstraint foreignKeyConstraint) {
        Stream<Column> allTableForeignKeyColumns = table.getColumns().stream();
        Stream<Column> allEmbeddedForeignKeyColumns = table.getEmbeddableClasses().stream().flatMap(embeddableClass -> embeddableClass.getColumns().stream());
        return Stream.concat(allTableForeignKeyColumns, allEmbeddedForeignKeyColumns).filter(column -> foreignKeyConstraint.getColumns().stream().anyMatch(s -> s.equals(column.getColumnName()))).collect(Collectors.toSet());
    }

    private static Set<Column> getAllPrimaryKeys(Table table, EmbeddableClass embeddableId) {
        //Get set of primary Keys
        if (embeddableId != null) {
            return embeddableId.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toSet());
        } else {
            return table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toSet());
        }
    }

}
