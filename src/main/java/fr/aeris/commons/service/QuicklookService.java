package fr.aeris.commons.service;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.opensearch.model.OpenSearchDescription;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.formatter.ResponseFormatter;
import fr.aeris.commons.formatter.impl.ResponseFormatterAtomImpl;
import fr.aeris.commons.formatter.impl.ResponseFormatterJsonImpl;
import fr.aeris.commons.model.elements.opensearch.JsonCollectionsResponse;
import fr.aeris.commons.model.elements.opensearch.OSCollection;
import fr.aeris.commons.model.elements.opensearch.OSEntry;
import fr.aeris.commons.model.elements.opensearch.Url;
import fr.aeris.commons.utils.AtomConstants;
import fr.aeris.commons.utils.CacheCleaner;
import fr.aeris.commons.utils.HttpConstants;
import fr.aeris.commons.utils.NamespaceConstants;
import fr.aeris.commons.utils.OpenSearchConstants;

/**
 * Quicklooks service
 * 
 * Routes:
 * 
 * - GET: quicklook/isAlive : Test this service
 * 
 * - GET: quicklook/cleanCache : clean all quicklooks caches
 * 
 * - GET: quicklook/{format} : get the collections description file (Format can be JSON or XML)
 * 
 * - GET: quicklook/{format}/collection : search for quicklooks with the given parameters
 *
 */
@Path("/quicklook")
public class QuicklookService {

	private final static Logger log = LoggerFactory.getLogger(QuicklookService.class);

	private Factory abderaFactory;
	private ResponseFormatter formatter;

	CacheCleaner cacheCleaner;

	@Autowired
	private CollectionDAO collectionDAO;

	@Context
	HttpServletRequest httpRequest;

	@PostConstruct
	private void init() {
		abderaFactory = Abdera.getInstance().getFactory();
	}

	/**
	 * @api {get} /quicklook/isAlive Vérifier la disponibilité du service
	 * @apiGroup Quicklook
	 * @apiName isAlive
	 * 
	 * 
	 * @apiSuccess {String} Response yes
	 *
	 */
	@GET
	@Path("/isAlive")
	@Produces(MediaType.TEXT_PLAIN)
	public Response isAlive() {
		String answer = "Yes";
		log.info(httpRequest.getRemoteAddr() + " requested 'isAlive' service");
		return Response.status(200).entity(answer).build();

	}

	/*
	 * IMPORTANT: You need to provide an "Authorization" header with the password 
	 * (defined in BeanLocation.xml) in your request to be allowed to clean cache.
	 */
	/**
	 * @api {get} /quicklook/cleanCache Nettoyer le cache
	 * @apiGroup Quicklook
	 * @apiName cleanCache
	 * 
	 * @apiHeader {String} Authorization Token d'identification
	 * 
	 * @apiSuccess {String} Response Confirmation de nettoyage du cache
	 * @apiError 401_Unauthorized Vous devez être identifié pour nettoyer le cache
	 *
	 */
	@GET
	@Path("/cleanCache")
	@Produces(MediaType.TEXT_PLAIN)
	public Response cleanCache() {
		// Recuperation du header "Authorization"
		String ident = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

		// Si le code d'authorisation correspond on vide le cache
		if (ident != null && ident.equals(CacheCleaner.getSecret())) {
			log.info(httpRequest.getRemoteAddr() + " requested 'cleanCache' service");
			String message = cacheCleaner.cleanAll();
			return Response.status(200).entity(message).build();
		} else {
			log.warn(httpRequest.getRemoteAddr() + " requested 'cleanCache' service with incorrect authorization");
			return Response.status(HttpStatus.SC_UNAUTHORIZED)
					.entity("You have to be identified to perform this request.").build();
		}

	}

	/**
	 * @api {get} /quicklook/:format Récupération du fichier de description des collections
	 * @apiGroup Quicklook
	 * @apiName getDescriptionFile
	 * 
 	 * @apiExample Example-Usage:
	 * http://yourserver.com/quicklook/json
	 * 
	 * @apiParam {String=json,atom} [format=atom] Format du fichier à récupérer
	 * 
	 * @apiSuccess {Object} Description Description de l'ensemble des collections disponibles
	 * @apiError (Error) 500_serverError Une erreur est survenue sur le serveur
	 * 
	 * @apiSuccessExample {json} Success-Response-JSON:
	 *  HTTP/1.1 200 OK
	 *  {
	 *    "searchUrl": "http://yourserver.com/quicklook/json/collection?q={searchTerms?}&parentIdentifier={eo:parentIdentifier?}&sd={time:start}&ed={time:end?}",
	 *    "totalResults": 1,
	 *    "results": [
	 *      "collectionName"
	 *    ],
	 *    "details": [
	 *      {
	 *        "name": "collectionName",
	 *        "firstDate": "YYYYMMDD",
	 *        "lastDate": "YYYYMMDD",
	 *        "properties": {}
	 *      }
	 *    ]
	 *  }
	 */
	@GET
	@Path("/{format: .*}") // le chemin peut etre /{format} ou /
	@Produces({ AtomConstants.OPENSEARCH_DESCRIPTION_MEDIA_TYPE, MediaType.APPLICATION_JSON })
	public Response description(@PathParam("format") String format) {
		Response response;
		try {
			OpenSearchDescription openSearchDescription = abderaFactory
					.newExtensionElement(OpenSearchConstants.OPENSEARCH_DESCRIPTION);
			// Ajout des namespaces
			openSearchDescription.declareNS(NamespaceConstants.PARAM_NS, NamespaceConstants.PARAM_PREFIX)
					.declareNS(NamespaceConstants.TIME_NS, NamespaceConstants.TIME_PREFIX)
					.declareNS(NamespaceConstants.EO_NS, NamespaceConstants.EO_PREFIX);
			// Definition des parametres du fichier de description
			openSearchDescription.setShortName("Search");
			openSearchDescription.setDescription("Search engine description");
			openSearchDescription.setLanguage("en");

			// Creation des urls
			configureOpenSearchUrls(openSearchDescription);

			if (StringUtils.isNotBlank(format) && format.equalsIgnoreCase("json")) {
				List<String> collections = collectionDAO.getAllCollections();
				JsonCollectionsResponse responseEntity = new JsonCollectionsResponse();
				URL baseUrl = new URL(httpRequest.getRequestURL().toString());

				for (String coll : collections) {
					OSCollection currColl = new OSCollection();
					currColl.setName(coll);
					String firstFolder = collectionDAO.getFirstFolder(coll);
					if (StringUtils.isNotBlank(firstFolder)) {
						currColl.setFirstDate(firstFolder);
					}
					String lastFolder = collectionDAO.getLastFolder(coll);
					if (StringUtils.isNotBlank(lastFolder)) {
						currColl.setLastDate(lastFolder);
					}

					Properties collectionProperties = collectionDAO.getCollectionProperties(coll);

					if (collectionProperties != null) {
						currColl.setProperties(collectionProperties);
					} else {
						currColl.setProperties(new Properties());
					}

					responseEntity.addDetail(currColl);
				}

				responseEntity.setResults(collections);
				responseEntity.setTotalResults(collections.size());
				String searchUrl = buildUrl(baseUrl, "").toString();
				responseEntity.setSearchUrl(searchUrl);

				ObjectMapper mapper = new ObjectMapper();
				String responseString = mapper.writeValueAsString(responseEntity);
				response = buildOkResponse(responseString, MediaType.APPLICATION_JSON);
			} else {
				response = buildOkResponse(openSearchDescription.toString(),
						AtomConstants.OPENSEARCH_DESCRIPTION_MEDIA_TYPE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			response = Response.serverError().entity(e.getMessage()).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers",
							"origin, content-type, accept, authorization,X-Requested-With")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
		}

		return response;
	}

	protected final Response buildOkResponse(Object entity, String format) {
		CacheControl cacheControl = new CacheControl();
		cacheControl.setPrivate(false);
		cacheControl.setMaxAge(5000);
		Response response = Response.ok(entity).cacheControl(cacheControl).type(format)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization,X-Requested-With")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();

		return response;
	}

	private void configureOpenSearchUrls(OpenSearchDescription openSearchDescription)
			throws ServiceUnavailableException, MalformedURLException {
		List<String> collections = collectionDAO.getAllCollections();
		String completeUrl = httpRequest.getRequestURL().toString();
		URL baseUrl = new URL(completeUrl);
		int endIndex = completeUrl.lastIndexOf("/");
		if (endIndex != -1) {
			String truncUrl = completeUrl.substring(0, endIndex);
			baseUrl = new URL(truncUrl);
		}

		// Creation url atom
		Url atomUrl = createOpenSearchUrl(baseUrl, "atom");
		ExtensibleElement paramAtom = atomUrl.addExtension(NamespaceConstants.QNAME_PARAM);
		paramAtom.setAttributeValue("name", HttpConstants.PARENTIDENTIFIER_PARAMETER);
		paramAtom.setAttributeValue("value", "{" + HttpConstants.QUERY_PARENTIDENTIFIER_LN + "}");
		for (String collection : collections) {
			Element option = paramAtom.addExtension(NamespaceConstants.QNAME_PARAM_OPTION);
			option.setAttributeValue("label", collection);
			option.setAttributeValue("value", collection);
		}

		// Creation url json
		Url jsonUrl = createOpenSearchUrl(baseUrl, "json");
		ExtensibleElement paramJson = jsonUrl.addExtension(NamespaceConstants.QNAME_PARAM);
		paramJson.setAttributeValue("name", HttpConstants.PARENTIDENTIFIER_PARAMETER);
		paramJson.setAttributeValue("value", "{" + HttpConstants.QUERY_PARENTIDENTIFIER_LN + "}");
		for (String collection : collections) {
			Element option = paramJson.addExtension(NamespaceConstants.QNAME_PARAM_OPTION);
			option.setAttributeValue("label", collection);
			option.setAttributeValue("value", collection);
		}

		// Ajout des urls
		openSearchDescription.addExtension(atomUrl);
		openSearchDescription.addExtension(jsonUrl);
	}

	private Url createOpenSearchUrl(URL baseUrl, String format) {
		StringBuilder urlTemplate = buildUrl(baseUrl, format);
		return getOpenSearchUrlFromTemplate(urlTemplate, format);
	}

	private StringBuilder buildUrl(URL baseUrl, String format) {
		StringBuilder urlTemplate = new StringBuilder(baseUrl.toString());
		// Eviter d'avoir 2 slash si le format n'est pas définit
		if (StringUtils.isNotEmpty(format)) {
			urlTemplate.append("/" + format + "/collection");
		} else {
			urlTemplate.append("/collection");
		}
		urlTemplate.append("?");
		addParameterToOpenSearchUrl(HttpConstants.TERMS_PARAMETER, OpenSearchConstants.QUERY_SEARCHTERMS_LN,
				urlTemplate, true);
		urlTemplate.append("&");
		addParameterToOpenSearchUrl(HttpConstants.PARENTIDENTIFIER_PARAMETER, HttpConstants.QUERY_PARENTIDENTIFIER_LN,
				urlTemplate, true);
		urlTemplate.append("&");
		addParameterToOpenSearchUrl(HttpConstants.START_PARAMETER, HttpConstants.QUERY_START_LN, urlTemplate, false);
		urlTemplate.append("&");
		addParameterToOpenSearchUrl(HttpConstants.END_PARAMETER, HttpConstants.QUERY_END_LN, urlTemplate, true);
		return urlTemplate;
	}

	private void addParameterToOpenSearchUrl(String parameterName, String parameterTemplate, StringBuilder urlTemplate,
			boolean optional) {
		urlTemplate.append(parameterName).append("=").append("{").append(parameterTemplate);
		if (optional) {
			urlTemplate.append("?");
		}
		urlTemplate.append("}");
	}

	private Url getOpenSearchUrlFromTemplate(StringBuilder urlTemplate, String format) {
		Url url = new Url(abderaFactory, OpenSearchConstants.URL);
		url.setAttributeValue(OpenSearchConstants.URL_TEMPLATE_LN, urlTemplate.toString());
		if (format.equalsIgnoreCase("atom")) {
			url.setAttributeValue(OpenSearchConstants.URL_TYPE_LN, MediaType.APPLICATION_ATOM_XML);
		} else {
			url.setAttributeValue(OpenSearchConstants.URL_TYPE_LN, MediaType.APPLICATION_JSON);
		}

		return url;
	}

	/**
	 * @api {get} /quicklook/:format/collection Récupération des granules de la collection
	 * @apiGroup Quicklook
	 * @apiName getCollectionGranules
	 * 
	 * @apiExample Example-Usage:
	 * http://yourserver.com/quicklook/json/collection?q=searchTerms&parentIdentifier=collectionName&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
	 * 
	 * @apiParam {String} format Format du fichier à récupérer (json ou xml)
	 * @apiParam {String} [q] Filtrage des granules par mots-clés
	 * @apiParam {String} [parentIdentifier=Toutes] Collections auxquelles appartiennent les granules séparées par un espace ("%20" dans une url)
	 * @apiParam {String} startDate Date de départ
	 * @apiParam {String} endDate Date de fin
	 * 
	 * @apiSuccess {Object} Granule Description de l'ensemble des granules disponibles
	 * @apiError (Error) 400_BadRequest La requête est mal formulée
	 * @apiError (Error) 204_NoContent Aucun granule n'est disponible
	 * 
	 * @apiSuccessExample {json} Success-Response:
	 *  HTTP/1.1 200 OK
	    {
		  "id": "responseId",
		  "title": "responseTitle",
		  "updated": "updatedDate",
		  "authors": [
		    "responseAuthor"
		  ],
		  "links": [
		    "http://yourserver.com/quicklook/json/collection?parentIdentifier=collectionName&sd=YYYY-MM-DD&ed=YYYY-MM-DD"
		  ],
		  "totalResults": 1,
		  "granules": [
		    {
		      "date": "YYYY-MM-DDTHH:mm:ssZ",
		      "parentIdentifier": "collectionName",
		      "type": "granuleType",
		      "media": {
		        "content": "granuleImageUrl",
		        "category": "granuleMediaCategory"
		      }
		    }
		  ]
		}
	 *
	 */
	@GET
	@Path("/{format}/collection")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response jsonSearch(@PathParam("format") String format, @QueryParam("q") String searchTerms,
			@QueryParam("parentIdentifier") String parentIdentifier, @QueryParam("sd") String startDate,
			@QueryParam("ed") String endDate) {
		List<String> terms = new ArrayList<String>();
		List<String> collections = new ArrayList<String>();
		List<OSEntry> result = new ArrayList<OSEntry>();
		String responseMediatype = MediaType.APPLICATION_JSON;

		if (StringUtils.trimToEmpty(endDate).isEmpty() || endDate.equalsIgnoreCase("today")) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			endDate = df.format(new Date());
		}

		// Decoupage searchTerms
		if (StringUtils.isNotBlank(searchTerms)) {
			terms = tokenizeParam(searchTerms);
		}
		try {
			// Decoupage collections
			if (StringUtils.isNotBlank(parentIdentifier))
				collections = tokenizeParam(parentIdentifier);
			else
				collections = collectionDAO.getAllCollections();

			// Recuperation des resultats
			if (terms.isEmpty())
				result = collectionDAO.findAll(collections, startDate, endDate);
			else
				result = collectionDAO.findTerms(collections, terms, startDate, endDate);
		} catch (ServiceUnavailableException e) {
			return Response.status(HttpURLConnection.HTTP_NO_CONTENT)
					.entity("Service unavailable. Retry in a few minutes").header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers",
							"origin, content-type, accept, authorization,X-Requested-With")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
		} catch (Exception e) {
			log.error(httpRequest.getRemoteAddr() + " made a bad request: " + httpRequest.getRequestURL() + "?"
					+ StringUtils.trimToEmpty(httpRequest.getQueryString()));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers",
							"origin, content-type, accept, authorization,X-Requested-With")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
		}

		// Choix du type de reponse
		if (format.equalsIgnoreCase("json")) {
			formatter = new ResponseFormatterJsonImpl();
			responseMediatype = MediaType.APPLICATION_JSON;
		} else if (format.equalsIgnoreCase("atom")) {
			formatter = new ResponseFormatterAtomImpl();
			responseMediatype = MediaType.APPLICATION_ATOM_XML;
		} else {
			log.error(httpRequest.getRemoteAddr() + " made a bad request: " + httpRequest.getRequestURL() + "?"
					+ StringUtils.trimToEmpty(httpRequest.getQueryString()));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Incorrect url format parameter. Must be JSON or ATOM")
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers",
							"origin, content-type, accept, authorization,X-Requested-With")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
		}

		// Formatage et envoi de la reponse
		try {
			String responseString = formatter.format(result, httpRequest);
			return buildOkResponse(responseString, responseMediatype);
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers",
							"origin, content-type, accept, authorization,X-Requested-With")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
		}

	}

	/**
	 * Split the given parameter based on white-space
	 * 
	 * @param parameter
	 *            String to split
	 * @return String list containing the tokens
	 */
	private List<String> tokenizeParam(String parameter) {
		StringTokenizer st = new StringTokenizer(parameter);
		List<String> result = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}

}
