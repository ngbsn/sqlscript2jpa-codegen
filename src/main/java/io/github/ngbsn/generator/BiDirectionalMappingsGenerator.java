package io.github.ngbsn.generator;

import io.github.ngbsn.model.Column;
import io.github.ngbsn.model.ForeignKeyConstraint;
import io.github.ngbsn.model.Table;
import io.github.ngbsn.model.annotations.field.JoinColumnAnnotation;
import io.github.ngbsn.model.annotations.field.JoinTableAnnotation;
import io.github.ngbsn.model.annotations.field.ManyToManyAnnotation;
import io.github.ngbsn.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate BiDirectional Mappings (many-to-many) for a specific table
 */
class BiDirectionalMappingsGenerator {

    private BiDirectionalMappingsGenerator() {
    }

    /**
     * Generate BiDirectional Mappings (many-to-many) for a specific table
     *
     * @param table                    The table to be processed
     * @param foreignKeyConstraintList List of generated foreignKeyConstraintList models
     */
    static void addBiDirectionalMappings(final Table table, final List<ForeignKeyConstraint> foreignKeyConstraintList) {
        Table table1 = ModelGenerator.getTablesMap().get(foreignKeyConstraintList.get(0).getReferencedTableName().replaceAll("[\"']", ""));
        Table table2 = ModelGenerator.getTablesMap().get(foreignKeyConstraintList.get(1).getReferencedTableName().replaceAll("[\"']", ""));

        //Adding @ManyToMany and @JoinTable to table1
        Column column1 = new Column();
        column1.setFieldName(Util.convertSnakeCaseToCamelCase(table2.getTableName(), false));
        column1.setType(table2.getClassName());
        column1.getAnnotations().add(ManyToManyAnnotation.builder().build().toString());
        List<JoinColumnAnnotation> joinColumnAnnotations = new ArrayList<>();
        for (String column : foreignKeyConstraintList.get(0).getColumns()) {
            joinColumnAnnotations.add(JoinColumnAnnotation.builder().name(column).build());
        }
        List<JoinColumnAnnotation> joinInverseColumnAnnotations = new ArrayList<>();
        for (String column : foreignKeyConstraintList.get(1).getColumns()) {
            joinInverseColumnAnnotations.add(JoinColumnAnnotation.builder().name(column).build());
        }
        column1.getAnnotations().add(JoinTableAnnotation.builder().tableName(table.getTableName()).joinColumns(joinColumnAnnotations).inverseJoinColumns(joinInverseColumnAnnotations).build().toString());
        table1.getColumns().add(column1);

        //Adding @ManyToMany(mappedBy) to table2
        Column column2 = new Column();
        column2.setFieldName(Util.convertSnakeCaseToCamelCase(table1.getTableName(), false));
        column2.setType(table1.getClassName());
        column2.getAnnotations().add(ManyToManyAnnotation.builder().mappedBy(column1.getFieldName()).build().toString());
        table2.getColumns().add(column2);
    }
}
