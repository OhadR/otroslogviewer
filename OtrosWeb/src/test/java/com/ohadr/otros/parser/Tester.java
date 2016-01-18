package com.ohadr.otros.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ohadr.otros.OtrosWebStatus;
import com.ohadr.otros.core.CacheHolder;
import com.ohadr.otros.core.MyLogReader;

import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporterUsingParser;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.reader.ProxyLogDataCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration				//this let Spring search for context file named Tester-context.xml
public class Tester {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Tester.class.getName());

	@Autowired
	private MyLogReader logReader;
	
	@Autowired
	private CacheHolder cacheHolder;
	
	//@Test
	public void testMyLogReader()
	{
        logReader.startTailingFile( "c:/MARS_pattern.log" );
	}
	
	
	//@Test
	public void test() throws IOException, InitializationException
	{
	    Properties p = new Properties();
	    p.put("type", "log4j");
	    p.put("pattern", "TIMESTAMP [THREAD] [LEVEL] [CLASS] - MESSAGE");
	    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
//	    p.put("customLevels", "L1=TRACE,L2=DEBUG,L3=INFO,L4=WARN,L5=ERROR");
	    ParsingContext parsingContext = new ParsingContext("?", "log4j/MARS_pattern.log");

	    // when
	    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();
	    InputStream in = loadLog("log4j/MARS_pattern.log");

	    
	    
	    Log4jPatternMultilineLogParser parser = new Log4jPatternMultilineLogParser();
		LogImporterUsingParser importer = new LogImporterUsingParser( parser );
		importer.init(p);
	    importer.initParsingContext(parsingContext);

	  //  BufferingLogDataCollectorProxy logDataCollector;
	    
	    importer.importLogs( in, dataCollector, parsingContext );
	}

	private InputStream loadLog(String resource) throws IOException 
	{
		return new ByteArrayInputStream(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(resource)));
	}
	
	
//	@Test
	/**
	 * I wanna simulate case of many clients (many browsers/tabs) try to get to files (or even the same file).
	 * the logReader opens a thread for each client, and at least in the first version it does not close any thread.
	 */
	public void testLogReaderManyReadersFakeFile()
	{
		for(int i = 0; i < 1000; ++i)
		{
			logReader.startTailingFile("g");

	    	OtrosWebStatus status = cacheHolder.getStatus();
	    	status.numThreads = logReader.getNumThreads();
	    	log.info( status.toString() );
		}
	}

	//@Test
	public void testLogReaderManyReadersRealFile()
	{
		for(int i = 0; i < 1000; ++i)
		{
//	        logReader.startTailingFile( "c:/MARS_pattern.log" );
	        logReader.startTailingFile( "C:/Ohad/Dev/Projects/otroslogviewer/OtrosWeb/marketingServices.log" );
	        

	    	OtrosWebStatus status = cacheHolder.getStatus();
	    	status.numThreads = logReader.getNumThreads();
	    	log.info( status.toString() );
		}
	}

	@Test
	public void testLogReader10ReadersBigFile() throws InterruptedException
	{
		int numClients = 3;
		Date start = new Date();
		for(int i = 0; i < numClients; ++i)
		{
//	        logReader.startTailingFile( "c:/MARS_pattern.log" );
	        logReader.startTailingFile( "C:/Ohad/Dev/Projects/otroslogviewer/OtrosWeb/marketingServices.log" );
	    	log.info( "sleeping 10 secs... good night." );
	    	Thread.sleep( 10000 );		//sleep 10 seconds
		}
		boolean work = true;
		while( work )
		{
	    	OtrosWebStatus status = cacheHolder.getStatus();
	    	status.numThreads = logReader.getNumThreads();
	    	log.info( status.toString() );
	    	Thread.sleep( 2000 );
	    	
	    	if(status.numElementsInCache == numClients * 667)
	    	{
	    		work = false;
	    	}
		}
		Date end = new Date();
		long diff = (end.getTime() - start.getTime())/1000;
		log.info("finished writing all data to cache, for " + numClients + " clients. took " + diff + "seconds.");
	}
}
