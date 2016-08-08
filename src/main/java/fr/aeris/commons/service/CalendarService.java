package fr.aeris.commons.service;

import java.util.ArrayList;
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
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
	public Response checkAvailability(@QueryParam("collection") String collection, @QueryParam("start") String start,
			@QueryParam("end") String end) {
		List<String> coll = new ArrayList<>();
		List<OSEntry> granules = new ArrayList<>();
		coll.add(collection);
		granules = collectionDAO.findAll(coll, start, end);

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

		DateTime startDate;
		DateTime endDate;
		int days = 0;
		startDate = formatter.parseDateTime(start);
		endDate = formatter.parseDateTime(end);
		Interval interval = new Interval(startDate, endDate);
		days = Days.daysBetween(startDate, endDate).getDays();

		for (OSEntry granule : granules) {
			System.out.println(interval.contains(new DateTime(granule.getDate())));
		}

		String resp = String.valueOf(days);

		return Response.ok().entity(resp).build();
	}

}
