package com.databasesandlife.util.hibernate;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @see <a href="http://www.databasesandlife.com/unique-constraints">Programming with unique constraints</a>
 * @author Adrian Smith &lt;adrian.m.smith@gmail.com>
 * @version $Revision$
 */
public class InsertOrFetcher {

    protected static Map<String, PropertyDescriptor> getProperties(Class<?> cl) {
        try {
            Map<String, PropertyDescriptor> result = new HashMap<String, PropertyDescriptor>();
            for (PropertyDescriptor p : Introspector.getBeanInfo(cl).getPropertyDescriptors())
                result.put(p.getName(), p);
            return result;
        }
        catch (java.beans.IntrospectionException e) { throw new RuntimeException(e); }
    }

    /**
     * See class documentation.
     * @param cl        The type of Hibernate-managed to be inserted/fetched
     * @param s         Hibernate session with active transaction
     * @param domainKey The identification of this object, for example the "day" parameter in the DailyLog example above
     * @return          See class documentation
     */
    public static <T> T load(Class<T> cl, Session s, Map<String, Object> domainKey) {
        try {
            Map<String, PropertyDescriptor> properties = getProperties(cl);

            // Create new object for insertion
            T newObject = cl.newInstance();
            for (Entry<String, Object> e : domainKey.entrySet()) {
                if ( ! properties.containsKey(e.getKey()))
                    throw new RuntimeException("Class '"+cl+"' appears not to have a setter for property '"+e.getKey()+"'");
                properties.get(e.getKey()).getWriteMethod().invoke(newObject, e.getValue());
            }

            // Insert object & catch exception if fail
            Session newSession = s.getSessionFactory().openSession();
            try {
                Transaction tx = newSession.beginTransaction();
                newSession.save(newObject);
                tx.commit();
            }
            catch (ConstraintViolationException e) { }    // Object already exists, continue to "fetch" below
            finally { newSession.close(); }    // Leave tx handing around: tx.commit -> error, tx.rollback -> error

            // Create fetch parameters
            Criteria select = s.createCriteria(cl);
            select.setLockMode(LockMode.UPGRADE);
            for (Entry<String, Object> e : domainKey.entrySet())
                select.add(Restrictions.eq(e.getKey(), e.getValue()));

            // Fetch & return object (it must exist if insert failed)
            T result = (T) select.uniqueResult();
            if (result == null) throw new RuntimeException("INSERT was successful or caused constraint exception, " +
                    "but SELECT didn't find object -- possibly unique constraint wrongly defined?");

            return result;
        }
        catch (InstantiationException e) { throw new RuntimeException(e); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
        catch (InvocationTargetException e) { throw new RuntimeException(e); }
    }
}
