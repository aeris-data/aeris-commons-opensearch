package fr.aeris.commons.dao.impl.quicklooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.opensearch.Media;
import fr.aeris.commons.model.elements.opensearch.OSEntry;
import fr.aeris.commons.utils.OpensearchUtils;

public class CollectionDAOLocalSatmosImpl implements CollectionDAO {

	private final static String FILENAME_SEPARATOR = "_";

	private String root;

	@PostConstruct
	private void init() {
		root = "/home/klein/Documents/rootFolder";
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
				folder.append("quicklooks");
				folder.append(File.separator);
				folder.append(year);
				folder.append(File.separator);
				folder.append(month);
				folder.append(File.separator);
				folder.append(day);

				// Ajout des granules au resultat et passage au jour suivant
				granules.addAll(listFiles(folder.toString(), tempCalendar));
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
							OSEntry entry = new OSEntry();
							entry.setMedia(new Media(testedFile.getAbsolutePath(), "QUICKLOOK"));

							// Extraction de la collection et du type depuis le
							// nom du fichier
							String filenameWithoutExt = FilenameUtils.removeExtension(name);
							List<String> filenameParts = Arrays
									.asList(StringUtils.split(filenameWithoutExt, FILENAME_SEPARATOR));

							String parentIdentifier = filenameParts.get(0);
							entry.setParentIdentifier(parentIdentifier);

							String type = filenameParts.get(1);
							entry.setType(type);

							// Extraction de la date et de l'heure
							int hour = Integer.valueOf(filenameParts.get(2)) / 100;
							calendar.set(Calendar.HOUR_OF_DAY, hour);
							entry.setDate(calendar.getTime());

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
	private List<String> listSubFolders(String folder) {
		File file = new File(folder);
		String[] names = file.list();
		List<String> subFolders = new ArrayList<String>();
		for (String name : names) {
			File testedFile = new File(folder + File.separator + name);
			if (testedFile.isDirectory()) {
				subFolders.add(name);
			}
		}
		return subFolders;
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
