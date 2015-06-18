package com.databasesandlife.util;

public enum OrderByDirection {
    asc     { public OrderByDirection getReverseDirection() { return desc; } },
    desc    { public OrderByDirection getReverseDirection() { return asc; } };

    public abstract OrderByDirection getReverseDirection();
}