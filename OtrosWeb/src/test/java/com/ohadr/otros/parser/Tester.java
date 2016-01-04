package com.ohadr.otros.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ohadr.otros.core.MyLogReader;

import pl.otros.logview.LogData;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporterUsingParser;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.reader.ProxyLogDataCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration				//this let Spring search for context file named Tester-context.xml
public class Tester {
	
	@Autowired
	MyLogReader logReader;
	
	@Test
	public void testMyLogReader()
	{
        logReader.setLogFile( "c:/MARS_pattern.log" );
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

}
