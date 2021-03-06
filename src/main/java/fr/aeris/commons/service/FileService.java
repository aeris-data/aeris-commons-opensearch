package fr.aeris.commons.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.aeris.commons.dao.FileSystemDAO;
import fr.aeris.commons.dao.impl.FileSystemDAOImpl;
import fr.aeris.commons.model.elements.FileObject;
import fr.sedoo.commons.spring.SpringBeanFactory;

@Path("/files")
public class FileService {

	private final static Logger log = LoggerFactory.getLogger(FileService.class);

	// Do NOT modify
	private final String USER_AGENT = "Mozilla/5.0";

	FileSystemDAO fileSystemDao;
	FileSystemDAOImpl dao;
	String currentFolder;
	String token;
	String tokenProvider;

	@Context
	HttpServletRequest httpRequest;

	@PostConstruct
	private void init() {
		if (fileSystemDao == null) {
			SpringBeanFactory springBeanFactory = new SpringBeanFactory();
			fileSystemDao = (FileSystemDAO) springBeanFactory.getBeanByName(FileSystemDAO.BEAN_NAME);
		}
		dao = (FileSystemDAOImpl) fileSystemDao;

	}

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listFiles(@QueryParam("path") String path) {
		List<FileObject> results = new ArrayList<>();
		ResponseBuilder response = null;
		String baseDir = dao.getBaseDirectory();
		currentFolder = baseDir + path;
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		List<File> fileList = fileSystemDao.listFolder(path);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (fileList.isEmpty()) {
			response = Response.ok().status(Status.NO_CONTENT).entity("No content");
		} else {
			for (File file : fileList) {
				FileObject fileObject = new FileObject();
				String filePath = file.getAbsolutePath().replace(baseDir, "");
				fileObject.setPath(filePath);
				if (file.isDirectory()) {
					fileObject.setType("folder");
				} else {
					fileObject.setType("file");
				}
				fileObject.setName(file.getName());
				fileObject.setExtension(FilenameUtils.getExtension(file.getName()));
				fileObject.setSize(file.length() / 1024);
				String baseUrl = httpRequest.getRequestURL().toString();
				int endIndex = baseUrl.lastIndexOf("/");
				if (endIndex != -1 && fileObject.getType().equals("file")) {
					String url = baseUrl.substring(0, endIndex);
					url = url + "/get?path=" + file.getAbsolutePath();
					fileObject.setUrl(url);
				}
				Date timestamp = new Date(file.lastModified());
				fileObject.setModified(sdf.format(timestamp));
				results.add(fileObject);
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				String responseString = mapper.writeValueAsString(results);
				response = Response.ok().entity(responseString).type(MediaType.APPLICATION_JSON);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return response.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization,X-Requested-With")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
	}

	@POST
	@Path("/list")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFiles(final FormDataMultiPart multiPart) {
		String baseDir = dao.getBaseDirectory();
		String folder = "";
		String completePath = "";
		ResponseBuilder resp = null;
		List<FormDataBodyPart> bodyParts = multiPart.getFields("file");
		try {
			BodyPartEntity folderEntity = (BodyPartEntity) multiPart.getField("folder").getEntity();
			folder = IOUtils.toString(folderEntity.getInputStream());
			completePath = baseDir + folder;
			BodyPartEntity tokenEntity = (BodyPartEntity) multiPart.getField("token").getEntity();
			token = IOUtils.toString(tokenEntity.getInputStream());
			BodyPartEntity tokenProviderEntity = (BodyPartEntity) multiPart.getField("token-provider").getEntity();
			tokenProvider = IOUtils.toString(tokenProviderEntity.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean auth = validateToken();

		if (!auth) {
			resp = Response.status(HttpStatus.SC_UNAUTHORIZED).entity("You must be identified to perform this action");
			log.info("---> " + httpRequest.getRemoteAddr() + " tried to upload file in " + completePath
					+ " but was unidentified");
		} else {
			for (int i = 0; i < bodyParts.size(); i++) {
				BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
				String fileName = bodyParts.get(i).getContentDisposition().getFileName();
				String file = dao.getBaseDirectory() + File.separator + folder + File.separator + fileName;
				resp = saveFile(bodyPartEntity.getInputStream(), file);
				log.info(httpRequest.getRemoteAddr() + " uploaded file " + fileName + " in " + completePath + "");
			}
		}

		return resp.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization,X-Requested-With")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
	}

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response serveFile(@QueryParam("path") String filepath) {
		File file = new File(filepath);
		ResponseBuilder response;
		if (file.exists() && file.getAbsolutePath().startsWith(dao.getBaseDirectory())) {
			response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
					"attachment; filename='" + file.getName() + "'");
		} else {
			response = Response.ok().status(Status.NO_CONTENT).entity("No content");
		}
		return response.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization,X-Requested-With")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
	}

	@GET
	@Path("/view")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, "application/pdf" })
	public Response showFile(@QueryParam("path") String filepath) {
		File file = new File(dao.getBaseDirectory() + filepath);
		String fileType;
		ResponseBuilder response;
		if (file.exists() && file.getAbsolutePath().startsWith(dao.getBaseDirectory())) {
			String extension = FilenameUtils.getExtension(file.getName());
			if (extension.equals("pdf")) {
				fileType = "application/pdf";
			} else if (extension.equals("html")) {
				fileType = MediaType.TEXT_HTML;
			} else {
				fileType = MediaType.TEXT_PLAIN;
			}
			response = Response.ok(file).header("Content-Disposition", "filename='" + file.getName() + "'")
					.type(fileType);
		} else {
			response = Response.ok().status(Status.NO_CONTENT).entity("No content");
		}
		return response.build();
	}

	private ResponseBuilder saveFile(InputStream file, String name) {
		try {
			java.nio.file.Path path = FileSystems.getDefault().getPath(name);
			File testFile = path.toFile();
			if (testFile.exists()) {
				return Response.status(HttpStatus.SC_CONFLICT).entity("File already exist on the server");
			} else {
				Files.copy(file, path);
				return Response.ok().status(HttpStatus.SC_CREATED).entity("File saved");
			}
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage());
		}
	}

	private boolean validateToken() {
		try {
			String url = tokenProvider;
			String urlParameters = "token=" + token;
			URL obj = new URL(url + "?" + urlParameters);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());

			if (responseCode == 200) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("Error trying to validate token: " + token);
		}
		return false;

	}

}
