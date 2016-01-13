package com.ohadr.otros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;


public class OtrosWebStatus 
{
	//list of pairs; each pair is clientIdentifier and the number of chunks per this client:
	public List<Pair<Serializable, Integer>> clientToChunk = new ArrayList<>();
	
	// num of Elements both diskStore and memStore
	public int numElementsInCache;	

	public int numThreads;	
}
