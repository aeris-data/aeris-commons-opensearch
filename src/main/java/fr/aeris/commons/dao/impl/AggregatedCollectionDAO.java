package fr.aeris.commons.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.OSEntry;

public class AggregatedCollectionDAO implements CollectionDAO {

	private List<CollectionDAO> daos;

	public List<CollectionDAO> getDaos() {
		return daos;
	}

	public void setDaos(List<CollectionDAO> daos) {
		this.daos = daos;
	}

	@Override
	public List<String> getAllCollections() {
		ArrayList<String> result = new ArrayList<String>();

		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result.addAll(dao.getAllCollections());
			}
		}

		return result;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {

		ArrayList<OSEntry> result = new ArrayList<OSEntry>();

		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result.addAll(dao.findAll(collections, startDate, endDate));
			}
		}

		return result;
	}

	@Override
	public List<OSEntry> findTerms(List<String> collections, List<String> terms, String startDate, String endDate) {
		ArrayList<OSEntry> result = new ArrayList<OSEntry>();

		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result.addAll(dao.findTerms(collections, terms, startDate, endDate));
			}
		}

		return result;
	}

	@Override
	public String getFirstFolder(String collection) {
		String result = "";
		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result = dao.getFirstFolder(collection);
			}
		}
		return result;
	}

	@Override
	public String getLastFolder(String collection) {
		String result = "";
		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result = dao.getLastFolder(collection);
			}
		}
		return result;
	}

	@Override
	public Properties getCollectionProperties(String collection) {
		Properties result = new Properties();
		if (daos != null) {
			for (CollectionDAO dao : daos) {
				result = dao.getCollectionProperties(collection);
			}
		}
		return result;
	}

}
