package fr.aeris.commons.service;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/images")
public class ImagesService {

	@GET
	@Path("/image")
	public Response serveImage(@QueryParam("q") String filepath) {
		File file = new File(filepath);
		ResponseBuilder response;
		if (file.exists()) {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String type = fileNameMap.getContentTypeFor(filepath);
			response = Response.ok(file, type);
		} else {
			response = Response.noContent();
		}
		return response.build();
	}

}
