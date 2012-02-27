To develop / Eclipse

- Download Java 1.5 or higher
- Download Eclipse
- Preferences in Eclipse
  - Text Editors
    - Print margin column 132
    - Insert spaces for tab
  - Java -> Code Style -> Formatter
    - New Profile -> Adrian
    - Set "tab policy" to spaces
  - Web -> HTML Files -> Editor -> Indent using spaces
  - Refresh workspace on startup
  - Runtime environment -> Add -> Tomcat 6
- Create new project
- Link "src"
- Delete old "src"
- Link "src-junit"
- Build path -> Add library -> Junit 3
- Add all external JARs, in both "lib-compile" and "lib-test"
- Create a MySQL database locally, "databasesandlife_common"
- Alter database connections in DatabaseConnection test class to a local MySQL
- Run "src-junit" 'as JUNIT' (try "debug as" in case JUNIT option is missing)

To develop / Netbeans

- Download Java 1.5 or higher
- Download Netbeans 6.0 or higher
- Create new project Java -> With existing sources
- Select "src" and "test-src" directories
- Add library (not "jar"): Hibernate, MySQL driver
- Create a MySQL database locally, "databasesandlife_common"
- If DB is not localhost, or username/password not root, then use -Ddb.name etc options
- Alter database connections in DatabaseConnection test class to a local MySQL
- Make sure svn propset svn:keywords "Revision" weather.txt

To build JARs:

- Make sure source is checked in to SVN (for -rXXXX filename)
- need ant-contrib.jar for this in lib dir of ant install
- run ant
