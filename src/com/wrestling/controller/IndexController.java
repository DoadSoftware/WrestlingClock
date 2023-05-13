package com.wrestling.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrestling.model.Clock;
import com.wrestling.model.Configurations;
import com.wrestling.util.WrestlingFunctions;
import com.wrestling.util.WrestlingUtil;
import com.wrestling.service.WrestlingService;

import net.sf.json.JSONObject;

@Controller
public class IndexController 
{
	@Autowired
	WrestlingService wrestlingService;

	public static String expiry_date = "2023-12-31";
	public static String current_date = "";
	public static String error_message = "";
	public static Clock session_clock = new Clock();
	List<PrintWriter> print_writers = new ArrayList<PrintWriter>();
	public static Socket session_socket;
	public static Configurations session_Configurations = new Configurations();
	public static String session_selected_broadcaster;
	public static boolean is_this_updating = false;
	
	@RequestMapping(value = {"/","/initialise"}, method={RequestMethod.GET,RequestMethod.POST}) 
	public String initialisePage(ModelMap model,
			@ModelAttribute("session_Configurations") Configurations session_Configurations) throws JAXBException, IOException, ParseException 
	{
		
		if(current_date == null || current_date.isEmpty()) {
			current_date = WrestlingFunctions.getOnlineCurrentDate();
		}
		
		if(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CONFIGURATIONS_DIRECTORY + WrestlingUtil.CLOCK_XML).exists()) {
			session_Configurations = (Configurations)JAXBContext.newInstance(Configurations.class).createUnmarshaller().unmarshal(
					new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CONFIGURATIONS_DIRECTORY + WrestlingUtil.CLOCK_XML));
		} else {
			session_Configurations = new Configurations();
			JAXBContext.newInstance(Configurations.class).createMarshaller().marshal(session_Configurations, 
					new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CONFIGURATIONS_DIRECTORY + 
					WrestlingUtil.CLOCK_XML));
		}
		
		model.addAttribute("session_Configurations",session_Configurations);
	
		return "initialise";
	}
	@RequestMapping(value = {"/clock"}, method={RequestMethod.GET,RequestMethod.POST}) 
	public String clockPage(ModelMap model,
			@RequestParam(value = "selectedBroadcaster", required = false, defaultValue = "") String selectedBroadcaster,
			@RequestParam(value = "vizIPAddress", required = false, defaultValue = "") String vizIPAddresss,
			@RequestParam(value = "vizPortNumber", required = false, defaultValue = "") int vizPortNumber,
			@RequestParam(value = "vizSecondaryIPAddress", required = false, defaultValue = "") String vizSecondaryIPAddress,
			@RequestParam(value = "vizSecondaryPortNumber", required = false, defaultValue = "") int vizSecondaryPortNumber) 
					throws JAXBException, IOException, ParseException 
	{
		if(current_date == null || current_date.isEmpty()) {
			current_date = WrestlingFunctions.getOnlineCurrentDate();
		}
		if(current_date == null || current_date.isEmpty()) {
			
			model.addAttribute("error_message","You must be connected to the internet online");
			return "error";
		
		} else if(new SimpleDateFormat("yyyy-MM-dd").parse(expiry_date).before(new SimpleDateFormat("yyyy-MM-dd").parse(current_date))) {
			
			model.addAttribute("error_message","This software has expired");
			return "error";
			
		}else {
			
			session_selected_broadcaster = selectedBroadcaster;
			session_Configurations = new Configurations(selectedBroadcaster, vizIPAddresss, 
					vizPortNumber, vizSecondaryIPAddress, vizSecondaryPortNumber);
			if(vizIPAddresss != null && vizPortNumber > 0) {
				print_writers = WrestlingFunctions.processPrintWriter(session_Configurations);
			}
			
			JAXBContext.newInstance(Configurations.class).createMarshaller().marshal(session_Configurations, 
					new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CONFIGURATIONS_DIRECTORY + WrestlingUtil.CLOCK_XML));
			
			model.addAttribute("session_selected_broadcaster", session_selected_broadcaster);
			model.addAttribute("session_clock",session_clock);
			
			return "clock";
		}
	}
	
	@RequestMapping(value = {"/processHandballProcedures"}, method={RequestMethod.GET,RequestMethod.POST})    
	public @ResponseBody String processHandballProcedures(
			@RequestParam(value = "whatToProcess", required = false, defaultValue = "") String whatToProcess,
			@RequestParam(value = "valueToProcess", required = false, defaultValue = "") String valueToProcess)
					throws JAXBException, IllegalAccessException, InvocationTargetException, IOException, 
					NumberFormatException, InterruptedException
	{	
		switch (whatToProcess.toUpperCase()) {
		case WrestlingUtil.RESET_CLOCK:
			session_clock = new Clock();
			
			new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
					+ WrestlingUtil.CLOCK_JSON), session_clock);
			
			return JSONObject.fromObject(session_clock).toString();
			
		case WrestlingUtil.CHECK_CLOCK_OPTION:

			if(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY + WrestlingUtil.CLOCK_JSON).exists()) {
				session_clock = new ObjectMapper().readValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY + 
						WrestlingUtil.CLOCK_JSON), Clock.class);
			}else {
				session_clock = new Clock();

				new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
						+ WrestlingUtil.CLOCK_JSON), session_clock);
			}
			
			return JSONObject.fromObject(session_clock).toString();
			
		case WrestlingUtil.SELECT_MATCH_HALVES:

			session_clock.setMatchHalves(valueToProcess.split(",")[0]);
			if(valueToProcess.split(",")[0].equalsIgnoreCase("first") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(0);
			}else if(valueToProcess.split(",")[0].equalsIgnoreCase("second") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(1800000);
			}else if(valueToProcess.split(",")[0].equalsIgnoreCase("extra1a") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(0);
			}else if(valueToProcess.split(",")[0].equalsIgnoreCase("extra1b") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(30000);
			}else if(valueToProcess.split(",")[0].equalsIgnoreCase("extra2a") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(0);
			}else if(valueToProcess.split(",")[0].equalsIgnoreCase("extra2b") && valueToProcess.split(",")[1].equalsIgnoreCase("true")) {
				session_clock.setMatchTotalMilliSeconds(30000);
			}
			
			new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
					+ WrestlingUtil.CLOCK_JSON), session_clock);
			
			return JSONObject.fromObject(session_clock).toString();
			
		case WrestlingUtil.LOG_CLOCK_STATUS:
			
			if(session_clock == null) {
				session_clock = new Clock();
			}
			session_clock.setMatchTimeStatus(valueToProcess);

			new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
					+ WrestlingUtil.CLOCK_JSON), session_clock);
			
			return JSONObject.fromObject(session_clock).toString();

		case WrestlingUtil.LOG_OVERWRITE_MATCH_TIME:
			
			session_clock.setMatchTotalMilliSeconds(Long.valueOf((Integer.valueOf(valueToProcess.split(":")[0])*60) + 
					Integer.valueOf(valueToProcess.split(":")[1]))*1000);
			
			new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
					+ WrestlingUtil.CLOCK_JSON), session_clock);
			
			return JSONObject.fromObject(session_clock).toString();

		case WrestlingUtil.LOG_TIME:
			long main_min=0,main_sec=0;
			session_clock.setMatchTotalMilliSeconds(Integer.valueOf(valueToProcess));
			
			new ObjectMapper().writeValue(new File(WrestlingUtil.WRESTLING_DIRECTORY + WrestlingUtil.CLOCK_DIRECTORY 
					+ WrestlingUtil.CLOCK_JSON), session_clock);

			main_min = session_clock.getMatchTotalMilliSeconds()/60000 ;
			main_sec = (session_clock.getMatchTotalMilliSeconds()/1000) - (main_min*60);
			
			if(print_writers.size() > 0) {
				print_writers.get(0).println("-1 RENDERER*FRONT_LAYER*TREE*$Main$noname$MainScoreBug$BottomGrp$txt_Time*GEOM*TEXT SET " + 
						WrestlingFunctions.twoDigitString(main_min) +":"+ WrestlingFunctions.twoDigitString(main_sec) + "\0");
				
				print_writers.get(1).println("LAYER1*EVEREST*TREEVIEW*Main*FUNCTION*TAG_CONTROL SET tTime " + 
						WrestlingFunctions.twoDigitString(main_min) +":"+ WrestlingFunctions.twoDigitString(main_sec) + ";");	
			}
			return JSONObject.fromObject(session_clock).toString();

		default:
			
			return JSONObject.fromObject(session_clock).toString();
			
		}
	}
}