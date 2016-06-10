package fr.aeris.commons.dao;

import java.io.File;
import java.util.List;

public interface FileSystemDAO {

	String BEAN_NAME = "FileSystemDao";

	public List<File> listFolder(String path);

}
