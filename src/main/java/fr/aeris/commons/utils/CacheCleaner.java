package fr.aeris.commons.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleaner {

	private final static Logger log = LoggerFactory.getLogger(CacheCleaner.class);

	private final static String ERROR = "Error cleaning cache";
	private final static String ALL_CACHES_CLEANED = "All caches have been cleaned";

	private List<Cleanable> cleanables;
	private static String secret;

	public static String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		CacheCleaner.secret = secret;
	}

	public List<Cleanable> getCleanables() {
		return cleanables;
	}

	public void setCleanables(List<Cleanable> daos) {
		this.cleanables = daos;
	}

	public String cleanAll() {
		log.info("--- Begin cache cleaning...");
		for (Cleanable cleanable : cleanables) {
			try {
				cleanable.clean();
			} catch (Exception e) {
				log.error("An error occured during cache cleaning");
				return ERROR;
			}
		}
		log.info("--- Cache cleaned successfully");
		return ALL_CACHES_CLEANED;
	}

}
