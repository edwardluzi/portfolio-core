package org.goldenroute.datafeed.pulling.sina;

import org.goldenroute.datafeed.pulling.SymbolConverter;

public final class SinaSymbolConverter extends SymbolConverter
{
    private static SinaSymbolConverter converter = null;

    public static synchronized SinaSymbolConverter getInstance()
    {
        if (converter == null)
        {
            converter = new SinaSymbolConverter();
        }

        return converter;
    }

    private SinaSymbolConverter()
    {
        super();
    }

    @Override
    public String toLocal(String symbol)
    {
        if (symbol.startsWith("0") || symbol.startsWith("3"))
        {
            return "sz" + symbol;
        }
        else
        {
            return "sh" + symbol;
        }
    }

    @Override
    public String fromLocal(String symbol)
    {
        return symbol.substring(2);
    }
}
