package com.ashley.migration.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.ini4j.Profile.Section;

import com.ashley.migration.model.ExportDocument;
import com.ashley.migration.model.Mask;


/**
 * IniReader Class
 * 
 * In this class, all document and folder information will be read and added to the
 * ExportDocument or Mask Class objects. An ELO export contains two types of ini files,
 * the initial "ExpInfo.ini" file which contains useful mask information and the many
 * es8 files, which contain all the folder or document information. There is a also a
 * third file type, ESW, which is not relevant to this program.
 * 
 * @author Ashley Stonehall
 * @version 1.0
 * 
 */

public class IniReader {

	/**
	 * <p>
	 * expInfoMaskReader, in this method the initial ExpInfo.ini is read to get the exisiting mask
	 * information (including the name and any information that has been added to the fields)
	 * 
	 * @param maskObj Mask, an object that contains Keywording Mask information
	 * @param maskObjs Map &lt;String, Mask&gt; a map for the created Mask objects
	 * @param maskNames List &lt;String&gt;, a temporary list with all the old Keywording names 
	 * @param ini Wini, instance of the ini file reader api
	 * @param expInfo String, is the file location of the ExpInfo.ini file on the local PC
	 * 
	 * <p>Throws - InvalidFileFormatException 
	 * <p>Throws - IOException 
	 */
	public void expInfoMaskReader(Mask maskObj, Map<String, Mask> maskObjs, List<String> maskNames, Wini ini, String expInfo) {

		try {
			// Load the ExpInfo.ini
			ini = new Wini(new File(expInfo));
			// Find the [Masks] section
			Section mask = ini.get("MASKS");
			// Get mask names
			for(String name : mask.keySet()) {
				maskNames.add(mask.get(name));
			}
			// Create Mask objects
			for(String name : maskNames) {
				maskObjs.put(name, new Mask(name));
			}

		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add the extra text

		int index = 0;
		for(String mask : maskNames) {
			// Find the correct mask sections
			String key = "MASK" + Integer.toString(index);
			// list of the fields
			List<String> fieldNames = new ArrayList<String>();

			for(String sectionName : ini.keySet()) {
				if(sectionName.contains(key)) {

					Section section = ini.get(sectionName);

					for(String optionKey : section.keySet()) {
						if(optionKey.contains("MaskLine")) {
							String elomask = section.get(optionKey);
							// Filter out unimportant info
							elomask = elomask.substring(elomask.indexOf('"'), elomask.length());
							elomask = elomask.replaceFirst(",", "¶");
							elomask = elomask.substring(0, elomask.indexOf(','));
							elomask = elomask.replaceAll("\"", "");
							// add to the list
							fieldNames.add(elomask);
						}
					}
				}
			}
			// Get the correct mask object
			maskObj = maskObjs.get(mask);
			// Add its field names
			maskObj.setMaskFields(fieldNames);

			index++;
		}

	}

	/**
	 * <p>
	 * eloFileReader, this method reads the es8 files and gathers the important information
	 * such as its name, location path, whether it is a folder or document etc. and adds
	 * this information the the correct ExportDocument object
	 * 
	 * @param directoryName String, the path to be used by the File api
	 * @param exportDocumentObjs Map &lt;String, ExportDocument&gt;, a map of all the created ExportDocument objects
	 * @param exportFileNames List &lt;String&gt;, a list of every es8 file name. This will be used to find the correct object
	 * in the exportDocumentObjs Map
	 * @param eloFileNames List &lt;String&gt;, is a list of the correct folder and document names
	 * 
	 * <p>Throws - InvalidFileFormatException
	 * <p>Throws - IOException
	 */
	public void eloFileReader(String directoryName, Map<String, ExportDocument> exportDocumentObjs, List<String> exportFileNames, List<String> eloFileNames) {

		// Starts with the root directory and loops through all files and folders
		File directory = new File(directoryName);

		File[] flist = directory.listFiles();

		for(File file : flist) {
			if(file.isDirectory()) {
				// Loop through all sub-directories
				eloFileReader(file.getAbsolutePath(), exportDocumentObjs, exportFileNames, eloFileNames);
			} 
			else {
				// .es8 files contain all the Folder/Document information
				if(file.getName().contains(".es8")) {

					String docName = file.getName().substring(0, file.getName().indexOf('.'));
					// Export file names list, used to find the objects later
					exportFileNames.add(docName);

					// Create an object for each doc or folder
					exportDocumentObjs.put(docName, new ExportDocument(docName));
					// Get the newly created object
					ExportDocument docObj = exportDocumentObjs.get(docName);

					try {
						// read the .es8 file like an ini file
						Wini ini = new Wini(new File(file.getAbsolutePath()));

						// Important information to be added to the ExportDocument objects
						String eloName = ini.get("GENERAL", "SHORTDESC");
						String docType = ini.get("GENERAL", "DOCTYPE");
						String ext = ini.get("GENERAL", "DOCEXT");
						String date = ini.get("GENERAL", "DOCDATEISO");

						// Add original date
						docObj.setDate(date);

						// Add extension
						docObj.setExt("." + ext);

						// Add its ELO name and Doctype(MASK) to the object

						if(eloName.contains("/")) {
							eloName = eloName.replaceAll("/", "###");
						}
						docObj.setEloName(eloName);

						eloFileNames.add(eloName);

						docObj.setDocType(docType);
						// Add the original path
						docObj.setExportPath(file.getAbsolutePath());

						// Find and read the field information
						String keyInfo = "";
						for(String sectionName : ini.keySet()) { 
							if(sectionName.contains("KEY") && !sectionName.contains("KEY52")) {
								Section section = ini.get(sectionName);

								for(String optionKey : section.keySet()) { 
									keyInfo = keyInfo + section.get(optionKey) + "¶";
								}
							}

						}
						// Create the corrected text to be written to the "Extra Text section"
						String[] str = keyInfo.split("¶");
						String extraText = "";

						int index = 0;
						for(String temp : str) {
							// use modulus to altenate between new line and ¶
							if(index%2 == 0) {
								extraText += temp + "¶";
							} else {
								extraText += temp + "\n";
							}
							index++;
						}

						docObj.setExtraText(extraText);
						
						System.out.println("Document Object: " + docObj.getEloName() + ", " + docName + " Created");

					} catch (InvalidFileFormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	/**
	 * <p>
	 * extraText, this method corrects all of the existing Keywording information taken form the es8 files to a String
	 * that will later be added to the "Extra text" section of the new mask. It replaces Field names to something that
	 * makes more sense and splits the String up with new lines to be more readable
	 * 
	 * @param exportFileNames List &lt;String&gt;, a list of every es8 file name. This will be used to find the correct object
	 * @param docObj ExportDocument, will be the object retrieved from the exportDocumentObjs Map
	 * @param exportDocumentObjs Map &lt;String, ExportDocument&gt;, is a Map of all the document and folder objects
	 * @param maskObj Mask, will be the object retrieved from the maskObjs Map
	 * @param maskObjs Map &lt;String, Mask&gt;, a map with all of the Mask objects
	 */
	public void extraText(List<String> exportFileNames, ExportDocument docObj, Map<String, ExportDocument> exportDocumentObjs, Mask maskObj, Map<String, Mask> maskObjs) {

		for(String docName : exportFileNames) {
			// Get the correct ExportDocument Object
			docObj = exportDocumentObjs.get(docName);
			String correctText = docObj.getExtraText();

			// loop to correct the String
			maskObj = maskObjs.get(docObj.getDocType());
			for(String field : maskObj.getMaskFields()) {
				String[] key = field.split("¶");

				// Replaces the Mask field names into a more readable name for the user to understand
				if(correctText.contains(key[1])) {
					correctText = correctText.replaceAll(key[1], key[0]);
				}

			}
			docObj.setExtraText(correctText);
		}
	}

	/**
	 * <p>
	 * eloArchivePath, this method corrects the path folder or document names contained in the export path, to the ELO Archive
	 * path using the correct ELO names instead of export id's
	 * 
	 * @param exportFileNames List &lt;String&gt;, a list of the export paths
	 * @param docObj ExportDocument, will be the object retrieved from the exportDocumentObjs Map
	 * @param exportDocumentObjs Map &lt;String, ExportDocument&gt;, a Map of the ExportDocument objects
	 * @param eloFileNames List &lt;String&gt;, a list of all the correct names to be used 
	 */

	public void eloArchivePath(List<String> exportFileNames, ExportDocument docObj, Map<String, ExportDocument> exportDocumentObjs, List<String> eloFileNames) {
		// Correct the archive path name
		int index = 0;

		for(String name : exportFileNames) {

			docObj = exportDocumentObjs.get(name);

			String oldPath = docObj.getExportPath();

			index = 0;
			for(String oldName : exportFileNames) {
				String elo = eloFileNames.get(index);

				// Important to use replace method and not replaceAll, which results in some errors
				if(oldPath.contains(oldName)) {
					oldPath = oldPath.replace(oldName, elo);
				}
				index++;	
			}
			docObj.setArchivePath(oldPath);
			System.out.println("Added Path: " + docObj.getArchivePath() + ", to: " + docObj.getEloName());
		}

	}



}
