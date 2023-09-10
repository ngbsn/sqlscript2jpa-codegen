package org.ngbsn.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class EmbeddableClass {
    private String className;
    private String fieldName;
}
