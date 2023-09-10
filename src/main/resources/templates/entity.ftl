package ${package};

<#list imports as import>
import ${import};
</#list>

<#list table.annotations as annotation>
${annotation}
</#list>

public class ${table.className}{
<#if (table.numOfPrimaryKeyColumns > 1) >
    @Embeddable
    static class ${table.embeddableClass.className} implements Serializable{
        <#list table.columns as column>
        <#if column.primaryKey == true>
        <#list column.annotations as annotation>
        ${annotation}
        </#list>
        private ${column.type} ${column.fieldName};
        </#if>
        </#list>
    }

    @EmbeddedId
    private ${table.embeddableClass.className} ${table.embeddableClass.fieldName};
    <#list table.columns as column>
    <#if column.primaryKey == false>
    <#list column.annotations as annotation>
    ${annotation}
    </#list>
    private ${column.type} ${column.fieldName};
    </#if>
    </#list>
<#else>
    <#list table.columns as column>
    <#if column.primaryKey == true>
    @Id
    </#if>
    <#list column.annotations as annotation>
    ${annotation}
    </#list>
    private ${column.type} ${column.fieldName};
    </#list>
</#if>
}