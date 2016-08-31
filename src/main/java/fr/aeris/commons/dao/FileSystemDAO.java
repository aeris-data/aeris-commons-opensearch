package fr.aeris.commons.dao;

import java.io.File;
import java.util.List;

public interface FileSystemDAO {

	/**
	 * List all subfolders at the given path
	 * @param path
	 * @return
	 */
	public List<File> listFolder(String path);

}
