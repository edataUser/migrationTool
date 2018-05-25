package com.ashley.migration.controller;

import java.io.File;


import com.ashley.migration.model.ExportDocument;

import byps.RemoteException;
import de.elo.ix.client.*;

/** 
 * <p>
 * Elo Login Class,<br>
 * 
 * This class is how the program interacts with ELO Professional
 * It contains methods to Login, create a folder structure, 
 * load documents and correct the folder names (if they contain a '/' character originally)
 * 
 * @author Ashley Stonehall
 * @version 1.00
 * 
 */
public class EloLogin {

    /**
    * <p>
    * Login to ELO Professional
    *
    * <p>
    * This method logs in with a user name and password that was given at the start of the
    * program
    *
    * @param url String  is the url of the ELO Professional Repository
    * @param user String  is the ELO Professional user name
    * @param password String is the ELO Professional users password
    * @return Returns IXConnection variable which contains ELO Professional login information
    * 
    * <p>Throws - RemoteException 
    *  
    */
	public IXConnection eloLogin(String url, String user, String password) {
		
		IXConnFactory connFact = new IXConnFactory(url, "Migration", "1.0");
		IXConnection conn = null;
		
		try {
			// conn is the variable that contains the users access information to ELO
			conn = connFact.create(user, password, "edata", null);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * <p>
	 * Create a folder structure in the ELO Professional archive 
	 * 
	 * <p>
	 * This method creates the folder structure within ELO professional. It takes a String such as 
	 * "Root Folder¶Sub Folder¶Another Folder" and splits it where the "¶" character is found.
	 * <br>
	 * == Root Folder<br>
	 * 		|<br>
	 * 		=== Sub Folder<br>
	 * 					|<br>
	 * 					==== Another Folder<br>
	 * 
	 * <p>It automatically ignores folders that already exist to ignore conflicts or errors.
	 * 
	 * 
	 * @param conn IXConection, variable 
	 * @param archivePath String, is the complete path to be created in ELO using the following
	 * format - "Folder¶Child Folder¶Another Folder". ELO will split the string into seperate
	 * folders using the "¶" character.
	 * @param docObj ExportDocument, is an object created from the ExportDocument Class
	 * 
	 * <p>Throws - RemoteException  
	 */
	public void createFolderArchive(IXConnection conn, String archivePath, ExportDocument docObj) {
		
		System.out.println(archivePath);
		
		// split the path into individual folders
		String[] sordNames = archivePath.split("¶");
		
		// create the sord array using the sordNames
		Sord[] sords = new Sord[sordNames.length];
		
		for(int i = 0; i < sords.length; i++) {
			try {
				// "1" is the mask id for "Folder"
				sords[i] = conn.ix().createSord(null, "1", SordC.mbAll);
				// assigns the new object the correct name
				sords[i].setName(sordNames[i]);
				
			} catch (RemoteException e) {
				e.printStackTrace();
				
				System.out.println(sords);
			}
		}
		
		// Suppress needed as "id" is actually used in the "try" part of this method
		@SuppressWarnings("unused")
		int[] ids = null;
		
		try {
			// "1" is the archive root id
			ids = conn.ix().checkinSordPath("1", sords, new SordZ(SordC.mbAll));

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * <p>
	 * Upload documents to the created paths in the ELO Professional Archive
	 * 
	 * <p>
	 * This method will find the correct folder location in ELO and then upload the relevant
	 * documents to it with its new Keywording mask and information from the old one.
	 * 
	 * @param conn IXConection, variable
	 * @param fileLocation String, pointing to the file location on the local PC
	 * @param archiveDocPath String, the complete ELO Professional archive path where the
	 * document should be uploaded to. 
	 * @param docName String, the name of the document
	 * @param extraText String, the old Keywording information is contained in this String. It will be added
	 * to the "Extra Text" section.
	 * @param maskId String, the Keywording mask id the document should recieve
	 * @param oldMask String, the name of the original Keywording Mask. This will be added to the first field of the 
	 * new mask for reference
	 * @param docObj ExportDocument, is an object created from the ExportDocument Class
	 * 
	 * <p>Throws - RemoteException  
	 */
		public void createDocumentsArchive(IXConnection conn, String fileLocation, String archiveDocPath, String docName, String extraText, String maskId, String oldMask, ExportDocument docObj) {
			
			EditInfo ed = null;
			String parentId = "";
			File file = new File(fileLocation);
			
			try {

				ed = conn.ix().checkoutSord("ARCPATH:/" + archiveDocPath, EditInfoC.mbOnlyId, LockC.NO);
				
				int objId = ed.getSord().getId();
				// Needs to be converted to String for later use
				parentId = Integer.toString(objId);
				
				// Load the documents
				// Step 1
				ed = conn.ix().createDoc(parentId, maskId, null, EditInfoC.mbSordDocAtt);
				
				Sord sord = ed.getSord();
				sord.setName(docName);
				
				// Set the orginal date
				sord.setXDateIso(docObj.getDate());;
				
				ObjKey[] objKeys = sord.getObjKeys();
				
				// Add the old mask to the first field
				objKeys[0].setData(new String[]{oldMask});
				
				// Adds the information from the keywording forms to the "Extra Text section"
				sord.setDesc(extraText);
				
				// Step 2
				Document doc = new Document();
				DocVersion dv = new DocVersion();
				dv.setPathId(sord.getPath());
				dv.setEncryptionSet(sord.getDetails().getEncryptionSet());
				dv.setExt(conn.getFileExt(file.toString()));
				
				doc.setDocs(new DocVersion[] {dv});
				doc = conn.ix().checkinDocBegin(doc);
				
				// Step 3
				dv = doc.getDocs()[0];
				String url = dv.getUrl();
				String uploadResult = conn.upload(url, file);
				dv.setUploadResult(uploadResult);
				
				// Step 4
				doc = conn.ix().checkinDocEnd(sord, SordC.mbAll, doc, LockC.NO);
				
				dv = doc.getDocs()[0];		
								
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		
		}

		/**
		 * <p>
		 * This method is to be used when all folders and documents have been created in 
		 * ELO Professional. During the creation of Folder paths or documents, any name with a "/" character 
		 * will be changed to "###", otherwise the name itself gets split into folders which results in errors. This method changes 
		 * the name back to the original form
		 * 
		 * @param conn IXConection, variable
		 * 
		 * <p>Throws - RemoteException 
		 */
		public void folderNameCorrection(IXConnection conn) {
			
			FindInfo findInfo = new FindInfo();
			FindByIndex findByIndex = new FindByIndex();
			findByIndex.setName("###");
			findInfo.setFindByIndex(findByIndex);
			
			FindResult findResult;
			try {
				findResult = conn.ix().findFirstSords(findInfo, 9999, SordC.mbAll);
				
				Sord[] sord = findResult.getSords();
				
				EditInfo ed = null;
				
				for(Sord folderSordWithOldName : sord) {
					
					int objId = folderSordWithOldName.getId();
					
					String folder = Integer.toString(objId);
					
					ed = conn.ix().checkoutSord(folder, EditInfoC.mbSord, LockC.NO);
					
					Sord folderSordWithCorrectName = ed.getSord();
					
					folderSordWithCorrectName.setName(folderSordWithCorrectName.getName().replaceAll("### ", "/ "));
					
					conn.ix().checkinSord(folderSordWithCorrectName, SordC.mbAll, LockC.YES);
				}
			
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	
}
