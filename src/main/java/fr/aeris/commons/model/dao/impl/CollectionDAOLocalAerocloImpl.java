package fr.aeris.commons.model.dao.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;

import fr.aeris.commons.model.dao.CollectionDAO;
import fr.aeris.commons.model.elements.Media;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.OpensearchUtils;
import fr.sedoo.commons.util.ListUtil;

public class CollectionDAOLocalAerocloImpl implements CollectionDAO {

	private final static String FILENAME_SEPARATOR = "_";
	private String root;

	@PostConstruct
	private void init() {
		root = "/home/klein/Documents/rootFolder/aeroclo";
	}

	@Override
	public List<String> getAllCollections() {
		List<String> collections = listSubFolders(root);
		return collections;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {

		Calendar start = OpensearchUtils.dateFromIso8601(startDate);
		Calendar end = OpensearchUtils.dateFromIso8601(endDate);

		List<OSEntry> granules = new ArrayList<OSEntry>();

		int daysBetween = OpensearchUtils.daysBetween(start.getTimeInMillis(), end.getTimeInMillis());

		Calendar tempCalendar;
		for (String collection : collections) {
			tempCalendar = OpensearchUtils.dateFromIso8601(startDate);
			for (int i = 0; i <= daysBetween; i++) {
				int year = tempCalendar.get(Calendar.YEAR);

				// Month + 1 car janvier = 0
				String month = String.format("%02d", tempCalendar.get(Calendar.MONTH) + 1);

				// Day
				String day = String.format("%02d", tempCalendar.get(Calendar.DAY_OF_MONTH));

				// Creation du chemin vers le repertoire
				StringBuilder folder = new StringBuilder();
				folder.append(root);
				folder.append(File.separator);
				folder.append(collection);
				folder.append(File.separator);
				folder.append(dateToFolderName(year + month + day));

				granules.addAll(listFiles(folder.toString(), tempCalendar));

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
	private List<OSEntry> listFiles(String folder, Calendar calendar) {
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
							entry.setMedia(new Media(testedFile.getAbsolutePath(), "QUICKLOOK"));

							String parentIdentifier = filenameParts.get(1);
							entry.setParentIdentifier(parentIdentifier);

							String type = filenameParts.get(2);
							entry.setType(type);

							// Extraction de la date et de l'heure int hour
							try {
								SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
								Date date = formatter.parse(filenameParts.get(0));
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
	protected List<String> listSubFolders(String folder) {
		List<String> results = new ArrayList<String>();
		File file = new File(folder);
		String[] names = file.list();
		for (String name : names) {
			File testedFile = new File(folder + File.separator + name);
			if (testedFile.isDirectory()) {
				String mainFolder = name;
				String[] subFiles = testedFile.list();
				if (subFiles.length > 0) {
					for (String subName : subFiles) {
						File subFile = new File(folder + File.separator + name + File.separator + subName);
						if (subFile.isDirectory()) {
							String subFolder = subName;
							results.add(mainFolder + "/" + subFolder);
						}
					}
				} else {
					results.add(mainFolder);
				}
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

}
