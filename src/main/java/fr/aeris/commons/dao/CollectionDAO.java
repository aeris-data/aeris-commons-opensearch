package fr.aeris.commons.dao;

import java.util.List;

import fr.aeris.commons.model.elements.OSEntry;

public interface CollectionDAO {

	String BEAN_NAME = "collectionDao";

	/**
	 * Look for all collections
	 * 
	 * @return List of all collections
	 */
	public List<String> getAllCollections();

	/**
	 * Find all granules corresponding to the passed parameters
	 * 
	 * @param collections
	 *            Collections to search in
	 * @param startDate
	 * @param endDate
	 * @return List of all found granules
	 */
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate);

	/**
	 * Find all granules corresponding to the passed parameters
	 * 
	 * @param collections
	 *            Collections to search in
	 * @param terms
	 *            Search terms
	 * @param startDate
	 * @param endDate
	 * @return List of all found granules
	 */
	public List<OSEntry> findTerms(List<String> collections, List<String> terms, String startDate, String endDate);

}
