package io.github.ngbsn.generator;

import io.github.ngbsn.model.Column;
import io.github.ngbsn.model.ForeignKeyConstraint;
import io.github.ngbsn.model.Table;
import io.github.ngbsn.model.annotations.fieldAnnotations.JoinColumnAnnotation;
import io.github.ngbsn.model.annotations.fieldAnnotations.JoinTableAnnotation;
import io.github.ngbsn.model.annotations.fieldAnnotations.ManyToManyAnnotation;
import io.github.ngbsn.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.ngbsn.generator.ModelGenerator.tablesMap;

public class BiDirectionalMappingsGenerator {
    static void addBiDirectionalMappings(Table table, List<ForeignKeyConstraint> foreignKeyConstraintList) {
        Table table1 = tablesMap.get(foreignKeyConstraintList.get(0).getReferencedTableName().replaceAll("[\"']", ""));
        Table table2 = tablesMap.get(foreignKeyConstraintList.get(1).getReferencedTableName().replaceAll("[\"']", ""));

        //Adding @ManyToMany and @JoinTable to table1
        Column column1 = new Column();
        column1.setFieldName(Util.convertSnakeCaseToCamelCase(table2.getTableName(), false));
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
        column2.setFieldName(Util.convertSnakeCaseToCamelCase(table1.getTableName(), false));
        column2.setType(table1.getClassName());
        column2.getAnnotations().add(ManyToManyAnnotation.builder().mappedBy(column1.getFieldName()).build().toString());
        table2.getColumns().add(column2);
    }
}
