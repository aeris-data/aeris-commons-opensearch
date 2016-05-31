package fr.aeris.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

}
