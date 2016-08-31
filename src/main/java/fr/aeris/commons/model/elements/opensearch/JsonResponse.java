package fr.aeris.commons.model.elements.opensearch;

import java.util.ArrayList;
import java.util.List;

public class JsonResponse {

	private String id;
	private String title;
	private String updated;
	private List<String> authors;
	private List<String> links;
	private int totalResults;
	private List<OSEntry> granules;

	public JsonResponse() {
		authors = new ArrayList<String>();
		links = new ArrayList<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> author) {
		this.authors = author;
	}

	public void addAuthor(String author) {
		this.authors.add(author);
	}

	public List<String> getLinks() {
		return links;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}

	public void addLink(String link) {
		this.links.add(link);
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public List<OSEntry> getGranules() {
		return granules;
	}

	public void setGranules(List<OSEntry> granules) {
		this.granules = granules;
	}

	public void addGranule(OSEntry granule) {
		this.granules.add(granule);
	}

}
