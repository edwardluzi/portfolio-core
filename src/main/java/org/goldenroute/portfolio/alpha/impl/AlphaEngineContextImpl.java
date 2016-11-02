package org.goldenroute.portfolio.alpha.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.goldenroute.DateTimeRange;
import org.goldenroute.model.Intervals;
import org.goldenroute.portfolio.alpha.AlphaEngineContext;
import org.goldenroute.portfolio.service.PortfolioService;
import org.goldenroute.util.CalendarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class AlphaEngineContextImpl implements AlphaEngineContext
{
    private static final Logger logger = Logger.getLogger(AlphaEngineContextImpl.class);

    @Autowired
    private PortfolioService portfolioService;

    @Value("${alpha.workingHours}")
    private String workingHoursString;

    private List<DateTimeRange> workingHours;
    private Set<Calendar> holidays;
    private Set<String> symbols;
    private Map<Intervals, Set<String>> symbolTable;

    @Override
    public List<DateTimeRange> getWorkingHours()
    {
        return workingHours;
    }

    @Override
    public Set<Calendar> getHolidays()
    {
        return holidays;
    }

    @Override
    public Set<String> getSymbols()
    {
        return symbols;
    }

    @Override
    public Map<Intervals, Set<String>> getSymbolTable()
    {
        return symbolTable;
    }

    @Override
    public void load()
    {
        workingHours = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String ranges[] = workingHoursString.split(";");

        for (String range : ranges)
        {
            String[] items = range.split("-");

            if (items.length != 2)
            {
                continue;
            }

            Calendar open = Calendar.getInstance();
            Calendar close = Calendar.getInstance();

            try
            {
                open.setTimeInMillis(dateFormat.parse(items[0].trim()).getTime());
                close.setTimeInMillis(dateFormat.parse(items[1].trim()).getTime());
            }
            catch (ParseException e)
            {
                logger.error(e);
                throw new RuntimeException(e);
            }

            CalendarUtil.normalize(open, Calendar.MINUTE);
            CalendarUtil.normalize(close, Calendar.MINUTE);

            workingHours.add(new DateTimeRange(open, close));
        }

        symbols = new HashSet<String>(portfolioService.findDistinctSymbols());
        symbolTable = new HashMap<>();
        symbolTable.put(Intervals.Minute5, symbols);
        symbolTable.put(Intervals.Daily, new HashSet<String>(symbols));
    }
}
