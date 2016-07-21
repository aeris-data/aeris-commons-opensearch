package fr.aeris.commons.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.Cleanable;
import fr.sedoo.commons.util.ListUtil;
import fr.sedoo.commons.util.StringUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class LocalFileSystemMultipleRootCachedImpl implements CollectionDAO, Cleanable {

	private final static int UPDATE_DELAY_MILLIS = 3600000;

	private String currentContent = "";
	private String configFileName = "";
	private List<LocalFileSystemSingleRootImpl> singleRootDaos = new ArrayList<>();
	CacheManager cacheManager;
	private Cache cache;

	Logger logger = LoggerFactory.getLogger(LocalFileSystemMultipleRootCachedImpl.class);

	@PostConstruct
	private void postConstruct() {
		cacheManager = CacheManager.getInstance();
		cache = cacheManager.getCache("appCache");
		if (cache == null) {
			cacheManager.addCache("appCache");
			cache = cacheManager.getCache("appCache");
			CacheConfiguration config = cache.getCacheConfiguration();
			config.setEternal(true);
		}

		try {
			checkNewContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Scheduled(fixedDelay = UPDATE_DELAY_MILLIS)
	public void schedule() {
		try {
			checkNewContent();
			updateCollections();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkNewContent() throws IOException {
		if (StringUtil.isNotEmpty(getConfigFileName())) {
			File file = new File(getConfigFileName());

			Properties properties = new Properties();

			logger.info("Lecture du fichier de collections");
			logger.debug("Fichier de configuration: " + file);

			if (file.exists()) {

				FileInputStream inputStream = new FileInputStream(file);
				properties.load(inputStream);

				Iterator<Object> iterator = properties.keySet().iterator();

				List<String> lines = new ArrayList<>();
				while (iterator.hasNext()) {
					String collectionPrefix = (String) iterator.next();
					String value = StringUtil.trimToEmpty(properties.getProperty(collectionPrefix));
					lines.add(collectionPrefix + "=" + value);
					logger.debug("Ligne trouvée :" + collectionPrefix + "=" + value);

				}

				String aux = ListUtil.toSeparatedString(lines, ",");
				if (aux.compareTo(currentContent) != 0) {
					currentContent = aux;
					singleRootDaos.clear();

					iterator = properties.keySet().iterator();

					while (iterator.hasNext()) {
						String collectionPrefix = (String) iterator.next();
						LocalFileSystemSingleRootImpl newDao = new LocalFileSystemSingleRootImpl();
						newDao.setRoot(StringUtil.trimToEmpty(properties.getProperty(collectionPrefix)));
						newDao.setCollectionPrefix(collectionPrefix);
						singleRootDaos.add(newDao);

					}

				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllCollections() {
		List<String> result = new ArrayList<>();
		logger.debug("Nombre de DAO " + singleRootDaos.size());
		if (cache.get("collections") == null || cache.get("collections").isExpired()) {
			result = updateCollections();
			logger.info("Pas de cache, création du cache");
		} else {
			Element el = cache.get("collections");
			result = (List<String>) el.getObjectValue();
		}
		return result;
	}

	private List<String> updateCollections() {
		List<String> result = new ArrayList<>();
		logger.info("Mise à jour des collections");
		for (CollectionDAO dao : singleRootDaos) {
			if (dao instanceof LocalFileSystemSingleRootImpl) {
				LocalFileSystemSingleRootImpl aux = (LocalFileSystemSingleRootImpl) dao;
				logger.debug("getAllCollection sur le DAO de racine " + aux.getRoot() + " pour le prefixe "
						+ aux.getCollectionPrefix());
			}
			result.addAll(dao.getAllCollections());
			cache.put(new Element("collections", result));
		}
		logger.info("Mise à jour des collections terminée");
		return result;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {
		List<OSEntry> result = new ArrayList<>();
		for (CollectionDAO dao : singleRootDaos) {
			result.addAll(dao.findAll(collections, startDate, endDate));
		}
		return result;
	}

	@Override
	public List<OSEntry> findTerms(List<String> collections, List<String> terms, String startDate, String endDate) {
		List<OSEntry> result = new ArrayList<>();
		for (CollectionDAO dao : singleRootDaos) {
			result.addAll(dao.findTerms(collections, terms, startDate, endDate));
		}
		return result;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	public void clean() throws Exception {
		cacheManager.clearAll();
		checkNewContent();
	}

	@Override
	public String getFirstFolder(String collection) {
		String result = "";
		for (CollectionDAO dao : singleRootDaos) {
			if (collection.contains(((LocalFileSystemSingleRootImpl) dao).getCollectionPrefix())) {
				result = dao.getFirstFolder(collection);
			}
		}
		return result;
	}

	@Override
	public String getLastFolder(String collection) {
		String result = "";
		for (CollectionDAO dao : singleRootDaos) {
			if (collection.contains(((LocalFileSystemSingleRootImpl) dao).getCollectionPrefix())) {
				result = dao.getLastFolder(collection);
			}
		}
		return result;
	}

	@Override
	public Properties getCollectionProperties(String collection) {
		Properties result = new Properties();
		for (CollectionDAO dao : singleRootDaos) {
			if (collection.contains(((LocalFileSystemSingleRootImpl) dao).getCollectionPrefix())) {
				result = dao.getCollectionProperties(collection);
			}
		}
		return result;
	}

}
