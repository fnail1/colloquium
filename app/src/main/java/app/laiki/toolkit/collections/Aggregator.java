package app.laiki.toolkit.collections;

public interface Aggregator<Param, Result>{
    Result invoke(Param p, Result prev);
}

