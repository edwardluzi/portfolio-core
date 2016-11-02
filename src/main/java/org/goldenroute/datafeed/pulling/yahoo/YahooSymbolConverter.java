package org.goldenroute.datafeed.pulling.yahoo;

import org.goldenroute.datafeed.pulling.SymbolConverter;

public final class YahooSymbolConverter extends SymbolConverter
{
    private static YahooSymbolConverter converter = null;

    public static synchronized YahooSymbolConverter getInstance()
    {
        if (converter == null)
        {
            converter = new YahooSymbolConverter();
        }

        return converter;
    }

    private YahooSymbolConverter()
    {
        super();
    }

    @Override
    public String toLocal(String symbol)
    {
        if (getToLocalMap().containsKey(symbol))
        {
            return getToLocalMap().get(symbol);
        }
        else if (symbol.startsWith("0") || symbol.startsWith("3"))
        {
            return symbol + ".SZ";
        }
        else
        {
            return symbol + ".SS";
        }
    }

    @Override
    public String fromLocal(String symbol)
    {
        if (getFromLocalMap().containsKey(symbol))
        {
            return getFromLocalMap().get(symbol);
        }
        else
        {
            return symbol.substring(0, symbol.length() - 3);
        }
    }
}
