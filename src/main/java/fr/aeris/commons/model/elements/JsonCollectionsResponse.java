package fr.aeris.commons.model.elements;

import java.util.ArrayList;
import java.util.List;

public class JsonCollectionsResponse {

	private String searchUrl;
	private int totalResults;
	private List<String> results;

	public JsonCollectionsResponse() {
		results = new ArrayList<String>();
	}

	public String getSearchUrl() {
		return searchUrl;
	}

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public List<String> getResults() {
		return results;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}

	public void addResult(String collection) {
		results.add(collection);
	}

}
