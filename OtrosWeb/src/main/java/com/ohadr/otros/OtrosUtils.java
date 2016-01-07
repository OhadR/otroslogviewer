package com.ohadr.otros;

import java.io.ByteArrayOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;


public class OtrosUtils {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OtrosUtils.class.getName());

    /**
     * 
     * @param objectToConvert
     * @return: JSON-String that represents objectToConvert. null if objectToConvert is null.
     */
	public static <T> String convertToJson(T objectToConvert)
    {
        if( objectToConvert == null )
        	return null;
    	
    	ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream bis = new ByteArrayOutputStream();

        String json = null;
        try
        {
            mapper.writeValue(bis, objectToConvert);
            json = bis.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("error converting " + objectToConvert.getClass().getSimpleName() + " to JSON");
        }

//        log.debug("convertToJson() result: " + json);
        return json;
    }
	
}
