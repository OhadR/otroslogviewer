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

import com.ohadr.otros.NormalizedStatistics;
import com.ohadr.otros.OtrosUtils;
import com.ohadr.otros.OtrosWebStatus;
import com.ohadr.otros.core.MyLogReader;
import com.ohadr.otros.core.StatusManager;

import pl.otros.logview.LogData;



@Controller
public class OtrosWebController
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OtrosWebController.class.getName());

	@Autowired
	MyLogReader logReader;
	
	@Autowired
	StatusManager statManager;

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
            @RequestParam long oldClientIdentifier,
            HttpServletResponse response) throws IOException 
    {  
    	log.info("setLogFile is called, for file: " + logFilePath + ", with old clientId=" + oldClientIdentifier + "; removing old entries for this client...");
    	
        // kill threads of this client
        onClientLeave(oldClientIdentifier);

    	long clientIdentifier = logReader.startTailingFile( logFilePath );
    	
    	//PrintWriter writer = response.getWriter();
    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print( clientIdentifier );
    }
    
    
    
    @RequestMapping(value = "/secured/getLogDataFromCache", method = RequestMethod.GET)
    protected void getLogDataFromCache(
            @RequestParam long clientIdentifier,
            HttpServletResponse response) throws IOException 
    {  
    	log.debug("getLogDataFromCache is called, for clientIdentifier: " + clientIdentifier);
    	
    	PrintWriter writer = response.getWriter();

        log.debug("reading from cache...");
    	LogData[] logDataColl = readLogDataFromCache( clientIdentifier );
        log.debug("reading from cache finished");

        log.debug("converting from cache to json...");
    	String jsonResponse = null;
		try 
		{
			jsonResponse = OtrosUtils.convertToJson( logDataColl );
		} 
		catch (Error e) 
		{
	        log.error("converting from cache to json failed, " + e);

	    	response.setContentType("text/html"); 
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
			return;
		}	 
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


    /**
     * returns the status of cache, and how many thread are running etc.
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/getBackendStatus", method = RequestMethod.GET)
    protected void getBackendStatus(
            HttpServletResponse response) throws IOException 
    {  
    	OtrosWebStatus stats = statManager.getCurrentStatistics();

		String jsonResponse = OtrosUtils.convertToJson( stats );

    	//PrintWriter writer = response.getWriter();
    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print( jsonResponse );
    }
    
    @RequestMapping(value = "/getBackendStatsHistory", method = RequestMethod.GET)
    protected void getBackendStatsHistory(
            HttpServletResponse response) throws IOException 
    {  
    	NormalizedStatistics stats = statManager.getNormalizedStatsHistory();

		String jsonResponse = OtrosUtils.convertToJson( stats );

    	//PrintWriter writer = response.getWriter();
    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print( jsonResponse );
    }

    @RequestMapping(value = "/secured/clientLeftPage", method = RequestMethod.POST)
    protected void onClientLeftPage(
            @RequestParam long clientIdentifier,
            HttpServletResponse response) 
    {  
        log.info("onClientLeftPage, for clientIdentifier: " + clientIdentifier);
        
        // kill threads of this client
        onClientLeave(clientIdentifier);

    	response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_OK);
    }


	private void onClientLeave(long clientIdentifier) 
	{
		logReader.stopClientThread( clientIdentifier );
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
		return logReader.readLogDataFromCache( clientIdentifier );
	}
}