package fr.aeris.commons.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ws.rs.ServiceUnavailableException;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.OpensearchUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

@SuppressWarnings("unchecked")
public class CollectionDAOCachedFtpImpl implements CollectionDAO {

	private CollectionDAO ftpDao;
	private Cache cache;

	public CollectionDAO getFtpDao() {
		return ftpDao;
	}

	public void setFtpDao(CollectionDAO ftpDao) {
		this.ftpDao = ftpDao;
	}

	@PostConstruct
	public void init() {
		CacheManager cacheManager = CacheManager.getInstance();
		cache = cacheManager.getCache("ftpCache");
		if (cache == null) {
			cacheManager.addCache("ftpCache");
			cache = cacheManager.getCache("ftpCache");
			CacheConfiguration config = cache.getCacheConfiguration();
			config.setTimeToIdleSeconds(3600);
			config.setTimeToLiveSeconds(3600);
		}
	}

	@Override
	public List<String> getAllCollections() {
		List<String> results = new ArrayList<String>();
		// Si les collections sont en cache on le renvoi, sinon connexion au FTP
		// puis on les stocke
		if (cache.get("collections") == null || cache.get("collections").isExpired()) {
			results = ftpDao.getAllCollections();
			cache.put(new Element("collections", results));
		} else {
			Element el = cache.get("collections");
			results = (List<String>) el.getObjectValue();
		}
		return results;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate)
			throws ServiceUnavailableException {
		List<OSEntry> results = new ArrayList<OSEntry>();
		for (String collection : collections) {

			// Verification collection en cache
			if (cache.get(collection) == null || cache.get(collection).isExpired()) {
				results.addAll(ftpDao.findAll(Arrays.asList(collection), startDate, endDate));
			} else {
				Element el = cache.get(collection);
				List<OSEntry> granules = (List<OSEntry>) el.getObjectValue();

				// On récupère tous les granules et on selectionne ceux situes
				// entre les date de debut et fin
				for (OSEntry granule : granules) {
					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date start = df.parse(startDate);
						Date end = df.parse(endDate);

						// recuperation de la date et mise a zero de l'heure
						// pour ne comparer que le jour
						Date granuleDate = OpensearchUtils.dateFromIso8601(granule.getDate()).getTime();
						granuleDate.setHours(0);

						int afterStart = granuleDate.compareTo(start);
						int beforeEnd = granuleDate.compareTo(end);
						if (afterStart >= 0 && beforeEnd <= 0) {
							results.add(granule);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}

		return results;
	}

	@Override
	public List<OSEntry> findTerms(List<String> collections, List<String> terms, String startDate, String endDate)
			throws ServiceUnavailableException {
		List<OSEntry> allGranules = findAll(collections, startDate, endDate);
		List<OSEntry> results = new ArrayList<OSEntry>();
		for (OSEntry granule : allGranules) {
			for (String term : terms) {
				// Recherche dans le nom du parentIdentifier
				if (granule.getParentIdentifier().toLowerCase().contains(term.toLowerCase())) {
					results.add(granule);
				} else {
					// Recherche dans le type
					if (granule.getType().toLowerCase().contains(term.toLowerCase())) {
						results.add(granule);
					}
				}
			}
		}
		return results;
	}

	@Override
	public String getFirstFolder(String collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastFolder(String collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getCollectionProperties(String collection) {
		// TODO Auto-generated method stub
		return null;
	}

}
