package com.ohadr.otros;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import pl.otros.logview.LogData;

public class JsonTester {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(JsonTester.class.getName());

	@Test
	public void testJsonConverter()
	{
    	LogData[] logDataColl = generateLogData(500000);
    			//readLogDataFromCache( clientIdentifier );

        log.info("converting from cache to json...");
    	String jsonResponse;
		try
		{
			jsonResponse = OtrosUtils.convertToJson( logDataColl );
	        log.info("converting from cache to json finished. json length:" + jsonResponse.length() );
		} 
		catch (Throwable e) 
		{
            log.error("error converting to JSON, " + e);
			e.printStackTrace();
		}	 
	}
	
	private LogData[] generateLogData(int size)
	{
	    LogData[] datas = new LogData[ size ];
	    for(int i = 0; i < size; ++i)
	    {
	    	datas[i] = new LogData();
	    	//2016-01-07 21:47:35,209 [http-bio-8080-exec-10] [DEBUG] [com.ohadr.otros.controller.OtrosWebController:81] - converting from cache to json...
	    	datas[i].setThread( "http-bio-8080-exec-10" );
	    	datas[i].setClazz( "com.ohadr.otros.controller.OtrosWebController:81" );
	    	datas[i].setMessage( "About to execute reader for task. taskName: RegisterVegasItalyAdHocDepositPlayersTask , readerName:VegasItalyReader , serviceName:ItalyVegasBonusService , marsTaskId: 449821 , numberOfTaskParameters: 4" );
	    }
	    return datas;
	}

}
