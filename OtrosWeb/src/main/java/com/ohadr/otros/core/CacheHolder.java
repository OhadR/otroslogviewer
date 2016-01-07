package com.ohadr.otros.core;

import java.io.Serializable;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ohadr.otros.OtrosConstants;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import pl.otros.logview.LogData;

@Component
public class CacheHolder implements InitializingBean
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CacheHolder.class.getName());

	private Ehcache cache;

	@Override
	public void afterPropertiesSet() throws Exception 
	{
        // Create a cache manager
        final CacheManager cacheManager = CacheManager.getInstance();

        cache = cacheManager.addCacheIfAbsent( OtrosConstants.CACHE_NAME );
	}

	/**
	 * 
	 * @param sessionIdentifier - the identifier of the caller-session. built from thread-id (old versions used the logFileName)
	 * @param logData
	 */
	void writeLogDataToCache(Serializable sessionIdentifier, LogData[] logDataColl)
	{
		log.debug( "writing to cache, identifier= " + sessionIdentifier + ", " + logDataColl.length + "entries.");
	    cache.put(new Element(sessionIdentifier, logDataColl));
	}

	
	/**
	 * 
	 * @param sessionIdentifier - the identifier of the caller-session. built from thread-id (old versions used the logFileName)
	 * @return
	 */
	public LogData[] readLogDataFromCache( Serializable sessionIdentifier )
	{
		LogData[] logDataColl = null;
		try
		{
			Element element = cache.get( sessionIdentifier );
			if( element != null )
			{
				logDataColl = (LogData[])element.getObjectValue();
				
				log.debug( "logData was read from cache, identifier= " + sessionIdentifier + ", " + logDataColl.length + "entries.");
				boolean removed = cache.remove( sessionIdentifier );			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("ERROR on readLogDataFromCache " + e);			
		}
		
		return logDataColl;
	}

}
