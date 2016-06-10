package fr.aeris.commons.model.elements;

public class FileObject {

	private String type;
	private String name;
	private String extension;
	private String path;
	private String url;
	private Long size;
	private String modified;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		if (size == 0) {
			this.size = 1L;
		} else {
			this.size = size;
		}
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String string) {
		this.modified = string;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
