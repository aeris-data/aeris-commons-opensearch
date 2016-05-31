package fr.aeris.commons.service;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/images")
public class ImagesService {

	@GET
	@Path("/getImage")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response serveImage(@QueryParam("image") String filepath) {
		File file = new File(filepath);
		ResponseBuilder response;
		if (file.exists()) {
			response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM);
		} else {
			response = Response.noContent();
		}
		return response.build();
	}

}
