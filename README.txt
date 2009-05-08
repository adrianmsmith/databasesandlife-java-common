To develop:

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
