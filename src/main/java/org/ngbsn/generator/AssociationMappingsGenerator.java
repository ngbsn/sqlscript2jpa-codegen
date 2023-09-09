package org.ngbsn.generator;

import org.apache.commons.text.CaseUtils;
import org.ngbsn.model.Column;
import org.ngbsn.model.ForeignKeyConstraint;
import org.ngbsn.model.Table;
import org.ngbsn.model.annotations.fieldAnnotations.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.ngbsn.generator.ModelGenerator.tablesMap;

/**
 * Is there more than 1 foreign key? - This is a linked table
 * Are all fields foreign keys? @ManyToMany and no separate entity needed for linked table
 * Are there some non-foreign key fields? - @ManyToOne in a separate entity for linked table
 * <p>
 * Is there a primary key in the foreign keys list? Mark this as @MapsId
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
            Optional<Column> optionalKey = table.getColumns().stream().filter(column -> !foreignKeyColumns.contains(column.getName())).findAny();
            if (optionalKey.isEmpty() && foreignKeyConstraintList.size() == 2) {
                //Case: All fields are foreign keys. Also, the relation exits between 2 entities only
                //Remove this link entity from the tablesMap as separate entity is not needed to track Link Table. Use @ManyToMany on other 2 entities
                tablesMap.remove(table.getName());
                Table table1 = tablesMap.get(foreignKeyConstraintList.get(0).getReferencedTableName());
                Table table2 = tablesMap.get(foreignKeyConstraintList.get(1).getReferencedTableName());

                Column column1 = new Column();
                column1.setFieldName(table2.getClassName().toLowerCase());
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

                column1.getAnnotations().add(JoinTableAnnotation.builder().tableName(table.getName()).joinColumns(joinColumnAnnotations).inverseJoinColumns(joinInverseColumnAnnotations).build().toString());

                Column column2 = new Column();
                column2.setFieldName(table1.getClassName().toLowerCase());
                column2.setType(table1.getClassName());
                column2.getAnnotations().add(ManyToManyAnnotation.builder().mappedBy(column1.getFieldName()).toString());

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

    private static void addBothUnidirectionalMappings(Table table, ForeignKeyConstraint foreignKeyConstraint) {
        Table referencedTable = tablesMap.get(foreignKeyConstraint.getReferencedTableName());
        Column foreignKeyColumn = new Column();
        foreignKeyColumn.setFieldName(referencedTable.getClassName().toLowerCase());
        foreignKeyColumn.setType(referencedTable.getClassName());
        foreignKeyColumn.getAnnotations().add(new ManyToOneAnnotation().toString());
        table.getColumns().add(foreignKeyColumn);

        Column childKeyColumn = new Column();
        childKeyColumn.setFieldName(table.getClassName().toLowerCase());
        childKeyColumn.setType("Set<" + table.getClassName() + ">");
        childKeyColumn.getAnnotations().add(OneToManyAnnotation.builder().mappedBy(foreignKeyColumn.getFieldName()).build().toString());
        referencedTable.getColumns().add(childKeyColumn);

        if (foreignKeyConstraint.getColumns().size() > 1) {
            //Case: Composite Foreign key
            Set<JoinColumnAnnotation> joinColumns = new HashSet<>();
            for (int i = 0; i < foreignKeyConstraint.getColumns().size(); i++) {
                int finalI = i;
                Optional<Column> optionalColumn = table.getColumns().stream().filter(column -> column.getName().equals(foreignKeyConstraint.getColumns().get(finalI))).findFirst();
                optionalColumn.ifPresent(column -> table.getColumns().remove(column));

                JoinColumnAnnotation joinColumnAnnotation = JoinColumnAnnotation.builder().name(foreignKeyConstraint.getColumns().get(i)).referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(i)).build();
                joinColumns.add(joinColumnAnnotation);

            }
            foreignKeyColumn.getAnnotations().add(JoinColumnsAnnotation.builder().joinColumns(joinColumns).build().toString());

        } else {
            //Case: Single Foreign Key
            Optional<Column> optionalColumn = table.getColumns().stream().filter(column -> column.getName().equals(foreignKeyConstraint.getReferencedColumns().get(0))).findFirst();
            optionalColumn.ifPresent(column -> table.getColumns().remove(column));
            foreignKeyColumn.getAnnotations().add(JoinColumnAnnotation.builder().name(CaseUtils.toCamelCase(foreignKeyConstraint.getReferencedColumns().get(0), false, '_')).referencedColumnName(foreignKeyConstraint.getReferencedColumns().get(0)).build().toString());
        }
    }
}
