package ${package};

<#list imports as import>
import ${import};
</#list>

<#list classAnnotations as annotation>
${annotation}
</#list>

@Table(name = "${table.name}")
public class ${className}{


<#list table.columns as column>
<#if column.primaryKey>
  @Id
</#if>

<#if column.nullable == false>
@NotNull
</#if>

  @Column
  private String ${column.name};
</#list>
}