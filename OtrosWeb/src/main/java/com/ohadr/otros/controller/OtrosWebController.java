package com.ohadr.otros.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ohadr.otros.OtrosUtils;
import com.ohadr.otros.core.CacheHolder;
import com.ohadr.otros.core.MyLogReader;

import pl.otros.logview.LogData;



@Controller
public class OtrosWebController
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OtrosWebController.class.getName());

	@Autowired
	private CacheHolder cacheHolder;

	@Autowired
	MyLogReader logReader;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String demo(ModelMap model) throws Exception {
		model.addAttribute("message", "it works");
		return "hello";
	}


    /**
     * the response to client contains the thread-id that tails the file. the client uses this id as identifier upon calling
     * {link @getLogDataFromCache}
     * 
     * @param logFilePath
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/secured/setLogFile", method = RequestMethod.POST)
    protected void setLogFile(
            @RequestParam String logFilePath,
            HttpServletResponse response) throws IOException 
    {  
    	log.debug("setLogFile is called, for file: " + logFilePath);
    	
    	long clientIdentifier = logReader.startTailingFile( logFilePath );
    	
    	//PrintWriter writer = response.getWriter();
    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println( clientIdentifier );
    }
    
    
    
    @RequestMapping(value = "/secured/getLogDataFromCache", method = RequestMethod.GET)
    protected void getLogDataFromCache(
            @RequestParam String logFilePath,
            @RequestParam long clientIdentifier,
            HttpServletResponse response) throws IOException 
    {  
    	log.debug("getLogDataFromCache is called, for file: " + logFilePath);
    	
    	PrintWriter writer = response.getWriter();

        log.debug("reading from cache...");
    	LogData[] logDataColl = readLogDataFromCache( clientIdentifier );
        log.debug("reading from cache finished");

        log.debug("converting from cache to json...");
    	String jsonResponse = OtrosUtils.convertToJson( logDataColl );	 
        log.debug("converting from cache to json finished");

    	if( jsonResponse == null )
    	{
    		//represent an emptry object:
    		jsonResponse = "[]";
    	}
        
    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);

		writer.println( jsonResponse );
    }


	
/*
 * 	@Scheduled(fixedDelay=1000)
	public void timerHandler()
	{
		log.info("timer is called");
		
		readLogDataFromCache();
	}
	*/
	
	private LogData[] readLogDataFromCache( Long clientIdentifier )
	{
		LogData[] logDataColl = cacheHolder.readLogDataFromCache( clientIdentifier );

		return logDataColl;
	}
}