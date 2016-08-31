package fr.aeris.commons.dao.impl.quicklooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.opensearch.Media;
import fr.aeris.commons.model.elements.opensearch.OSEntry;
import fr.aeris.commons.utils.CollectionFolderValidator;
import fr.aeris.commons.utils.FileUtils;

public class LocalFileSystemSingleRootImpl implements CollectionDAO {

	private final static String IMAGE_SERVICE_URL = "http://opensearch.sedoo.fr/rest/files/getimage?q=";
	private final static String FILENAME_SEPARATOR = "_";
	private String root;
	private CollectionFolderValidator collectionFolderValidator;
	private String collectionPrefix;

	Logger logger = LoggerFactory.getLogger(LocalFileSystemSingleRootImpl.class);

	public LocalFileSystemSingleRootImpl() {
		setCollectionFolderValidator(new SedooCampaignCollectionFolderValidator());
	}

	@Override
	public List<String> getAllCollections() {
		List<String> collections = listCollectionFolders(getRoot());
		return collections;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {

		if (StringUtils.isEmpty(getRoot())) {
			return new ArrayList<>();
		}

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

		DateTime start = formatter.parseDateTime(startDate);
		DateTime end = formatter.parseDateTime(endDate);
		DateTime tempCalendar;

		int daysBetween = Days.daysBetween(start, end).getDays();

		List<OSEntry> granules = new ArrayList<OSEntry>();

		for (String collection : collections) {

			int aux = collection.indexOf(File.separator);

			collection = collection.substring(aux + 1);

			tempCalendar = start;
			for (int i = 0; i <= daysBetween; i++) {
				int year = tempCalendar.getYear();

				// Month
				String month = String.format("%02d", tempCalendar.getMonthOfYear());

				// Day
				String day = String.format("%02d", tempCalendar.getDayOfMonth());

				// Creation du chemin vers le repertoire
				StringBuilder folder = new StringBuilder();
				folder.append(getRoot());
				folder.append(File.separator);
				folder.append(collection);
				folder.append(File.separator);
				folder.append(dateToFolderName(year + month + day));

				granules.addAll(listFiles(collection, folder.toString(), tempCalendar));

				// Passage au jour suivant
				tempCalendar = tempCalendar.plusDays(1);
			}
		}

		return granules;
	}

	@Override
	public List<OSEntry> findTerms(List<String> collections, List<String> terms, String startDate, String endDate) {
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

	/**
	 * List all images files in the given folder and create corresponding
	 * granules
	 * 
	 * @param folder
	 * @param calendar
	 * @return
	 */
	private List<OSEntry> listFiles(String collection, String folder, DateTime calendar) {
		File file = new File(folder);
		String[] names = file.list();
		List<OSEntry> granules = new ArrayList<OSEntry>();
		// Si pas de dossier on retourne un resultat vide
		if (names != null) {
			for (String name : names) {
				File testedFile = new File(folder + File.separator + name);
				if (!testedFile.isDirectory()) {
					try {
						// Si le fichier est une image
						if (FileUtils.isAnImage(testedFile)) {
							// Extraction de la collection et du type depuis le
							// nom du fichier
							String filenameWithoutExt = FilenameUtils.removeExtension(name);
							List<String> filenameParts = Arrays
									.asList(StringUtils.split(filenameWithoutExt, FILENAME_SEPARATOR));
							OSEntry entry = new OSEntry();
							String mediaPath = IMAGE_SERVICE_URL + testedFile.getAbsolutePath();
							entry.setMedia(new Media(mediaPath, "QUICKLOOK"));

							// String parentIdentifier = filenameParts.get(1);
							entry.setParentIdentifier(collectionPrefix + File.separator + collection);

							String type = filenameParts.get(2);
							entry.setType(type);

							// Extraction de la date et de l'heure int hour
							try {
								String dateString = filenameParts.get(0);
								String hourString = filenameParts.get(3).substring(0, 3);

								DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
								DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HHmm");
								DateTime date = dateFormatter.parseDateTime(dateString);
								DateTime hour = hourFormatter.parseDateTime(hourString);
								date = date.hourOfDay().setCopy(hour.getHourOfDay());
								date = date.minuteOfHour().setCopy(hour.getMinuteOfHour());

								entry.setDate(date.toDate());
							} catch (Exception e) {
								logger.error("Mauvais format de fichier: " + name
										+ " - Impossible de récupérer la date et l'heure");
								e.printStackTrace();
							}

							// Ajout du granule
							granules.add(entry);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return granules;
	}

	/**
	 * List all subFolders of the given folder
	 * 
	 * @param folder
	 * @return
	 */
	protected List<String> listCollectionFolders(String folderName) {

		List<String> results = new ArrayList<String>();
		List<File> subDirectories = FileUtils.getReccursiveSubDirectories(new File(folderName));
		for (File currentDirectory : subDirectories) {
			logger.debug("Test de " + currentDirectory);
			if (getCollectionFolderValidator().isCollectionFolder(currentDirectory)) {
				String aux = currentDirectory.getPath().replace(getRoot(), "");

				if (aux.startsWith(File.separator)) {
					aux = aux.replaceFirst(aux.substring(0, 1), "");
				}

				results.add(getCollectionPrefix() + File.separator + aux);
				logger.debug(" ---> Ajout collection " + getCollectionPrefix() + File.separator + aux);
			} else {
				logger.debug(" ---> KO");
			}
		}
		return results;
	}

	private String dateToFolderName(String date) {
		return date.replace("-", "");
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public CollectionFolderValidator getCollectionFolderValidator() {
		return collectionFolderValidator;
	}

	public void setCollectionFolderValidator(CollectionFolderValidator collectionFolderValidator) {
		this.collectionFolderValidator = collectionFolderValidator;
	}

	public void setCollectionPrefix(String collectionPrefix) {
		this.collectionPrefix = collectionPrefix;
	}

	public String getCollectionPrefix() {
		return collectionPrefix;
	}

	@Override
	public String getFirstFolder(String collection) {
		ArrayList<String> directories = FileUtils.listSubDirsNames(collection, getRoot());
		String result = "";
		if (directories.size() > 0) {
			Collections.sort(directories);
			result = directories.get(0);
		}
		return result;
	}

	@Override
	public String getLastFolder(String collection) {
		ArrayList<String> directories = FileUtils.listSubDirsNames(collection, getRoot());
		String result = "";
		if (directories.size() > 0) {
			Collections.sort(directories);
			result = directories.get(directories.size() - 1);
		}
		return result;
	}

	@Override
	public Properties getCollectionProperties(String collection) {
		Properties props = null;
		collection = collection.substring(collection.indexOf("/"));
		File collectionFolder = new File(getRoot() + File.separator + collection + File.separator);

		File[] found = collectionFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".properties");
			}
		});
		if (found != null && found.length > 0) {
			File propertiesFile = found[0];
			props = getProperties(propertiesFile);
		}
		return props;
	}

	private Properties getProperties(File propertiesFile) {
		InputStream inputStream = null;
		Properties prop = new Properties();
		try {
			inputStream = new FileInputStream(propertiesFile);
			prop.load(inputStream);
			inputStream.close();
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		return prop;
	}

}
