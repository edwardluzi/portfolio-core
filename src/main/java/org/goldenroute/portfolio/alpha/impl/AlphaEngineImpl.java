package org.goldenroute.portfolio.alpha.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.goldenroute.Constants;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.model.Intervals;
import org.goldenroute.portfolio.alpha.AlphaEngine;
import org.goldenroute.portfolio.alpha.AlphaEngineContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.wso2.siddhi.core.SiddhiManager;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;

public class AlphaEngineImpl implements AlphaEngine, DisposableBean
{
    private static final Logger logger = Logger.getLogger(AlphaEngineImpl.class);
    private static final String CLASS_PATH = "java.class.path";

    @Autowired
    private TimeseriesFeed timeseriesFeed;

    @Autowired
    private QuoteFeed quoteFeed;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private AlphaEngineContext engineContext;

    @Autowired
    @Qualifier("wechatNotifier")
    private Consumer<Event<String>> wechatNotifier;

    @Value("${alpha.siddhi.datasource.name}")
    private String dataSourceName;

    private SiddhiManager siddhiManager;
    private AlphaExecutionPlanRuntime executionPlanRuntime;

    @Override
    public void start()
    {
        modifyClassPath();

        engineContext.load();

        setupEventConsumer();
        setupEngine();
        setupDateFeed();

        executionPlanRuntime.start();

        timeseriesFeed.start();
        quoteFeed.start();
    }

    @Override
    public void stop()
    {
        if (executionPlanRuntime != null)
        {
            executionPlanRuntime.dispose();
        }

        if (siddhiManager != null)
        {
            siddhiManager.shutdown();
        }
    }

    @Override
    public void destroy() throws Exception
    {
        stop();
    }

    private void setupEventConsumer()
    {
        eventBus.on(Selectors.object(Constants.EVENT_GROUP_ALERT), wechatNotifier);
    }

    private void setupEngine()
    {
        siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(dataSourceName, dataSource);
        executionPlanRuntime = new AlphaExecutionPlanRuntime(siddhiManager, eventBus, timeseriesFeed, quoteFeed);
    }

    private void setupDateFeed()
    {
        timeseriesFeed.setWorkingHours(engineContext.getWorkingHours());
        quoteFeed.setWorkingHours(engineContext.getWorkingHours());

        for (Map.Entry<Intervals, Set<String>> entry : engineContext.getSymbolTable().entrySet())
        {
            timeseriesFeed.subscribe(entry.getKey(), entry.getValue());
        }

        quoteFeed.subscribe(engineContext.getSymbols());
    }

    private void modifyClassPath()
    {
        ClassLoader applicationClassLoader = getClass().getClassLoader();

        if (applicationClassLoader == null)
        {
            applicationClassLoader = ClassLoader.getSystemClassLoader();
        }

        Set<String> loadedClassPathes = new HashSet<>();

        URL[] urls = ((URLClassLoader) applicationClassLoader).getURLs();

        for (int i = 0; i < urls.length; i++)
        {
            loadedClassPathes.add(new File(urls[i].getFile()).getParent());
        }

        String workingFolder = "";

        try
        {
            workingFolder = new java.io.File(".").getCanonicalPath();
        }
        catch (IOException e)
        {
            logger.error(e);
        }

        logger.debug("loadedClassPathes=" + String.join(File.pathSeparator, loadedClassPathes.toArray(new String[0])));

        loadedClassPathes.remove(workingFolder);

        String systemClassPathes = System.getProperty(CLASS_PATH);

        Set<String> classPathElements = new HashSet<>(Arrays.asList(systemClassPathes.split(File.pathSeparator)));

        for (String path : loadedClassPathes)
        {
            if (!classPathElements.contains(path))
            {
                systemClassPathes += File.pathSeparator + path;
            }
        }

        System.setProperty(CLASS_PATH, systemClassPathes);
    }
}
