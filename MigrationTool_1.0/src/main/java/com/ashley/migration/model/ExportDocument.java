package com.ashley.migration.model;


/**
 * ExportDocument Class
 * 
 * <p>
 * Objects that will contain all of the information about folders or documents
 * contained in the exported folder location
 * <p>
 * Uses Simple getter and setter methods to assign or retrieve the information
 * 
 * @author Ashley Stonehall
 * @version 1.0
 *
 */

public class ExportDocument {
	

	// export name
	private String exportName = "";
	
	public ExportDocument(String name) {
		this.exportName = name;
	}
	
	public String getExportName() {
		return exportName;
	}

	public void setExportName(String exportName) {
		this.exportName = exportName;
	}
	
	// elo archive name
	private String eloName = "";

	public String getEloName() {
		return eloName;
	}

	public void setEloName(String eloName) {
		this.eloName = eloName;
	}
	
	// elo doctype
	private String docType = "";

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	// elo extra text
	private String extraText = "";

	public String getExtraText() {
		return extraText;
	}

	public void setExtraText(String extraText) {
		this.extraText = extraText;
	}
	
	// export path
	private String exportPath = "";

	public String getExportPath() {
		return exportPath;
	}

	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}
	
	// elo Archive Path
	private String archivePath = "";

	public String getArchivePath() {
		return archivePath;
	}

	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}
	
	// Document extension
	private String ext = "";

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}
	
	// Document upload date
	private String date = "";

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
	
	
}
