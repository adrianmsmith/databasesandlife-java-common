package com.databasesandlife.util;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public enum OrderByDirection {
    asc     { public OrderByDirection getReverseDirection() { return desc; } },
    desc    { public OrderByDirection getReverseDirection() { return asc; } };

    public abstract OrderByDirection getReverseDirection();
}