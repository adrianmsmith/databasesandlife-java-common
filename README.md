This is a repository full of utility classes developed at Adrian Smith Software (A.S.S.)

The master repository is Subversion but it's also on github, with a two-way bridge. Feel free to download and use, feel free to make contributions via pull requests.

Includes the following classes:

* [DbTransaction](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/jdbc/DbTransaction.html), connects to MySQL, PostgreSQL and MS SQL Server databases. Basically a wrapper around the JDBC connection but provides utility methods such as query which returns an Iterable of rows. Has the method jooq() to return jOOQ context.
* [EmailTemplate](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/EmailTemplate.html). You check in a directory full of files like body.velocity.utf8.html and pass it variables and it expands and sends the email. Images in the same directory are sucked in with content-type: multipart/related, HTML and Text alternative parts are supported, and so on.
* [EmailTransaction](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/EmailTransaction.html). Bascially just a list of emails which will be sent on "commit". Allows one to open not only a transaction with the database and other transactional services like Lucene, but also when sending email.
* [OutOfHeapTemporaryStorage](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/OutOfHeapTemporaryStorage.html). A temporary file which can store arbitrary data for the duration of a process.
* [AutoStopThreadPool](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/AutoStopThreadPool.html). A pool of threads which execute tasks, and which will shut down after all tasks have been completed.
* [Timer](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/Timer.html), can be used with try(...) syntax in Java, to time the operation within.
* Many utility classes for the Apache Wicket web framework. Including: [AskUserForLatitudeLongitudePanel](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/AskUserForLatitudeLongitudePanel.html), [CountingUpThenAutoRefreshingLabel](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/CountingUpThenAutoRefreshingLabel.html), [DateTextField](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/DateTextField.html) and [TimeTextField](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/TimeTextField.html) (<input type=date> etc.), [MaxWordCountValidator](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/MaxWordCountValidator.html), [OverlayIframeCloser](https://www.databasesandlife.com/blog-attachments/databasesandlife-util-javadoc/com/databasesandlife/util/wicket/OverlayIframeCloser.html) for working with Fancybox.

To develop, take the following steps:

- Download Java (see build.xml for which version)
- Download Eclipse or IntelliJ
- Download VitualBox, Vagrant and, if on Windows, cygwin
- Checkout this to "databasesandlife-java-common"
- In Eclipse, "import" the project (all the ".project" files etc. are checked in); in IntelliJ there is presumably something similar.
- Within the directory, "vagrant up" and follow the on-screen instructions. Vagrant is used for running unit tests, and for creating the javadoc.

