package io.github.ngbsn.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TableEnum {
    private String enumName;
    private List<String> values = new ArrayList<>();
}
