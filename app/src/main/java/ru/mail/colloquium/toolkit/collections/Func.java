package ru.mail.colloquium.toolkit.collections;

public interface Func<Param, Result>{
    Result invoke(Param p);
}

