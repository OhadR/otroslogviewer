package com.ohadr.otros.core;

import java.util.Date;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ohadr.otros.NormalizedStatistics;
import com.ohadr.otros.OtrosWebStatus;

@Component
@EnableAsync			//to allow @Scheduled
@EnableScheduling		//to allow @Scheduled
public class StatusManager 
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(StatusManager.class.getName());

	@Value("${max_records_in_stats_history}")
	private int MAX_RECORDS_IN_HISTORY;

	@Autowired
	private CacheHolder cacheHolder;

	@Autowired
	private MyLogReader logReader;
	
	//private List<OtrosWebStatus>	statsHistory = new ArrayList<>();
	private NormalizedStatistics	normalizedStatistics = new NormalizedStatistics();
	
	@Scheduled(fixedRateString="${sample_stat_fixed_rate}")
	private void sampleStatus()
	{
		log.info("sampling Status... ");

    	OtrosWebStatus stats = cacheHolder.getStatus();
    	stats.numThreads = logReader.getNumThreads(); 
    	
    	//statsHistory.add(status);
		log.info("logReader.getNumThreads(): "+logReader.getNumThreads());
    	
		if(normalizedStatistics.numThreads.size() > MAX_RECORDS_IN_HISTORY)
		{
			normalizedStatistics.numThreads.remove(0);
			normalizedStatistics.numElementsInCache.remove(0);
			normalizedStatistics.timeOfMeasurements.remove(0);
		}
		normalizedStatistics.numThreads.add( logReader.getNumThreads() );
		normalizedStatistics.numElementsInCache.add( stats.numElementsInCache );
		normalizedStatistics.timeOfMeasurements.add(new Date());
	}
	
	public OtrosWebStatus getCurrentStatistics() 
	{
    	OtrosWebStatus stats = cacheHolder.getStatus();
    	stats.numThreads = logReader.getNumThreads(); 
    	return stats;
	}
	
	public NormalizedStatistics getNormalizedStatsHistory()
	{
		return normalizedStatistics;
	}

}
