package ${package};

<#list imports as import>
import ${import};
</#list>

<#list table.annotations as annotation>
${annotation}
</#list>

public class ${table.className}{
<#if (table.numOfPrimaryKeyColumns > 1) >
    <#list table.embeddableClasses as embeddableClass>
    @Embeddable
    static class ${embeddableClass.className} implements Serializable{
        <#list embeddableClass.columns as column>
        <#list column.annotations as annotation>
        ${annotation}
        </#list>
        private ${column.type} ${column.fieldName};
        </#list>
   }

    <#if embeddableClass.embeddedId == true>
    @EmbeddedId
    private ${embeddableClass.className} ${embeddableClass.fieldName};
    </#if>
    </#list>

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