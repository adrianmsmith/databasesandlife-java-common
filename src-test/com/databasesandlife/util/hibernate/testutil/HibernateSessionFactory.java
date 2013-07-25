package com.databasesandlife.util.hibernate.testutil;

import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class HibernateSessionFactory {
    
    protected static Configuration configuration = null;
    protected static SessionFactory sessionFactory = null;

    protected synchronized static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.dialect",                 "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.cache.provider_class",    "org.hibernate.cache.NoCacheProvider");
            configuration.setProperty("hibernate.show_sql",                "true");
            configuration.setProperty("hibernate.connection.url",          "jdbc:mysql://localhost/databasesandlife_common");
            configuration.setProperty("hibernate.connection.username",     "root");
            configuration.setProperty("hibernate.connection.password",     "root");
            configuration.addClass(PersistentObject.class);
        }
        return configuration;
    }

    public synchronized static SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            sessionFactory = getConfiguration().buildSessionFactory();

        return sessionFactory;
    }
}
