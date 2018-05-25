package com.ashley.migration.model;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * <p>
 * Objects that will contain all of the Keywording Mask information used by the folders
 * or documents contained in the export location
 * <p>
 * Uses Simple getter and setter methods to assign or retrieve the information
 * 
 * @author Ashley Stonehall
 * @version 1.0
 *
 */

public class Mask {
	
	
	// Mask name
	private String maskName = "";

	public Mask(String name) {
		this.maskName = name;
	}

	public String getMaskName() {
		return maskName;
	}

	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}
	
	// Mask field names
	private List<String> maskFields = new ArrayList<String>();

	public List<String> getMaskFields() {
		return maskFields;
	}

	public void setMaskFields(List<String> maskFields) {
		this.maskFields = maskFields;
	}

}
