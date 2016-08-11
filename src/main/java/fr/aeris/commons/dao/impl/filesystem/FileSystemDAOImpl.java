package fr.aeris.commons.dao.impl.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.aeris.commons.dao.FileSystemDAO;

public class FileSystemDAOImpl implements FileSystemDAO {

	String baseDirectory = "";
	String imgBaseDirectory;

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory) {
		if (!baseDirectory.endsWith(File.separator)) {
			baseDirectory += File.separator;
		}
		this.baseDirectory = baseDirectory;
	}

	public String getImgBaseDirectory() {
		return imgBaseDirectory;
	}

	public void setImgBaseDirectory(String imgBaseDirectory) {
		this.imgBaseDirectory = imgBaseDirectory;
	}

	@Override
	public List<File> listFolder(String path) {
		List<File> result = new ArrayList<File>();
		File file = new File(baseDirectory + path);
		if (file.exists()) {
			List<String> files = new ArrayList<String>();
			files = Arrays.asList(file.list());
			for (String f : files) {
				result.add(new File(baseDirectory + path + f));
			}
		}
		return result;
	}

}
