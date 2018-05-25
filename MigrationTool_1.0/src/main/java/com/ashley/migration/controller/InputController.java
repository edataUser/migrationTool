package com.ashley.migration.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ini4j.Wini;

import com.ashley.migration.model.ExportDocument;
import com.ashley.migration.model.Mask;

import de.elo.ix.client.IXConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * <p>This Class is will handle the information taken from the user and then send it to the relevant models and controllers.
 * It will also manipulate strings that need to be handled in different ways by different methods.
 * <br>example - to create a path in ELO the String needs to be, "root¶Sub Folder¶Another Folder" before it is used by the function.
 * <br>example - to create a document in ELO the String needs to be,  "root/Sub Folder/Another Folder" before it is used by the function.
 * 
 * @author Ashley
 * @version 1.0
 */
public class InputController {

	@FXML private Button startBtn;

	@FXML private TextField directoryFld, 
							urlFld, 
							userFld, 
							passwordFld, 
							maskIdFld;
	
	@FXML
	public void userInput(ActionEvent event) {
		
		String directoryName = directoryFld.getText().toString();
		String url = urlFld.getText().toString();
		String user = userFld.getText().toString();
		String password = passwordFld.getText().toString();
		String maskId = maskIdFld.getText().toString();

		if(!(directoryName.isEmpty()) 
				&& !(url.isEmpty())
				&& !(user.isEmpty())
				&& !(password.isEmpty())
				&& !(maskId.isEmpty())) {


			///////////////////////////
			// Mask variables /////////

			// Initialize the mask obj
			Mask maskObj = null;
			// Initialize the mask object map
			Map<String, Mask> maskObjs = new TreeMap<String, Mask>();
			// A list that will temporarily hold the mask names
			List<String> maskNames = new ArrayList<String>();

			///////////////////////////
			// Document Variables /////

			// Initialize the document object
			ExportDocument docObj = null;
			// A map of document objects
			Map<String, ExportDocument> exportDocumentObjs = new TreeMap<String, ExportDocument>();
			// A list of file names, usesd to find the object in the map
			List<String> exportFileNames = new ArrayList<String>();

			// A list of the correct ELO names
			List<String> eloFileNames = new ArrayList<String>();

			///////////////////////////
			// ini and export info ////

			// root of the export directory
			String root = directoryName.substring(0, directoryName.lastIndexOf('\\'));

			root = root.replaceAll("\\\\", "\\\\\\\\");
			root = root + "\\\\";

			// Ini file reader
			Wini ini = null; 

			// The main export ini location
			String expInfo = directoryName + "\\ExpInfo.ini";

			//////////////////////////////////
			// ELO login information /////////

			EloLogin login = new EloLogin();
			IXConnection conn = null;

			///////////////////////////////////
			// Reads the initial expinfo.ini //

			IniReader iniInfo = new IniReader();
			// Gets the mask objects, names and field names expinfo.ini
			System.out.println(">>> Reading the ExpInfo.ini...");
			iniInfo.expInfoMaskReader(maskObj, maskObjs, maskNames, ini, expInfo);

			System.out.println(">>> Reading the es8 files and creating document objects...");
			// Create document objects and read important info from the es8 files
			iniInfo.eloFileReader(directoryName, exportDocumentObjs, exportFileNames, eloFileNames);

			System.out.println(">>> Adding extra text to document objects");
			// Add the extra text to the document Objects
			iniInfo.extraText(exportFileNames, docObj, exportDocumentObjs, maskObj, maskObjs);

			System.out.println(">>> Adding ELO archive path to document objects");
			// Add the correct ELO Archive path to document objects
			iniInfo.eloArchivePath(exportFileNames, docObj, exportDocumentObjs, eloFileNames);

			///////////////////////////////////
			// ELO Login //////////////////////
			conn = login.eloLogin(url, user, password);
			System.out.println("Ticket: " + conn.getLoginResult().getClientInfo().getTicket() + "\n");


			int folderCount = 0;
			// Create the folder structure
			for(String name : exportFileNames) {

				docObj = exportDocumentObjs.get(name);

				// depending on the language setting at the time of import
				if(docObj.getDocType().equals("Folder") || docObj.getDocType().equals("Ordner")) {

					String archivePath = docObj.getArchivePath().replaceAll(root, "");

					//ELO needs ¶ character to split the path up into separate folders
					archivePath = archivePath.replaceAll("\\\\", "¶");
					archivePath = archivePath.replaceAll(".es8", "");

					archivePath = archivePath.replaceAll("/", "### ");

					// Create the folder paths
					login.createFolderArchive(conn, archivePath, docObj);
					System.out.println(docObj.getEloName() + " - Created!: " + archivePath);
					folderCount++;
				}
			}

			// Logout and back in
			conn.close();
			conn = login.eloLogin(url, user, password);
			System.out.println("Ticket: " + conn.getLoginResult().getClientInfo().getTicket() + "\n");

			int docCount = 0;
			// Add the documents
			for(String name : exportFileNames) {
				docObj = exportDocumentObjs.get(name);
				if(!(docObj.getDocType().equals("Folder")) && !(docObj.getDocType().equals("Ordner"))) { 

					String archiveDocPath = docObj.getArchivePath();
					archiveDocPath = archiveDocPath.substring(0,archiveDocPath.lastIndexOf("\\"));

					archiveDocPath = archiveDocPath.replaceAll("/", "### ");

					archiveDocPath = "/" + archiveDocPath.replaceAll(root, "").replaceAll("\\\\", "/");

					String fileLocation = docObj.getExportPath();
					fileLocation = fileLocation.replaceAll(".es8", docObj.getExt());

					String docName = docObj.getEloName();
					String extraText = docObj.getExtraText().replaceAll("¶", " : ");
					String oldMask = docObj.getDocType();

					File fileCheck = new File(fileLocation);
					if(fileCheck.exists()) {
						login.createDocumentsArchive(conn, fileLocation, archiveDocPath, docName, extraText, maskId, oldMask, docObj);
						System.out.println(docObj.getEloName() + " - Document created!: " + archiveDocPath + " >>>> DOC Location: " + docObj.getExportPath());
						docCount++;
					}

				}
			}

			//////////////////////////////////////////////////////////////

			// Import is complete
			System.out.println("#######################");
			System.out.println("### Import Complete ###");
			System.out.println("#######################");

			///////////////////////////////////

			conn.close();

			conn = login.eloLogin(url, user, password);
			System.out.println("Ticket: " + conn.getLoginResult().getClientInfo().getTicket() + "\n");

			///////////////////////////////////
			// File name correction ///////////

			System.out.println("Correcting folder names....");

			login.folderNameCorrection(conn);

			System.out.println("Name changes complete!");

			conn.close();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Finished!");
			alert.setHeaderText("Import is complete.");
			alert.setContentText("Folders Total: " + folderCount + "\nDocuments Total: " + docCount);

			alert.showAndWait();

			System.exit(0);

		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Unfilled fields");
			alert.setContentText("Make sure all fields are filled before pressing start!");
			alert.showAndWait();
		}

	}

}
