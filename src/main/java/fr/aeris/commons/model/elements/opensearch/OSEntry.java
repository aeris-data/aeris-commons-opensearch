package fr.aeris.commons.model.elements.opensearch;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OSEntry {

	private String date;
	private String parentIdentifier;
	private String type;
	private Media media;

	public String getDate() {
		return date;
	}

	public void setDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.date = df.format(date);
	}

	public String getParentIdentifier() {
		return parentIdentifier;
	}

	public void setParentIdentifier(String parentIdentifier) {
		this.parentIdentifier = parentIdentifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

}
