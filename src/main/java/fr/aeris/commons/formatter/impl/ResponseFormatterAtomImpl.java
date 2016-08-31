package fr.aeris.commons.formatter.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElementWrapper;
import org.apache.abdera.model.Feed;
import org.apache.commons.net.ntp.TimeStamp;

import fr.aeris.commons.formatter.ResponseConstants;
import fr.aeris.commons.formatter.ResponseFormatter;
import fr.aeris.commons.model.elements.opensearch.Group;
import fr.aeris.commons.model.elements.opensearch.OSEntry;
import fr.aeris.commons.utils.NamespaceConstants;
import fr.aeris.commons.utils.OSEntryComparator;

public class ResponseFormatterAtomImpl implements ResponseFormatter {

	@Override
	@SuppressWarnings("unchecked")
	public String format(Object object, HttpServletRequest httpRequest) throws Exception {
		Abdera abdera = new Abdera();
		Feed feed = abdera.newFeed();
		List<OSEntry> result = (List<OSEntry>) object;

		feed.declareNS(NamespaceConstants.DC_NS, NamespaceConstants.DC_PREFIX)
				.declareNS(NamespaceConstants.EO_NS, NamespaceConstants.EO_PREFIX)
				.declareNS(NamespaceConstants.MEDIA_NS, NamespaceConstants.MEDIA_PREFIX);
		feed.setId(UUID.randomUUID().toString());
		feed.setTitle(ResponseConstants.FEED_TITLE);
		feed.setUpdated(new TimeStamp(System.currentTimeMillis()).toString());
		for (String author : ResponseConstants.FEED_AUTHORS) {
			feed.addAuthor(author);
		}
		feed.addLink(httpRequest.getRequestURL() + "?" + httpRequest.getQueryString(), "self");
		Collections.sort(result, new OSEntryComparator());
		feed.addSimpleExtension(NamespaceConstants.QNAME_TOTALRESULTS, String.valueOf(result.size()));

		for (OSEntry aux : result) {
			Entry entry = feed.addEntry();
			// Date
			Element date = entry.addExtension(NamespaceConstants.QNAME_DATE);
			date.setText(aux.getDate().toString());
			// ParentIdentifier
			Element parentIdentifier = entry.addExtension(NamespaceConstants.QNAME_PARENTIDENTIFIER);
			parentIdentifier.setText(aux.getParentIdentifier());
			// Type
			Element type = entry.addExtension(NamespaceConstants.QNAME_TYPE);
			type.setText(aux.getType());
			// Media group
			ExtensibleElementWrapper group = new Group(abdera.getFactory(), NamespaceConstants.QNAME_MEDIA_GROUP);
			entry.addExtension(group);
			// Media content
			Element content = group.addExtension(NamespaceConstants.QNAME_MEDIA_CONTENT);
			content.setAttributeValue("medium", "image").setAttributeValue("url", aux.getMedia().getContent());
			group.addExtension(content);
			// Media category
			Element category = group.addExtension(NamespaceConstants.QNAME_MEDIA_CATEGORY);
			category.setAttributeValue("scheme", "http://www.opengis.net/spec/EOMPOM/1.0")
					.setText(aux.getMedia().getCategory());
			group.addExtension(category);
		}
		return feed.toString();
	}

}
