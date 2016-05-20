package fr.aeris.commons.utils;

import javax.xml.namespace.QName;

public interface NamespaceConstants {

	public final static String OPENSEARCH_NS = "http://a9.com/-/spec/opensearch/extensions/parameters/1.0/";
	public final static String OPENSEARCH_PREFIX = "os";

	public final static String PARAM_NS = "http://a9.com/-/spec/opensearch/extensions/parameters/1.0/";
	public final static String PARAM_PREFIX = "param";

	public final static String TIME_NS = "http://a9.com/-/opensearch/extensions/time/1.0/";
	public final static String TIME_PREFIX = "time";

	public final static String DC_NS = "http://purl.org/dc/elements/1.1/";
	public final static String DC_PREFIX = "dc";

	public final static String EO_NS = "http://a9.com/-/opensearch/extensions/eo/1.0/";
	public final static String EO_PREFIX = "eo";

	public final static String MEDIA_NS = "http://search.yahoo.com/mrss/";
	public final static String MEDIA_PREFIX = "media";

	public final static QName QNAME_DATE = new QName(DC_NS, "date", DC_PREFIX);
	public final static QName QNAME_TOTALRESULTS = new QName(OPENSEARCH_NS, "totalResults", OPENSEARCH_PREFIX);
	public final static QName QNAME_PARAM = new QName(PARAM_NS, "Parameter", PARAM_PREFIX);
	public final static QName QNAME_PARAM_OPTION = new QName(PARAM_NS, "Option", PARAM_PREFIX);
	public final static QName QNAME_PARENTIDENTIFIER = new QName(EO_NS, "parentIdentifier", EO_PREFIX);
	public final static QName QNAME_TYPE = new QName(DC_NS, "type", DC_PREFIX);
	public final static QName QNAME_MEDIA_GROUP = new QName(MEDIA_NS, "group", MEDIA_PREFIX);
	public final static QName QNAME_MEDIA_CONTENT = new QName(MEDIA_NS, "content", MEDIA_PREFIX);
	public final static QName QNAME_MEDIA_CATEGORY = new QName(MEDIA_NS, "category", MEDIA_PREFIX);

}
