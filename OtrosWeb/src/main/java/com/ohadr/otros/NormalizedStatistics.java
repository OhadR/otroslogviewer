package com.ohadr.otros;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NormalizedStatistics 
{
	//TODO list of pairs; each pair is clientIdentifier and the number of chunks per this client:
	//public List<Pair<Serializable, Integer>> clientToChunk = new ArrayList<>();
	
	// num of Elements both diskStore and memStore
	public List<Integer> numElementsInCache = new ArrayList<>();	

	public List<Integer> numThreads = new ArrayList<>();

	public List<Date> timeOfMeasurements = new ArrayList<>();
}
