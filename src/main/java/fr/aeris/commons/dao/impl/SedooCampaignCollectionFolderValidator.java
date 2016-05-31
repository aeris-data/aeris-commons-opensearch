package fr.aeris.commons.dao.impl;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import fr.aeris.commons.utils.CollectionFolderValidator;
import fr.aeris.commons.utils.FileUtils;

public class SedooCampaignCollectionFolderValidator implements CollectionFolderValidator {

	@Override
	public boolean isCollectionFolder(File folder) {
		List<File> subFolders = FileUtils.getDirectSubDirectories(folder);
		if (subFolders.isEmpty()) {
			return false;
		}

		for (int i = 0; i < subFolders.size(); i++) {
			File currentFolder = subFolders.get(i);
			if (!isMatching(currentFolder.getName())) {
				return false;
			}
		}

		return true;
	}

	private boolean isMatching(String name) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			format.parse(name);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

}
