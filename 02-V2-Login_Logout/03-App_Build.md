# Build and Deploy the Application

## Install the Build Tool
1. Install Java

```
sudo dnf install -y java-1.8.0-openjdk-devel
java -version
```
2. Install GIT
```
sudo dnf install git -y
```

4. Install Maven

```
sudo dnf install -y maven
mvn -version
```
5. Clone the Repository
```
git clone <Repo-URL>
```
## Build the Application

From the project directory.
```
mvn clean package
```
Generated artifact ==> digistack-bank-ear/target/digistack-bank-v2.ear

# Confirm the EAR contains the correct WAR

Compile SeedUsers with both the compiled classes and the JDBC JAR on the classpath.
```
javac -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
  digistack-bank-web/src/main/java/com/digistack/bank/util/SeedUsers.java \
  digistack-bank-web/src/main/java/com/digistack/bank/util/PasswordUtil.java \
  -d digistack-bank-web/target/classes
```
Expected result — no output means success. If you see error messages, share them.

Run this command to list the EAR contents:
```
jar tf digistack-bank-ear\target\digistack-bank-v2.ear
```
jar tf: The jar tool is bundled with your Java installation. tf means "table of contents", it lists every file inside the archive without extracting it. EARs and WARs are ZIP files with different extensions.


Expected result — you should see entries including:
```
META-INF/
META-INF/MANIFEST.MF
META-INF/application.xml
digistack-bank-web-1.0.war
```
The application.xml is the EAR deployment descriptor — Maven's EAR plugin generated it automatically from your pom.xml configuration. The WAR file inside confirms the EAR is correctly assembled.
