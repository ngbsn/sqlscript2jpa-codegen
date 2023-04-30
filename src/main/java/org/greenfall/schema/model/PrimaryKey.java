package org.greenfall.schema.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PrimaryKey {
    private List<String> columns;
}
