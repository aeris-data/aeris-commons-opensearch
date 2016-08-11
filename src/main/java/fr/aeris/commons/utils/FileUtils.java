package fr.aeris.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static List<File> getDirectSubDirectories(File file) {

		logger.debug("DirectSubDirectories sur " + file.getPath());
		if (!file.exists()) {
			logger.debug(file.getPath() + " n'existe pas");
			return new ArrayList<>();
		}
		logger.debug(file.getPath() + " existe");
		List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (logger.isDebugEnabled()) {
					logger.debug(f.getPath() + " est un repertoire :" + f.isDirectory());
				}
				return f.isDirectory();
			}
		}));
		return new ArrayList<File>(subdirs);

	}

	public static List<File> getReccursiveSubDirectories(File file) {
		List<File> subdirs = getDirectSubDirectories(file);
		subdirs = new ArrayList<File>(subdirs);

		List<File> deepSubdirs = new ArrayList<File>();
		for (File subdir : subdirs) {
			deepSubdirs.addAll(getReccursiveSubDirectories(subdir));
		}
		subdirs.addAll(deepSubdirs);
		return subdirs;
	}

	public static ArrayList<String> listSubDirsNames(String collection, String collectionRoot) {
		ArrayList<String> results = new ArrayList<String>();
		collection = collection.substring(collection.indexOf("/"));
		File file = new File(collectionRoot + "/" + collection);
		if (file.exists()) {
			String[] directories = file.list();
			if (directories != null) {
				for (String dir : directories) {
					File testedFile = new File(collectionRoot + "/" + collection + "/" + dir);
					if (testedFile.isDirectory()) {
						results.add(dir);
					}
				}
			}
		}
		return results;
	}

	/**
	 * Define if the passed file is an image
	 * 
	 * @param file
	 * @return true if it is an image file, false if not
	 * @throws IOException
	 */
	public static boolean isAnImage(File file) throws IOException {
		List<String> imagesExt = new ArrayList<String>();
		imagesExt.addAll(Arrays.asList("jpg", "png", "gif", "bmp"));
		String filename = file.getName();
		for (String ext : imagesExt) {
			if (FilenameUtils.getExtension(filename).equalsIgnoreCase(ext)) {
				return true;
			}
		}
		return false;
	}

}
