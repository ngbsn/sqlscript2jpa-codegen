package ${package};

<#list imports as import>
import ${import};
</#list>

// Generated source code

@Getter
@Setter
@Builder
<#list table.annotations as annotation>
${annotation}
</#list>
public class ${table.className}{

    <#list table.tableEnums as tableEnum>
    public enum ${tableEnum.enumName}{
        <#list tableEnum.values as value>
        <#if value?is_last>
        ${value}
        <#else>
        ${value},
        </#if>
        </#list>
    }
    </#list>

<#if (table.numOfPrimaryKeyColumns > 1) >
    <#list table.embeddableClasses as embeddableClass>
    @Getter
    @Setter
    @Builder
    @Embeddable
    public static class ${embeddableClass.className} implements Serializable{
        <#list embeddableClass.columns as column>
        <#list column.annotations as annotation>
        ${annotation}
        </#list>
        private ${column.type} ${column.fieldName};

        </#list>
    }
    </#list>

    <#if table.embeddedId??>
    @Getter
    @Setter
    @Builder
    @Embeddable
    public static class ${table.embeddedId.className} implements Serializable{
        <#list table.embeddedId.columns as column>
        <#list column.annotations as annotation>
        ${annotation}
        </#list>
        private ${column.type} ${column.fieldName};

        </#list>
    }
    @EmbeddedId
    private ${table.embeddedId.className} ${table.embeddedId.fieldName};
    </#if>

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
    <#if column.sharedPrimaryKey == false>
    @GeneratedValue(strategy = GenerationType.AUTO)
    </#if>
    </#if>
    <#list column.annotations as annotation>
    ${annotation}
    </#list>
    private ${column.type} ${column.fieldName};

    </#list>
</#if>
}