package com.ohadr.otros.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.ohadr.otros.OtrosWebStatus;

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
public class MyLogReader
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MyLogReader.class.getName());
	
	@Autowired
	private CacheHolder cacheHolder;

	private String logFilePath;
	
	//TODO use Spring's thread pool manager?
	private List<Long> threadsIds = new ArrayList<Long>();

    
/* TODO: handle threads. there should be 2 threads, look in TailLogAction...
 */
	/*
	String catalinaHome = System.getenv( "CATALINA_HOME" );
	logFilePath = catalinaHome + "/logs/MARS_pattern.log"
	 * 
	 */
	/**
	 * reads a file in tail mode, and put the parsed data in cache.
	 * @return
	 * @throws IOException
	 * @throws InitializationException
	 * @throws ConfigurationException
	 * @throws InterruptedException
	 */
	private LogData[] readLogs() throws IOException, InitializationException, ConfigurationException, InterruptedException
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
			removeCurrentThreadFromCollection();
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
			log.error("failed to open file " + resolveFile, e);
			removeCurrentThreadFromCollection();
			return null;
		}	
		
	    long threadId = Thread.currentThread().getId();

		//now we are tailing!!
	    
	    Log4jPatternMultilineLogParser parser = new Log4jPatternMultilineLogParser();
		LogImporterUsingParser importer = new LogImporterUsingParser( parser );
		importer.init(p);
	    importer.initParsingContext(parsingContext);

		while (parsingContext.isParsingInProgress() && threadsIds.contains( threadId ))
        {
//    		log.debug("while loop, thread: " + Thread.currentThread().getName() + "; thread-id=" + Thread.currentThread().getId() );
    		
            Utils.reloadFileObject( openFileObject );

            importer.importLogs( openFileObject.getContentInputStream(), dataCollector, parsingContext );
//        	importer.importLogs( in, dataCollector, parsingContext );
    	    LogData[] logData = dataCollector.getLogData();
    	    dataCollector.clear();
    	    
            if(logData.length > 0)
            {
        		log.debug("writing to cache, " + logData.length + " log-lines...");
				cacheHolder.writeLogDataToCache( threadId, logData );
            }

            Thread.sleep(1000);
        }
	    
		//we are here when the client closed the browser, so the thread-id was removed from the list of thread-ids:
		log.warn("thread id " + threadId + " stopped.");

		removeCurrentThreadFromCollection();
		LogData[] logData = dataCollector.getLogData();
	    return logData;
	}


	private void removeCurrentThreadFromCollection() 
	{
		Long threadId = Thread.currentThread().getId();
		removeThreadFromCollection( threadId );
	}

	private void removeThreadFromCollection(Long threadId) 
	{
        log.info("removeThreadFromCollection, for threadId: " + threadId);
		synchronized (threadsIds) 
		{
			threadsIds.remove( threadId );
		}
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
		    
		    
		Thread t = new Thread( r );
		t.setDaemon(true);
		synchronized (threadsIds) 
		{
			threadsIds.add( t.getId() );
		}
		t.start();
		return t.getId();
	}
	
	public int getNumThreads()
	{
		return threadsIds.size();
	}


	public void stopClientThread(long clientIdentifier) 
	{
		//remove the client-identifier (which is the thread-id) from the list, that will stop the thread:
		removeThreadFromCollection( clientIdentifier );	
		
		cacheHolder.removeClient( clientIdentifier );
	}


	public OtrosWebStatus getStatus() 
	{
    	OtrosWebStatus status = cacheHolder.getStatus();
    	status.numThreads = getNumThreads(); 
    	return status;
	}


	public LogData[] readLogDataFromCache(Long clientIdentifier) 
	{
		return cacheHolder.readLogDataFromCache( clientIdentifier );
	}
}
