package org.jahia.tools.contentgenerator;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jahia.tools.contentgenerator.bo.PageBO;

/**
 * Class used to write data into output files
 * @author Guillaume Lucazeau
 *
 */
public class OutputService {

	public void initOutputFile(File f) throws IOException {	
		// if file already exist, we empty it
		FileUtils.writeStringToFile(f, "");		
	}
	
	public void appendStringToFile(File f, String s) throws IOException {
		OutputStreamWriter fwriter = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
        BufferedWriter fOut = new BufferedWriter(fwriter);
		fOut.write(s);
		fOut.close();
	}

	public void appendPagesToFile(File f, List<PageBO> listePages) throws IOException {
		for (Iterator<PageBO> iterator = listePages.iterator(); iterator.hasNext();) {
			PageBO page = (PageBO) iterator.next();
			appendPageToFile(f, page);
		}
	}

	public void appendPageToFile(File f, PageBO page) throws IOException {
		appendStringToFile(f, page.toString());
	}
	
	public void appendPathToFile(File f, List<String> paths) throws IOException {
		for (Iterator<String> iterator = paths.iterator(); iterator.hasNext();) {
			String path = (String) iterator.next();
			path = path + ",";
			appendStringToFile(f, path);
		}		
	}
}
