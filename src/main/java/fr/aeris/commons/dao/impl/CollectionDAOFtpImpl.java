package fr.aeris.commons.dao.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import fr.aeris.commons.dao.CollectionDAO;
import fr.aeris.commons.model.elements.Media;
import fr.aeris.commons.model.elements.OSEntry;
import fr.aeris.commons.utils.OpensearchUtils;
import fr.sedoo.commons.util.ListUtil;

public class CollectionDAOFtpImpl implements CollectionDAO {

	private final static String FILENAME_SEPARATOR = "_";

	private final static String CONNECT_MESSAGE = "FTP: Connexion au serveur";
	private final static String DISCONNECT_MESSAGE = "FTP: Deconnecté du serveur";
	private final static String ERROR_MESSAGE = "Operation failed. Server reply code: ";

	private final static String BASE_URL = "http://aeroclo.sedoo.fr/archive/";

	FTPClient ftpClient;

	private String host;
	private String path;
	private String user;
	private String pass;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public List<String> getAllCollections() {
		List<String> results = new ArrayList<String>();

		connect();

		try {
			// Listing des dossiers (nom des dosiers = nom des collections)
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile file : files) {
				String name = file.getName();
				if (file.isDirectory()) {
					String mainFolder = name;
					ftpClient.changeWorkingDirectory(name);
					FTPFile[] subFiles = ftpClient.listFiles();
					if (subFiles.length > 0) {
						for (FTPFile subFile : subFiles) {
							String subName = subFile.getName();
							if (subFile.isDirectory()) {
								String subFolder = subName;
								results.add(mainFolder + "/" + subFolder);
							}
						}
					} else {
						results.add(mainFolder);
					}
					ftpClient.changeWorkingDirectory("/");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		disconnect();

		return results;
	}

	@Override
	public List<OSEntry> findAll(List<String> collections, String startDate, String endDate) {
		List<OSEntry> granules = new ArrayList<OSEntry>();

		Calendar start = OpensearchUtils.dateFromIso8601(startDate);
		Calendar end = OpensearchUtils.dateFromIso8601(endDate);

		int daysBetween = OpensearchUtils.daysBetween(start.getTimeInMillis(), end.getTimeInMillis());

		Calendar tempCalendar;

		connect();

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
				folder.append("/");
				folder.append(collection);
				folder.append("/");
				folder.append(dateToFolderName(year + month + day));

				System.out.println("DOSSIER: " + folder.toString());

				try {
					ftpClient.changeWorkingDirectory(folder.toString());
					if (ftpClient.getReplyCode() != 550) {
						granules.addAll(listFiles(collection, tempCalendar));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Passage au jour suivant
				tempCalendar.add(Calendar.DATE, 1);
			}
		}
		disconnect();

		return granules;
	}

	private List<OSEntry> listFiles(String collection, Calendar calendar) {
		List<OSEntry> results = new ArrayList<OSEntry>();

		try {
			// Listing des dossiers (nom des dosiers = nom des collections)
			FTPFile[] files = ftpClient.listFiles();
			if (files != null) {
				for (FTPFile file : files) {
					String name = file.getName();
					if (!file.isDirectory()) {
						// Si le fichier est une image
						if (isAnImage(name)) {

							// Extraction de la collection et du type depuis le
							// nom du fichier
							String filenameWithoutExt = FilenameUtils.removeExtension(name);
							List<String> filenameParts = tokenizeFilename(filenameWithoutExt);

							OSEntry entry = new OSEntry();
							String link = BASE_URL + collection + "/" + filenameParts.get(0) + "/" + file.getName();
							entry.setMedia(new Media(link, "QUICKLOOK"));

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
							results.add(entry);
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;
	}

	private List<String> tokenizeFilename(String filename) {
		return ListUtil.fromSeparatedString(filename, FILENAME_SEPARATOR);
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
	 * Show the response code from the FTP server
	 * 
	 * @param ftpClient
	 */
	private static void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				System.out.println("FTP SERVER: " + aReply);
			}
		}
	}

	/**
	 * Connect and login into the FTP server
	 * 
	 * @return ftp connection
	 */
	public void connect() {
		ftpClient = new FTPClient();
		try {
			// Essai de connection
			System.out.println(CONNECT_MESSAGE);
			ftpClient.connect(host);
			showServerReply(ftpClient);
			int replyCode = ftpClient.getReplyCode();

			// Si la connection echoue
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				System.out.println(ERROR_MESSAGE + replyCode);
			} else {
				// Identification aupres du serveur
				ftpClient.login(user, pass);
				showServerReply(ftpClient);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disconnect and close connection to the FTP server
	 * 
	 * @param ftpClient
	 */
	public void disconnect() {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
			showServerReply(ftpClient);
			System.out.println(DISCONNECT_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Define if the passed file is an image
	 * 
	 * @param file
	 * @return true if it is an image file, false if not
	 * @throws IOException
	 */
	private boolean isAnImage(String filename) {
		List<String> imagesExt = Arrays.asList("png", "jpg", "bmp");
		String extension = FilenameUtils.getExtension(filename);
		if (imagesExt.contains(extension)) {
			return true;
		}
		return false;
	}

	private String dateToFolderName(String date) {
		return date.replace("-", "");
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
