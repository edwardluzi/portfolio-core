package org.goldenroute.datafeed.pulling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class SymbolConverter
{
    private Map<String, String> toLocalMap = null;
    private Map<String, String> fromLocalMap = null;

    public SymbolConverter()
    {
        toLocalMap = new HashMap<>();
        fromLocalMap = new HashMap<>();
    }

    public abstract String toLocal(String symbol);

    public Collection<String> toLocal(Collection<String> symbols)
    {
        Collection<String> locals = new HashSet<String>();

        for (String symbol : symbols)
        {
            locals.add(toLocal(symbol));
        }

        return locals;
    }

    public abstract String fromLocal(String symbol);

    public Collection<String> fromLocal(Collection<String> locals)
    {
        Collection<String> symbols = new HashSet<String>();

        for (String local : locals)
        {
            symbols.add(fromLocal(local));
        }

        return symbols;
    }

    protected Map<String, String> getToLocalMap()
    {
        return toLocalMap;
    }

    protected Map<String, String> getFromLocalMap()
    {
        return fromLocalMap;
    }

    protected void add(String symbol, String local)
    {
        toLocalMap.put(symbol, local);
        fromLocalMap.put(local, symbol);
    }
}
