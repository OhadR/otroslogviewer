package com.ohadr.otros.core;

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

	public void writeLogDataToCache(String logFileName, LogData[] logData)
	{
	    cache.put(new Element(logFileName, logData));
	}

	
	public LogData[] readLogDataFromCache( String logFileName )
	{
		LogData[] logDataColl = null;
		try
		{
			Element element = cache.get( logFileName );
			if( element != null )
			{
				logDataColl = (LogData[])element.getObjectValue();
				
				log.debug( "logData was read from cache: " + logDataColl.length + "entries.");
				boolean removed = cache.remove( logFileName );			
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
