To develop:

- Requires Java 1.5
- Should have no dependency on NetBeans 6.0
- Include source files in JAR (for distribution to customers)
- Make sure $Revision$ properties are set: svn propset svn:keywords "Revision" weather.txt
- Alter DatabaseConnection in testutil to have correct mysql settings for that machine
- Add Netbeans hibernate library to project

To build JARs:

- need ant-contrib.jar for this in lib dir of ant install
- run ant
