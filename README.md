# sqlscript2jpa-codegen

A Java library to generate JPA entities from DDL statements. The library offers both a maven plugin and a jar to be run as a standalone tool.

### Getting Started

## Maven

**sqlscript2jpa-codegen** is available at [Maven Central Repository](https://central.sonatype.com/artifact/io.github.ngbsn/sqlscript2jpa-codegen-maven-plugin).
To use it, simply declare the following plugin in your pom file:

```xml
            <plugin>
                <groupId>io.github.ngbsn</groupId>
                <artifactId>sqlscript2jpa-codegen-maven-plugin</artifactId>
                <version>${latest-version-from-maven-central}</version>
                <executions>
                    <execution>
                        <id>parse-schema</id>
                        <phase>compile</phase>
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
```
**sqlFilePath**: Path to the SQL file containing the DDL statements.

**packageName**: The package name for the generated entities.

By default, the source code will be generated under `target/generated-sources/sqlscript2jpa`

## Standalone

Get the jar from https://repo1.maven.org/maven2/io/github/ngbsn/sqlscript2jpa-codegen-maven-plugin/1.0.3/sqlscript2jpa-codegen-maven-plugin-1.0.3-standalone.jar

```
java -jar sqlscript2jpa-codegen-maven-plugin-1.0.3-standalone.jar "<sql_file_path>" "<package_name>"
```
This will generate the JPA entities in a folder structure as defined by the package name
