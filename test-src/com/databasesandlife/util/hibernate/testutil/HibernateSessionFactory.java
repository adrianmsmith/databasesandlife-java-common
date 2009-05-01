package com.databasesandlife.util.hibernate.testutil;

import com.databasesandlife.testutil.DatabaseConnection;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {
    
    protected static Configuration configuration = null;
    protected static SessionFactory sessionFactory = null;

    protected synchronized static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.dialect",                 "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.show_sql",                "true");
            configuration.setProperty("hibernate.connection.url",          DatabaseConnection.getJdbcUrl());
            configuration.setProperty("hibernate.connection.username",     DatabaseConnection.getUsername());
            configuration.setProperty("hibernate.connection.password",     DatabaseConnection.getPassword());
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
