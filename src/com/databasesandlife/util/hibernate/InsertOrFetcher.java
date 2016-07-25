package com.databasesandlife.util.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

// Javadoc deliberately doesn't have * at the start to make it easier to copy/paste to an HTML editor
/**
<p>Reads an instance from a database, creating it just in time if it doesn't exist yet.</p>

<h3>Background</h3>

<p>It is sometimes desirable to create rows "just in time" in a database.</p>

<p>For example, to maintain a count of the number of SMS sent per day, a database table with columns (day, count) might be
created. Each time an SMS is sent, a row should be created for that day if none exists yet, and the count in the previously
existing row should be incremented otherwise.</p>

<p>Hibernate provides no facility for managing such "just in time" creation. (The <i>saveOrUpdate</i> method looks like it might
help, but assumes the application knows whether the row already exists or not. In reality, in a concurrent environment, as soon
as the application has determined whether the row exists or not, the information is already stale, as some other session might
have changed the data.) </p>

<h3>Usage</h3>

<pre>
Session s = .... ; // Hibernate Session

DailyLog newObj = new DailyLog();
newObj.setDay(getTodaysDate());
newObj.setCount(0);

Collection&lt;String&gt; uniqueIdentifier = Arrays.asList("day");

DailyLog todaysLog = <b>InsertOrFetcher.loadAndLock</b>(
   DailyLog.class, s, newObj, uniqueIdentifier);
todaysLog.incrementCount();
</pre>

<p>If the row didn't exist in the database, then <i>newObj</i> is inserted and returned. If the row existed then it is
fetched and returned.</p>

<p>In addition, the returned object is:</p>
<ul>
        <li>never null,</li>
        <li>always already exists in the database by the time this call ends</li>
        <li>associated with the passed Hibernate Session (i.e. is "persistent" in Hibernate Session terminology)</li>
</ul>
<p>The database table backing <i>DailyLog</i> must have not only a primary key constraint defined, but in addition <b>another
constraint</b> which will make sure that only one row per <i>day</i> can be created. The decision about if the object already
exists or if it needs to be inserted is taken by attempting an insert and seeing if a constraint violation error occurs (see
"strategy" later.)</p>

<p>The method <i>loadAndLock</i> locks the object when returning it (with "select for update"), intended for read/write access;
the method <i>load</i> loads the object without locking it, intended for read-only access.</p>

<h3>Strategy</h3>

<p>As mentioned earlier, to "just in time" insert an object it is no good doing a "select" to see if the object exists and
inserting it if it doesn't. Between the time the select is done and the time the insert is done, another session might have done
an insert.
The only way to proceed is to perform the "insert", and if that succeeds then one can be certain that the row now exists, and
if that fails with a constraint violation then one can be certain that the row already existed.</p>

<p>However, although this is the only strategy that can be adopted, it is not easy to implement in Hibernate. Hibernate states
(in the Session Javadoc) that if a statement fails, then the Session must be discarded.
Therefore the strategy which is adopted is to create a new Session with its own Transaction, perform the insert. Afterwards
one can be certain that the row exists in the database, so the Session is destroyed, and the row is loaded in the original
Session and returned.</p>

<p>This may have performance penalties, however it is the only way to ensure correct behavior.</p>

 * See <a href="http://www.databasesandlife.com/unique-constraints">Programming with unique constraints</a>.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class InsertOrFetcher {

    protected static <T> T load(Class<T> cl, Session mainSession, T objectForInsertion, Collection<String> domainKey, LockMode lk) {
        try {
            // Insert object & catch exception if fail
            Session newSession = mainSession.getSessionFactory().openSession();
            try {
                Transaction tx = newSession.beginTransaction();
                try {
                    newSession.save(objectForInsertion);
                    tx.commit();
                }
                finally { if (tx.isActive()) tx.rollback(); }
            }
            catch (ConstraintViolationException e) { }    // Object already exists, continue to "fetch" below
            finally { newSession.close(); }

            // Create fetch parameters
            Criteria select = mainSession.createCriteria(cl);
            select.setLockMode(lk);
            for (String attr : domainKey) {
                try {
                    String methodName = "get" + attr.substring(0, 1).toUpperCase() + attr.substring(1);
                    Method method = cl.getMethod(methodName);
                    Object value = method.invoke(objectForInsertion);
                    select.add(Restrictions.eq(attr, value));
                }
                catch (NoSuchMethodException e) { throw new RuntimeException(
                    "Class '"+cl+"' has no public getter for property '"+attr+"'"); }
            }

            // Fetch & return object (it must exist if insert failed)
            T result = cl.cast(select.uniqueResult());
            if (result == null) throw new RuntimeException("INSERT was successful or caused constraint exception, " +
                "but SELECT didn't find object -- possibly unique constraint wrongly defined?");

            return result;
        }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
        catch (InvocationTargetException e) { throw new RuntimeException(e); }
    }

    /**
     * See class documentation.
     * @param cl                 The type of Hibernate-managed to be inserted/fetched
     * @param mainSession        Hibernate session with active transaction
     * @param objectForInsertion Not managed by Hibernate yet
     * @param domainKey          Which attributes of objectForInsertion should be used for the WHERE to re-find the object
     * @return                   See class documentation
     */
    public static <T> T load(Class<T> cl, Session mainSession, T objectForInsertion, Collection<String> domainKey) {
        return load(cl, mainSession, objectForInsertion, domainKey, LockMode.READ);
    }

    /**
     * See class documentation.
     * @param cl                 The type of Hibernate-managed to be inserted/fetched
     * @param mainSession        Hibernate session with active transaction
     * @param objectForInsertion Not managed by Hibernate yet
     * @param domainKey          Which attributes of objectForInsertion should be used for the WHERE to re-find the object
     * @return                   See class documentation
     */
    public static <T> T loadAndLock(Class<T> cl, Session mainSession, T objectForInsertion, Collection<String> domainKey) {
        return load(cl, mainSession, objectForInsertion, domainKey, LockMode.UPGRADE);
    }
}
