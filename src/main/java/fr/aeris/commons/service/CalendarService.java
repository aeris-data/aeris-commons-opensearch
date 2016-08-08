package fr.aeris.commons.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.OSEntry;

@Path("/calendar")
public class CalendarService {
	
	private final static Logger log = LoggerFactory.getLogger(CalendarService.class);
	
	@Autowired
	private CollectionDAO collectionDAO;
	
	@Context
	HttpServletRequest httpRequest;
	
	@GET
	@Path("/isAlive")
	@Produces(MediaType.TEXT_PLAIN)
	public Response isAlive() {
		String answer = "Yes";
		log.info(httpRequest.getRemoteAddr() + " requested 'isAlive' service");
		return Response.status(200).entity(answer).build();

	}
	

	@GET
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkAvailability(@QueryParam("collection") String collection, @QueryParam("start") String start, @QueryParam("end") String end) {
		List<String> collec = new ArrayList<>();
		List<OSEntry> results = new ArrayList<>();
		collec.add(collection);
		results = collectionDAO.findAll(collec, start, end);

	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	    Date startDate;
	    Date endDate;
	    int days = 0;
		try {
			startDate = sdf.parse(start);
			endDate = sdf.parse(end);
			days = Days.daysBetween(new DateTime(startDate.getTime()), new DateTime(endDate.getTime())).getDays();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String resp = String.valueOf(days);
		
		return Response.ok().entity(resp).build();
	}

}
