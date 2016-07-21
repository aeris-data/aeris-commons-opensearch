package fr.aeris.commons.model.elements;

import java.util.Properties;

public class OSCollection {

	String name;
	String firstDate;
	String lastDate;
	Properties properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstDate() {
		return firstDate;
	}

	public void setFirstDate(String startDate) {
		this.firstDate = startDate;
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String endDate) {
		this.lastDate = endDate;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
