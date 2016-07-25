package com.databasesandlife.util.hibernate.testutil;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class PersistentObject {
    protected Long id = null;
    protected String key1, key2;
    protected String data = "x";

    public PersistentObject() { }

    public PersistentObject(String k1, String k2) { key1=k1; key2=k2; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
