package com.ohadr.otros.core;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import pl.otros.logview.LogData;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporterUsingParser;
import pl.otros.logview.io.LoadingInfo;
import pl.otros.logview.io.Utils;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.reader.ProxyLogDataCollector;


@Component
@EnableAsync			//to allow @Scheduled
@EnableScheduling		//to allow @Scheduled
public class MyLogReader implements InitializingBean 
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MyLogReader.class.getName());
	
	@Autowired
	private CacheHolder cacheHolder;

	private String logFilePath;

    
/* TODO: handle threads. there should be 2 threads, look in TailLogAction...
 */
	/*
	String catalinaHome = System.getenv( "CATALINA_HOME" );
	logFilePath = catalinaHome + "/logs/MARS_pattern.log"
	 * 
	 */
	public LogData[] readLogs() throws IOException, InitializationException, ConfigurationException, InterruptedException
	{

	    Properties p = new Properties();
	    p.put("type", "log4j");
	    p.put("pattern", "TIMESTAMP [THREAD] [LEVEL] [CLASS] - MESSAGE");
	    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
//	    p.put("customLevels", "L1=TRACE,L2=DEBUG,L3=INFO,L4=WARN,L5=ERROR");
	    ParsingContext parsingContext = new ParsingContext("?", logFilePath);

	    // when
	    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();


		//this code is from @BathcProcessor:
	    FileObject resolveFile = null;
		try 
		{
			FileSystemManager manager = VFS.getManager();
			resolveFile = manager.resolveFile( logFilePath );
		} 
		catch (FileSystemException e1) 
		{
			log.error("resolving file " + logFilePath + " error: " + e1);
			return null;
		}

		LoadingInfo openFileObject = null;
		try 
	    {
			log.debug("trying to open file " + resolveFile + "...");
			openFileObject = Utils.openFileObject(resolveFile, true);	//true is for tail mode.
		}
	    catch (Exception e) 
	    {
			// TODO Auto-generated catch block
			log.error("failed to open file " + resolveFile, e);
			return null;
		}	
		
		//now we are tailing!!
	    
	    Log4jPatternMultilineLogParser parser = new Log4jPatternMultilineLogParser();
		LogImporterUsingParser importer = new LogImporterUsingParser( parser );
		importer.init(p);
	    importer.initParsingContext(parsingContext);

		while (parsingContext.isParsingInProgress())
        {
//    		log.debug("while loop, thread: " + Thread.currentThread().getName() + "; thread-id=" + Thread.currentThread().getId() );
    		
        	importer.importLogs( openFileObject.getContentInputStream(), dataCollector, parsingContext );
//        	importer.importLogs( in, dataCollector, parsingContext );
    	    LogData[] logData = dataCollector.getLogData();
    	    dataCollector.clear();
    	    
            if(logData.length > 0)
            {
        		log.debug("writing to cache, " + logData.length + " log-lines...");
        	    cacheHolder.writeLogDataToCache( Thread.currentThread().getId(), logData );
            }

    	    /*
    	    for(int i = 0; i < logData.length; ++i)
    	    {
        	    System.out.println("@@@@ " + logData[i]);    	    	
    	    }
    	    */

            Thread.sleep(1000);

            Utils.reloadFileObject( openFileObject );
            
        }
	    
		log.error("AM I REALLY SUPPOSE TO BE HERE??");

		LogData[] logData = dataCollector.getLogData();
	    return logData;
	}

	@Override
	public void afterPropertiesSet() throws Exception 
	{
	}

	/*
	private InputStream loadLog(String resource) throws IOException 
	{
		InputStream rscAsStream = this.getClass().getClassLoader().getResourceAsStream(resource);
		if( rscAsStream == null )
			throw new IOException("ERROR - log resource is null; " + resource);
		
		return new ByteArrayInputStream(IOUtils.toByteArray( rscAsStream ));
	}
	*/	
	
	/**
	 * this method sets the log file path that is to be tailed, and starts tailing it in a different thread.
	 * @param logFilePath the file to be tailed.
	 * @return the thread id that is tailing the file.
	 */
	public long startTailingFile(String logFilePath) 
	{
		log.info("log file is set to: " + logFilePath);
		this.logFilePath = logFilePath;

		Runnable r = () -> {
				log.info("*** timer started*** ");
		    	try 
		    	{
					readLogs();
				}
		    	catch (ConfigurationException | IOException | InitializationException | InterruptedException e) 
		    	{
					e.printStackTrace();
					log.error("error on ReadLogs(): " + e);
				}        	
		};
		    
		    
		Thread t = new Thread(r, "Log reader-" + logFilePath);
		t.setDaemon(true);
		t.start();
		return t.getId();
	}
}
