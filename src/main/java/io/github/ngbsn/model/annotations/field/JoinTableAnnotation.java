package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class JoinTableAnnotation implements Annotation {
    private String tableName;
    private List<JoinColumnAnnotation> joinColumns;
    private List<JoinColumnAnnotation> inverseJoinColumns;

    @Override
    public String toString() {
        return "@JoinTable(name = \"" + tableName
                + "\", "
                + "joinColumns = {"
                + joinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining("," + System.lineSeparator()))
                + "}, "
                + "inverseJoinColumns = {"
                + inverseJoinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining("," + System.lineSeparator()))
                + "})";
    }
}
