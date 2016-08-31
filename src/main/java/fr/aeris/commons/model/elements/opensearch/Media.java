package fr.aeris.commons.model.elements.opensearch;

public class Media {

	private String content;
	private String category;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Media(String content) {
		this.content = content;
	}

	public Media(String content, String category) {
		this.content = content;
		this.category = category;
	}

}
