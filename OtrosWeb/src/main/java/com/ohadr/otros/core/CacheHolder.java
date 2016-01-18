package com.ohadr.otros.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ohadr.otros.OtrosConstants;
import com.ohadr.otros.OtrosWebStatus;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import pl.otros.logview.LogData;

@Component
public class CacheHolder implements InitializingBean
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CacheHolder.class.getName());

	@Value("${log_data_entries_per_chunk}")
	private int MAX_LOG_DATA_ENTRIES_PER_CHUNK;

	private Ehcache cache;
	
	public CacheHolder()
	{
        sessionChunksMap = new HashMap<Serializable, List<Integer>>();
	}
	
	
	/**
	 * maps each sessionIdentifier to a list of chunk-IDs that are waiting to be read by client
	 */
	private Map<Serializable, List<Integer>> sessionChunksMap;

	private int chunks_counter = 0;

	@Override
	public void afterPropertiesSet() throws Exception 
	{
        // Create a cache manager
        final CacheManager cacheManager = CacheManager.getInstance();

        cache = cacheManager.addCacheIfAbsent( OtrosConstants.CACHE_NAME );
        
        log.info("logDataEntriesPerChunk: " + MAX_LOG_DATA_ENTRIES_PER_CHUNK);
   	}

	/**
	 * 
	 * @param sessionIdentifier - the identifier of the caller-session. built from thread-id (old versions used the logFileName)
	 * @param logData
	 */
	void writeLogDataToCache(Serializable sessionIdentifier, LogData[] logDataColl)
	{
		log.debug( "writing to cache, identifier= " + sessionIdentifier + ", " + logDataColl.length + " entries.");

		if( logDataColl.length > MAX_LOG_DATA_ENTRIES_PER_CHUNK )
		{
			writeLogDataToCacheInChunks(sessionIdentifier, logDataColl);
		}
		else
		{
			writeLogDataChunkToCache(sessionIdentifier, logDataColl);
		}
	}

	/**
	 * the key in the cache is built from (sessionIdentifier + sub-chunk-id). this is managed by the 'sessionChunksMap',
	 * that stores per each sessionIdentifier a list of sub-chunks-id. then, when a client reads data, he uses its sessionIdentifier,
	 * so the read method checks the map for the chunk-id, builds the cache-key, and gets the data.
	 * @param sessionIdentifier
	 * @param logDataColl
	 */
	private void writeLogDataChunkToCache(Serializable sessionIdentifier, LogData[] logDataColl) 
	{
		List<Integer> chunksColl;

		if( sessionChunksMap.containsKey(sessionIdentifier) )
		{
			chunksColl = sessionChunksMap.get(sessionIdentifier);
		}
		else
		{
			chunksColl = new ArrayList<Integer>();
			sessionChunksMap.put(sessionIdentifier, chunksColl);
		}
		// TODO generate chunk-id:
		int chunkId = ++chunks_counter ;
				//LongGenerator.;//of apache

		chunksColl.add( chunkId );
		
		String elementId = sessionIdentifier + "_" + chunkId;

		log.debug( "writing chunk to cache, elementId= " + elementId + ", " + logDataColl.length + " entries.");
		cache.put(new Element(elementId, logDataColl));
		
	}

	private void writeLogDataToCacheInChunks(Serializable sessionIdentifier, LogData[] logDataColl) 
	{
		//log.debug( "writing to cache in chunks, identifier= " + sessionIdentifier + ", " + logDataColl.length + " entries.");

		int fromIndex = 0;
		int toIndex = MAX_LOG_DATA_ENTRIES_PER_CHUNK;
		while( toIndex <= logDataColl.length)
		{
			LogData[] chunk = Arrays.copyOfRange(logDataColl, fromIndex, toIndex); 
			writeLogDataToCache(sessionIdentifier, chunk);
			fromIndex = toIndex;
			toIndex += MAX_LOG_DATA_ENTRIES_PER_CHUNK;
		}
		//handle the last chunk:
		LogData[] chunk = Arrays.copyOfRange(logDataColl, fromIndex, logDataColl.length); 
		writeLogDataToCache(sessionIdentifier, chunk);
	}

	/**
	 * 
	 * @param sessionIdentifier - the identifier of the caller-session. built from thread-id (old versions used the logFileName)
	 * @return
	 */
	public LogData[] readLogDataFromCache( Serializable sessionIdentifier )
	{
		LogData[] logDataColl = null;
		
		if( ! sessionChunksMap.containsKey(sessionIdentifier) )
		{
			//no need to log this, as this is a normal situation that happens periodically and will blow up the log:
			//log.info( "readLogDataFromCache did not find identifier= " + sessionIdentifier + " in the chunks map");
			return null;
		}
		
		//no need to log this, as this is a normal situation that happens periodically and will blow up the log:
		// log.info( "readLogDataFromCache finds identifier= " + sessionIdentifier + " in the chunks map.");
		
		List<Integer> chunksColl = sessionChunksMap.get( sessionIdentifier );
		if(chunksColl.isEmpty())
		{
			//the client sends periodic requests for new data. but if there is no new data, the cache is empty:
			return null;
		}
		
		//Integer chunkId = chunksColl.get( 0 );
		Integer chunkId = chunksColl.remove( 0 );
		String elementId = sessionIdentifier + "_" + chunkId;

		try
		{
			Element element = cache.get( elementId );
			if( element != null )
			{
				logDataColl = (LogData[])element.getObjectValue();
				
				log.debug( "logData was read from cache, elementId= " + elementId + ", " + logDataColl.length + " entries.");
				boolean removed = cache.remove( elementId );			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("ERROR on readLogDataFromCache " + e);			
		}
		
		return logDataColl;
	}

	public OtrosWebStatus getStatus() 
	{
		OtrosWebStatus status = new OtrosWebStatus();
		for(Map.Entry<Serializable, List<Integer>> entry : sessionChunksMap.entrySet())
		{
			status.clientToChunk.add( Pair.of(entry.getKey(), entry.getValue().size()) );
			
		}
		
		status.numElementsInCache = cache.getSize();	// num of Elements both diskStore and memStore

		return status;
	}
	
	/**
	 * we need to remove from the cache all entries that are stored for this client, plus
	 * remove his entries from the sessions-map 
	 */
	public void removeClient(Serializable clientIdentifier) 
	{
		List<Integer> chunksColl = sessionChunksMap.remove( clientIdentifier );
		if( chunksColl == null )
		{
			String msg = "removeClientEntries did not find identifier= " + clientIdentifier + " in the chunks map";
			log.error( msg );
			throw new IllegalArgumentException( msg );
		}
		log.debug( clientIdentifier + " was removed from sessionChunksMap." );
		
		//now delete all entries of this client from cache:
		for(Integer chunkId : chunksColl)
		{
			String elementId = clientIdentifier + "_" + chunkId;
			log.debug( clientIdentifier + " removing elementId= " + elementId + " from cache." );
			boolean removed = cache.remove( elementId );
			if( !removed )
			{
				log.error("removeClientEntries did not find Element in the cache with id= " + elementId );
			}
			
		}

		log.info("client " + clientIdentifier + " was removed successfully from cache." );
	}
}
