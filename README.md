# sqlscript2jpa-codegen

A Java tool to generate Lombok wired JPA entities from DDL statements. The library offers both a maven plugin and a
jar to be run as a standalone tool. It internally uses JSqlParser to parse the DDL statements.

The tool can generate the following, 
- Entities for all tables with annotations @Table and @Column for table and column names respectively.
- @OneToMany, @ManyToMany, @JoinColumn annotations for association mappings. 
- @Embeddable and @EmbeddedId for composite keys. 
- @MapsId for shared primary keys. 
- javax validation annotation @NotNull for the SQL NOT NULL Constraint.
and much more!

### Getting Started

## Prerequisites

1. The SQL file should contain only SQL statements
2. All SQL statements should terminate with a semicolon(;)
3. Presence of other instructions such as PL/SQL blocks may get ignored if properly terminated with semicolons.
4. Supported DDL statements are:
    * CREATE TABLE
    * ALTER TABLE ADD CONSTRAINT PRIMARY KEY
    * ALTER TABLE ADD CONSTRAINT FOREIGN KEY
    * ALTER TABLE ADD COLUMN
    * ALTER TABLE DROP COLUMN
5. Not all SQL Types are supported. Unsupported types get generated as Java Type **Object**.

## Maven

**sqlscript2jpa-codegen** is available
at [Maven Central Repository](https://central.sonatype.com/artifact/io.github.ngbsn/sqlscript2jpa-codegen-maven-plugin).
To use it, simply declare the following plugins in your pom file:

```xml
            <plugins>
               <plugin>
                  <groupId>io.github.ngbsn</groupId>
                  <artifactId>sqlscript2jpa-codegen-maven-plugin</artifactId>
                  <version>${latest-version-from-maven-central}</version>
                  <executions>
                     <execution>
                        <id>parse-schema</id>
                        <phase>generate-sources</phase>
                        <goals>
                           <goal>parse-schema</goal>
                        </goals>
                        <configuration>
                           <sqlFilePath>${basedir}/src/main/resources/sql/organization.sql</sqlFilePath>
                           <packageName>org.mycompany.entities</packageName>
                        </configuration>
                     </execution>
                  </executions>
               </plugin>
               <plugin>
                  <groupId>org.codehaus.mojo</groupId>
                  <artifactId>build-helper-maven-plugin</artifactId>
                  <version>3.2.0</version>
                  <executions>
                     <execution>
                        <id>add-source</id>
                        <phase>generate-sources</phase>
                        <goals>
                           <goal>add-source</goal>
                        </goals>
                        <configuration>
                           <sources>
                              <source>target/generated-sources/sqlscript2jpa/src/main/java/</source>
                           </sources>
                        </configuration>
                     </execution>
                  </executions>
               </plugin>   
            </plugins>

```

Also, you would need to add lombok to your dependencies:

```xml
            <dependency>
               <groupId>org.projectlombok</groupId>
               <artifactId>lombok</artifactId>
               <version>1.18.28</version>
               <scope>provided</scope>
            </dependency>
```

**sqlFilePath**: Path to the SQL file containing the DDL statements.

**packageName**: The package name for the generated entities.

By default, the source code will be generated under `target/generated-sources/sqlscript2jpa`

## Standalone

Get the jar
from https://repo1.maven.org/maven2/io/github/ngbsn/sqlscript2jpa-codegen-maven-plugin/1.0.5/sqlscript2jpa-codegen-maven-plugin-1.0.6-standalone.jar

```
java -jar sqlscript2jpa-codegen-maven-plugin-1.0.6-standalone.jar "<sql_file_path>" "<package_name>"
```

This will generate the JPA entities in a folder structure as defined by the package name
under `./target/generated-sources/sqlscript2jpa`

