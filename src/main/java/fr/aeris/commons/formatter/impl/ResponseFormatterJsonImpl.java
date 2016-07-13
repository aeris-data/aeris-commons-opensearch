package fr.aeris.commons.formatter.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.ntp.TimeStamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.aeris.commons.formatter.ResponseConstants;
import fr.aeris.commons.formatter.ResponseFormatter;
import fr.aeris.commons.model.elements.JsonResponse;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.OSEntryComparator;

public class ResponseFormatterJsonImpl implements ResponseFormatter {

	@Override
	@SuppressWarnings("unchecked")
	public String format(Object object, HttpServletRequest httpRequest) throws JsonProcessingException {
		JsonResponse responseObject = new JsonResponse();
		List<OSEntry> result = (List<OSEntry>) object;
		responseObject.setId(UUID.randomUUID().toString());
		responseObject.setTitle(ResponseConstants.FEED_TITLE);
		responseObject.setUpdated(new TimeStamp(System.currentTimeMillis()).toString());
		for (String author : ResponseConstants.FEED_AUTHORS) {
			responseObject.addAuthor(author);
		}
		responseObject.addLink(httpRequest.getRequestURL() + "?" + httpRequest.getQueryString());
		responseObject.setTotalResults(result.size());
		Collections.sort(result, new OSEntryComparator());
		responseObject.setGranules(result);

		ObjectMapper mapper = new ObjectMapper();
		String responseString = mapper.writeValueAsString(responseObject);
		return responseString;
	}

}
