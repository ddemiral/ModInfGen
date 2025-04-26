package com.chaoticsomeone.ModInfGen.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpensDeclaration {
	@Expose
	@SerializedName("target")
	private String targetModule;

	@Expose
	@SerializedName("modules")
	private List<String> sourceModules;

	public String getTargetModule() {
		return targetModule;
	}

	public void setTargetModule(String targetModule) {
		this.targetModule = targetModule;
	}

	public List<String> getSourceModules() {
		return sourceModules;
	}

	public void setSourceModules(List<String> sourceModules) {
		this.sourceModules = sourceModules;
	}
}
