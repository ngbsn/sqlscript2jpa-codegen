package ${package};

<#list imports as import>
import ${import};
</#list>

// Generated source code

@NoArgsConstructor
@AllArgsConstructor
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

<#-- Case: Composite Primary Key -->
<#if (table.numOfPrimaryKeyColumns > 1) >
    <#if table.embeddedId??>
    @NoArgsConstructor
    @AllArgsConstructor
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
    <#if column.primaryKey == false && column.embeddedId == false>
    <#list column.annotations as annotation>
    ${annotation}
    </#list>
    private ${column.type} ${column.fieldName};
    </#if>
    <#-- This is for shared primary key usecase -->
    <#if column.embeddedId == true>
    @EmbeddedId
    private ${column.type} ${column.fieldName};
    </#if>

    </#list>
<#else>
    <#list table.columns as column>
    <#if column.primaryKey == true>
    @Id
     <#-- If foreign key is primary key, then no need for ID generation in this table, its handled by referenced table -->
    <#if column.sharedPrimaryKey == false && (column.type == "Integer" || column.type == "Short" || column.type == "Long") >
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    </#if>
    <#list column.annotations as annotation>
    ${annotation}
    </#list>
    private ${column.type} ${column.fieldName};

    </#list>
</#if>
}