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
Generated artifact ==> digistack-bank-ear/target/digistack-bank-v4.ear


# Confirm the EAR contains the correct WAR

Run this command to list the EAR contents:
```
jar tf digistack-bank-ear\target\digistack-bank-v4.ear
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

The WAR is inside the EAR. List the WAR contents directly:
```
jar tf digistack-bank-web\target\digistack-bank-web-1.0.war
```
Expected result — you should see all compiled class files including:
```
WEB-INF/classes/com/digistack/bank/servlet/HomeServlet.class
WEB-INF/classes/com/digistack/bank/servlet/LoginServlet.class
WEB-INF/classes/com/digistack/bank/servlet/DashboardServlet.class
WEB-INF/classes/com/digistack/bank/servlet/LogoutServlet.class
WEB-INF/classes/com/digistack/bank/servlet/AccountServlet.class
WEB-INF/classes/com/digistack/bank/servlet/BalanceJsonServlet.class
WEB-INF/classes/com/digistack/bank/util/PasswordUtil.class
WEB-INF/classes/com/digistack/bank/util/SeedUsers.class
WEB-INF/classes/com/digistack/bank/model/Account.class
WEB-INF/classes/com/digistack/bank/dao/AccountDao.class
WEB-INF/classes/com/digistack/bank/service/AccountService.class
WEB-INF/classes/com/digistack/bank/exception/InsufficientFundsException.class
WEB-INF/web.xml
```
