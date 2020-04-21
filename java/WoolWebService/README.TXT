================================================================================
WOOL WEB SERVICE 1.0.0
20 April 2020
Dennis Hofs (d.hofs@rrd.nl)

================================================================================
1 REQUIREMENTS

Instructions to install the required software are included for Windows.

--------------------------------------------------------------------------------
1.1 JAVA

The web service was tested with OpenJDK 11.

https://jdk.java.net/archive/

Download the ZIP file and extract it to a location on your hard disk. For
example:
  C:\apps\jdk-11
Then set environment variable JAVA_HOME. For example:
  C:\apps\jdk-11
And add the bin directory to your path. For example:
  C:\apps\jdk-11\bin

You should now be able to run Java from the command prompt. For example:
> java -version

--------------------------------------------------------------------------------
1.2 TOMCAT

The web service was tested with Tomcat 8.5.

http://tomcat.apache.org/download-80.cgi

Download the 32-bit/64-bit Windows Service Installer of Tomcat 8.5.
Run the installer and check the following pages:
Choose Components:
- You may want to add Tomcat > Service Startup so Tomcat is started
  automatically when the computer is started.
- Add "Host Manager". This is needed to deploy web applications from the Gradle
  build file.
Configuration:
- At Tomcat Administrator Login fill in a user name (for example "admin") and
  password.
- Set "Roles" to: admin-gui,manager-gui,admin-script,manager-script
  The script roles are also needed for deployment from the build file.
Java Virtual Machine:
- Select the directory to the JDK. For example:
  C:\apps\jdk-11

Configuration of the JDK location, Java options and memory size:
- From the Windows start menu, open Monitor Tomcat. If you get a permission
  error, you may need to open the Tomcat folder in Windows Explorer first:
  C:\Program Files\Apache Software Foundation\Tomcat 8.5
  The Tomcat monitor opens in the notification area of the task bar.
- Open the Tomcat monitor from the task bar.
- Open the tab Java.

You should now be able to open the Tomcat manager at:
http://localhost:8080/


================================================================================
2 CONFIGURATION

The web service is configured with this file that needs to be created:
<GITDIR>\wool\java\WoolWebService\gradle.properties

You can make a copy of this file and change it:
<GITDIR>\wool\java\WoolWebService\gradle.sample.properties

For the secret key, you may use the key generator in the BuildTools. Open a
command prompt in this directory:
<GITDIR>\wool\java\BuildTools\BuildTools-1.0.0
Enter this command:
> .\keygenerator -t base64 -s 1024

The passwords are loaded from an XML file that should be in the configured data
dir:
<DATADIR>\users.xml
An example file can be found in:
<GITDIR>\wool\java\WoolWebService\config\users.xml


================================================================================
3 DEPLOY

Make sure that Tomcat is running.

The web service is deployed with Gradle task cargoRedeployRemote. It requires
the following properties that you need to fill in gradle.properties:
- tomcatDeployPath
- remoteTomcat*

Open a command prompt in:
  <GITDIR>\wool\java\WoolWebService
Enter a command like this:
> .\gradlew build cargoRedeployRemote
using the deploy task(s) that you want to run.

If you want to make a clean build and deploy, then enter:
> .\gradlew clean build cargoRedeployRemote

Logging is done using Logback (http://logback.qos.ch/). This is configured in:
  <GITDIR>\wool\java\WoolWebService\src\main\resources\logback.xml
With this configuration, log files are written to the directory that you set
in woolconfigDataDir in gradle.properties.

After deploying you can access the Swagger interface at:
http://localhost:8080/servlets/wool/


================================================================================
4 USAGE

First get an auth token with POST /auth/login. The request body should be a JSON
object like this:
{
  "user": "user@example.com",
  "password": "p4ssw0rd",
  "tokenExpiration": 1440
}
The "user" is case-insensitive.
The "tokenExpiration" is optional. It can be a value in minutes or "never".
The default is 1440 minutes (24 hours).

The response is a JSON object like this:
{
  "user": "user@example.com",
  "token": "eyJhbGciOiJI..."
}
The "user" in the response may have different case than the case-insensitive
"user" in the request.

In subsequent queries you must include the token in header X-Auth-Token.
