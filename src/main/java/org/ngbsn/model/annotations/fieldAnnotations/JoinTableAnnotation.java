package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class JoinTableAnnotation implements Annotation {
    private String tableName;
    private Set<JoinColumnAnnotation> joinColumns;
    private Set<JoinColumnAnnotation> inverseJoinColumns;

    @Override
    public String toString() {
        return "@JoinTable(name = \"" + tableName
                + "\", "
                + "joinColumns = {\""
                + joinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining("," + System.lineSeparator()))
                + "}, "
                + "inverseJoinColumns = {\""
                + inverseJoinColumns.stream().map(JoinColumnAnnotation::toString).collect(Collectors.joining("," + System.lineSeparator()))
                + "})";
    }
}
