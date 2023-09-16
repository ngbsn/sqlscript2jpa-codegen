package org.ngbsn.generator;

import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;
import org.ngbsn.model.Column;
import org.ngbsn.model.EmbeddableClass;
import org.ngbsn.model.ForeignKeyConstraint;
import org.ngbsn.model.Table;
import org.ngbsn.model.annotations.fieldAnnotations.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ngbsn.generator.ModelGenerator.tablesMap;

/**
 * This class contains logic for generating all the association mappings
 */
public class AssociationMappingsGenerator {

    public static void generateMappings(final Table table) {

        List<ForeignKeyConstraint> foreignKeyConstraintList = table.getForeignKeyConstraints();
        if (foreignKeyConstraintList.size() == 1) {
            //Case: There is only 1 Foreign key or 1 Composite Foreign Key
            //Treat this as a regular table and add a new column with for parent field with @ManyToOne
            //and add a new column in ReferencedTable for child field with @OneToMany
            addBothUnidirectionalMappings(table, foreignKeyConstraintList.get(0));

        } else {
            //Case: There are multiple Foreign Keys or multiple Composite Foreign Keys
            //Treat this as a Link table
            List<String> foreignKeyColumns = foreignKeyConstraintList.stream().flatMap(foreignKeyConstraint -> foreignKeyConstraint.getColumns().stream()).toList();
            Optional<Column> optionalKey = table.getColumns().stream().filter(column -> !foreignKeyColumns.contains(column.getColumnName())).findAny();
            if (optionalKey.isEmpty() && foreignKeyConstraintList.size() == 2) {
                //Case: All fields are foreign keys. Also, the relation exits between 2 entities only
                //Remove this link entity from the tablesMap as separate entity is not needed to track Link Table. Use @ManyToMany on other 2 entities
                tablesMap.remove(table.getTableName());
                Table table1 = tablesMap.get(foreignKeyConstraintList.get(0).getReferencedTableName());
                Table table2 = tablesMap.get(foreignKeyConstraintList.get(1).getReferencedTableName());

                //Adding @ManyToMany and @JoinTable to table1
                Column column1 = new Column();
                column1.setFieldName(CaseUtils.toCamelCase(table2.getTableName(), false, '_'));
                column1.setType(table2.getClassName());
                column1.getAnnotations().add(ManyToManyAnnotation.builder().build().toString());
                Set<JoinColumnAnnotation> joinColumnAnnotations = new HashSet<>();
                for (String column : foreignKeyConstraintList.get(0).getColumns()) {
                    joinColumnAnnotations.add(JoinColumnAnnotation.builder().name(column).build());
                }
                Set<JoinColumnAnnotation> joinInverseColumnAnnotations = new HashSet<>();
                for (String column : foreignKeyConstraintList.get(1).getColumns()) {
                    joinInverseColumnAnnotations.add(JoinColumnAnnotation.builder().name(column).build());
                }
                column1.getAnnotations().add(JoinTableAnnotation.builder().tableName(table.getTableName()).joinColumns(joinColumnAnnotations).inverseJoinColumns(joinInverseColumnAnnotations).build().toString());
                table1.getColumns().add(column1);

                //Adding @ManyToMany(mappedBy) to table2
                Column column2 = new Column();
                column2.setFieldName(CaseUtils.toCamelCase(table1.getTableName(), false, '_'));
                column2.setType(table1.getClassName());
                column2.getAnnotations().add(ManyToManyAnnotation.builder().mappedBy(column1.getFieldName()).build().toString());
                table2.getColumns().add(column2);

            } else {
                //Case1: There are some fields that are not foreign keys. So separate entity is needed to track Link Table
                //Case2: All fields are foreign keys. But, the relation exits between 2 or more entities
                //Add @ManyToOne for each foreignKey and corresponding @OneToMany in referenced Table
                foreignKeyConstraintList.forEach(foreignKeyConstraint -> {
                    addBothUnidirectionalMappings(table, foreignKeyConstraint);
                });
            }
        }

    }

    /**
     * This method will handle each foreign key constraint in the table.
     *
     * @param table                table
     * @param foreignKeyConstraint foreignKeyConstraint
     */
    private static void addBothUnidirectionalMappings(Table table, ForeignKeyConstraint foreignKeyConstraint) {
        Table referencedTable = tablesMap.get(foreignKeyConstraint.getReferencedTableName());
        //In the Child table, create a new column having field name as Parent(Referenced) Table.
        Column parentTableField = new Column();
        parentTableField.setFieldName(CaseUtils.toCamelCase(referencedTable.getTableName(), false, '_'));
        parentTableField.setType(referencedTable.getClassName());
        parentTableField.getAnnotations().add(new ManyToOneAnnotation().toString());
        table.getColumns().add(parentTableField);

        //In the Parent(Referenced) table, create a new column having field name as child Table.
        Column childTableField = new Column();
        childTableField.setFieldName(CaseUtils.toCamelCase(table.getTableName(), false, '_'));
        childTableField.setType("Set<" + table.getClassName() + ">");
        childTableField.getAnnotations().add(OneToManyAnnotation.builder().mappedBy(parentTableField.getFieldName()).build().toString());
        referencedTable.getColumns().add(childTableField);

        //get EmbeddedId for this table
        Optional<EmbeddableClass> optionalEmbeddableId = table.getEmbeddableClasses().stream().filter(EmbeddableClass::isEmbeddedId).findFirst();
        EmbeddableClass embeddableId = optionalEmbeddableId.orElse(null);
        Set<Column> allPrimaryKeyColumns = getAllPrimaryKeys(table, embeddableId); //get all primary keys

        //Case: Composite Foreign key
        if (foreignKeyConstraint.getColumns().size() > 1) {
            Set<Column> setOfForeignKeyColumns = setOfForeignKeys(table, foreignKeyConstraint);
            //If composite foreign key is inside the composite primary key, don't remove them from table.
            //This case assumes there is a primary composite key
            //Add a @MapsId annotation to the referenced table field
            if (embeddableId != null && allPrimaryKeyColumns.containsAll(setOfForeignKeyColumns)) {
                EmbeddableClass foreignCompositeKeyEmbedded =  new EmbeddableClass(); //Create a new embeddable for this foreign composite key
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

                if(embeddableId.getFieldName() != null)
                    parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(foreignCompositeField.getFieldName()).build().toString());
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
        //Case: Single Foreign Key
        else {
            //Get the foreign key column from the table
            Optional<Column> optionalColumn = table.getColumns().stream().filter(column -> column.getColumnName() != null && column.getColumnName().equals(foreignKeyConstraint.getReferencedColumns().get(0))).findFirst();
            if (optionalColumn.isPresent()) {
                Column foreignKeyColumn = optionalColumn.get();
                //Check if foreign key is also a primary key, by iterating through the primary key list
                Optional<Column> optionalColumnPrimaryForeign = allPrimaryKeyColumns.stream().filter(column -> column.getColumnName() != null && column.getColumnName().equals(foreignKeyColumn.getColumnName())).findFirst();
                optionalColumnPrimaryForeign.ifPresentOrElse(column -> {
                    //If foreign key is a primary key, don't remove it from table.
                    //Add a @MapsId annotation to the referenced table field
                    parentTableField.getAnnotations().add(MapsIdAnnotation.builder().fieldName(column.getFieldName()).build().toString());
                }, () -> {
                    //If foreign key is not a primary key, then remove it from the table
                    optionalColumn.ifPresent(column -> table.getColumns().remove(column));
                });
            }
            //Add a @JoinColumn annotation for the referenced table field
            parentTableField.getAnnotations().add(JoinColumnAnnotation.builder().name(foreignKeyConstraint.getReferencedColumns().get(0)).referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(0)).build().toString());
        }
    }

    private static Set<Column> setOfForeignKeys(Table table, ForeignKeyConstraint foreignKeyConstraint) {
        Stream<Column> allTableForeignKeyColumns = table.getColumns().stream();
        Stream<Column> allEmbeddedForeignKeyColumns = table.getEmbeddableClasses().stream().flatMap(embeddableClass -> embeddableClass.getColumns().stream());
        return Stream.concat(allTableForeignKeyColumns, allEmbeddedForeignKeyColumns).filter(column -> foreignKeyConstraint.getColumns().stream().anyMatch(s -> s.equals(column.getColumnName()))).collect(Collectors.toSet());
    }

    private static Set<Column> getAllPrimaryKeys(Table table, EmbeddableClass embeddableId) {
        //Get set of primary Keys
        if(embeddableId != null){
            return embeddableId.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toSet());
        }else{
            return table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toSet());
        }
    }
}
