define stream TimeseriesEventStream (symbol string, intervals int, timestamp long, open double, high double, low double, close double, volume double);
define stream QuoteEventStream (symbol string, timestamp long, price double, volume double);

@From(eventtable = 'rdbms', datasource.name = 'portfolio', table.name = 'timeseries')
define table TimeseriesEventTable (symbol string, intervals int, timestamp long, open double, high double, low double, close double, volume double);

@From(eventtable = 'rdbms', datasource.name = 'portfolio', table.name = 'quote')
define table QuoteEventTable (symbol string, timestamp long, price double, volume double);

define stream AlertEventStream (subject string, params string);

from TimeseriesEventStream [ str:substr(symbol, 0, 4) == '#EVT' ]
select symbol as subject, convert(intervals, 'string') as params
insert into AlertEventStream;

from QuoteEventStream [ str:substr(symbol, 0, 4) == '#EVT' ]
select symbol as subject, convert(timestamp, 'string') as params
insert into AlertEventStream;