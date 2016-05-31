package fr.aeris.commons.dao.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.Media;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.CollectionFolderValidator;
import fr.aeris.commons.utils.FileUtils;
import fr.aeris.commons.utils.OpensearchUtils;
import fr.sedoo.commons.util.ListUtil;
import fr.sedoo.commons.util.StringUtil;

public class LocalFileSystemSingleRootImpl implements CollectionDAO {

	private final static String FILENAME_SEPARATOR = "_";
	private String root;
	private CollectionFolderValidator collectionFolderValidator;
	private String collectionPrefix;

	Logger logger = LoggerFactory.getLogger(LocalFileSystemSingleRootImpl.class);

	public LocalFileSystemSingleRootImpl() {
		setCollectionFolderValidator(new SedooCampaignCollectionFolderValidator());
	}

	// @PostConstruct
	// private void init() {
	// //setRoot("/home/klein/Documents/rootFolder/aeroclo");
	// }

	@Override
	public List<String> getAllCollections() {
		List<String> collections = listCollectionFolders(getRoot());
		return collections;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {

		if (StringUtil.isEmpty(getRoot())) {
			return new ArrayList<>();
		}

		Calendar start = OpensearchUtils.dateFromIso8601(startDate);
		Calendar end = OpensearchUtils.dateFromIso8601(endDate);

		List<OSEntry> granules = new ArrayList<OSEntry>();

		int daysBetween = OpensearchUtils.daysBetween(start.getTimeInMillis(), end.getTimeInMillis());

		Calendar tempCalendar;
		for (String collection : collections) {

			int aux = collection.indexOf(File.separator);

			collection = collection.substring(aux + 1);

			tempCalendar = OpensearchUtils.dateFromIso8601(startDate);
			for (int i = 0; i <= daysBetween; i++) {
				int year = tempCalendar.get(Calendar.YEAR);

				// Month + 1 car janvier = 0
				String month = String.format("%02d", tempCalendar.get(Calendar.MONTH) + 1);

				// Day
				String day = String.format("%02d", tempCalendar.get(Calendar.DAY_OF_MONTH));

				// Creation du chemin vers le repertoire
				StringBuilder folder = new StringBuilder();
				folder.append(getRoot());
				folder.append(File.separator);
				folder.append(collection);
				folder.append(File.separator);
				folder.append(dateToFolderName(year + month + day));

				granules.addAll(listFiles(collection, folder.toString(), tempCalendar));

				// Passage au jour suivant
				tempCalendar.add(Calendar.DATE, 1);
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
	private List<OSEntry> listFiles(String collection, String folder, Calendar calendar) {
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
						if (isAnImage(testedFile)) {
							// Extraction de la collection et du type depuis le
							// nom du fichier
							String filenameWithoutExt = FilenameUtils.removeExtension(name);
							List<String> filenameParts = tokenizeFilename(filenameWithoutExt);

							OSEntry entry = new OSEntry();
							String mediaPath = "http://opensearch.sedoo.fr/rest/images/getImage?image="
									+ testedFile.getAbsolutePath();
							entry.setMedia(new Media(mediaPath, "QUICKLOOK"));

							// String parentIdentifier = filenameParts.get(1);
							entry.setParentIdentifier(collectionPrefix + File.separator + collection);

							String type = filenameParts.get(2);
							entry.setType(type);

							// Extraction de la date et de l'heure int hour
							try {
								SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
								String dateString = filenameParts.get(0).substring(0, 8);
								Date date = formatter.parse(dateString);
								calendar.setTime(date);
								entry.setDate(calendar.getTime());
							} catch (Exception e) {
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

	/**
	 * Tokenize passed string with FILENAME_SEPARATOR
	 * 
	 * @param filename
	 * @return
	 */
	private List<String> tokenizeFilename(String filename) {
		return ListUtil.fromSeparatedString(filename, FILENAME_SEPARATOR);
	}

	/**
	 * Define if the passed file is an image
	 * 
	 * @param file
	 * @return true if it is an image file, false if not
	 * @throws IOException
	 */
	private boolean isAnImage(File file) throws IOException {
		String absolutePath = file.getAbsolutePath();
		String folder = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
		String filename = file.getName();
		String fileType = java.nio.file.Files.probeContentType(FileSystems.getDefault().getPath(folder, filename));
		return fileType.contains("image/") ? true : false;
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

}
