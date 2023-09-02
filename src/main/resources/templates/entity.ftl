package ${package};

<#list imports as import>
import ${import};
</#list>

@Entity
@Table(name = "${table.name}")
<#if (table.numOfPrimaryKeyColumns > 1) >
@IdClass(${table.className}.${table.className}PK.class)
</#if>
public class ${table.className}{
<#if (table.numOfPrimaryKeyColumns > 1) >
    static class ${table.className}PK implements Serializable{
        <#list table.columns as column>
        <#if column.primaryKey == true>
            private ${column.type} ${column.fieldName};
        </#if>
        </#list>
    }
</#if>

<#list table.columns as column>
<#if column.primaryKey == true>
    @Id
</#if>
<#if column.nullable == false>
    @NotNull
</#if>
    @Column(name = "${column.name}")
    private ${column.type} ${column.fieldName};
</#list>
}