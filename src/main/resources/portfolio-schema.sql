CREATE TABLE IF NOT EXISTS `timeseries` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `symbol` varchar(32) DEFAULT NULL,
  `intervals` int(11) DEFAULT NULL,
   `timestamp` bigint(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `volume` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_timeseries_symbol_intervals_timestamp` (`symbol`,`intervals`,`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `quote` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `symbol` varchar(32) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `volume` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quote_symbol` (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
