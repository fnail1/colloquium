package ru.mail.colloquium.toolkit.collections;

public interface Aggregator<Param, Result>{
    Result invoke(Param p, Result prev);
}

