![Application screensho](/docs/Screenshot_1.png)

# LPAdesktop

LPA Simulator is an application for managing eSIM profiles on removable eUICCs. 

The current version supports the following features:
-	List installed profiles
- Download new profile
- Enable profile
- Disable profile
- Delete profile
- Retrieve EID 
- Retrieve Root SM-DS and Default SM-DP+ addresses
- Set SM-DP+ address

Using this tool you can deliver eSIM profile on the removable eUICC (plastic eSIM)

# How to build
1. `git clone https://github.com/Truphone/LPAdesktop.git`
2. `docker-compose up`
3. Binaries will be in the `/target` folder

# How to use
1. Connect SmartCard reader 
2. Insert eUICC into the card reader
3. Open LPAdesktop
4. Insert RSP URL or scan QR via camera
5. eSIM profile will be delivired to connected eUICC

## ARM (M1) known issue
Plugin launch4j-maven-plugin is nor support ARM, to disable it comment section in pom.xml. 

As a result - no exe file will be generated.

```
<plugin>
                <groupId>com.akathist.maven.plugins.launch4j</groupId>
                <artifactId>launch4j-maven-plugin</artifactId>
                <version>2.1.1</version>
                <executions>
                    <execution>
                        <id>l4j-clui</id>
                        <phase>package</phase>
                        <goals>
                            <goal>launch4j</goal>
                        </goals>
                        <configuration>
                            <headerType>console</headerType>
                            <outfile>target/TruLPA_${project.version}/lpa.exe</outfile>
                            <jar>target/LPA-${project.version}-jar-with-dependencies.jar</jar>
                            <icon>${basedir}/src/main/resources/tru_logo.ico</icon>
                            <errTitle>LPA</errTitle>
                            <classPath>
                                <mainClass>com.truphone.lpap.mainUI</mainClass>
                                <addDependencies>true</addDependencies>
                                <preCp>anything</preCp>
                            </classPath>
                            
                            <jre>
                                <minVersion>1.8.0</minVersion> -->
<!--                                <opts>
                                    <opt>-Djava.endorsed.dirs=./endorsed</opt>
                                </opts>--> 
                                <!--
                            </jre>
                            <versionInfo>
                                <fileVersion>${project.version}</fileVersion>
                                <txtFileVersion>${project.version}</txtFileVersion>
                                <fileDescription>Truphone LPA Simulator</fileDescription>
                                <copyright>Truphone 2019</copyright>
                                <productVersion>${project.version}</productVersion>
                                <txtProductVersion>${project.version}</txtProductVersion>
                                <productName>Tru LPA</productName>
                                <internalName>LPA</internalName>
                                <originalFilename>lpa.exe</originalFilename>
                            </versionInfo>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```
