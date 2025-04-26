package com.chaoticsomeone.ModInfGen.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleInfo {
	private static final Pattern VARIABLE_PATTERN = Pattern.compile("^\\$([a-z][\\w\\-]*)");

	@Expose
	@SerializedName("module")
	private String moduleName;

	@Expose
	@SerializedName("source-root")
	private String sourceRoot;

	@Expose
	private Map<String, String> variables;

	@Expose
	@SerializedName("requires")
	private List<String> requirements;

	private List<String> exports;

	private List<OpensDeclaration> opens;

	@Expose
	private List<String> legacy;

	@Expose
	private Map<String, String> comments;

	private final List<String> modules = new ArrayList<>();

	private final Function<String, String> expandMapper = (s) -> {
		if (s.equals(".")) {
			return getModuleName();
		} else if (s.startsWith(".")) {
			return getModuleName() + s;
		} else {
			return s;
		}
	};

	public void scanForModules() {
		if (getSourceRoot() == null || getSourceRoot().isBlank()) {
			return;
		}

		File root = new File(getSourceRoot(), moduleName.replace(".", "/"));
		scanModule(root);
	}

	private void scanModule(File directory) {
		File root = new File(getSourceRoot());
		File[] files = directory.listFiles();
		modules.add(directory.getPath().replace(root.getPath() + "\\", "").replace("\\", ".").replace("/", "."));

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					scanModule(file);
				}
			}
		}
	}

	public void expandRoot() {
		this.exports = exports.stream().map(expandMapper).toList();

		for (OpensDeclaration open : opens) {
			open.setSourceModules(open.getSourceModules().stream().map(expandMapper).toList());
		}
	}

	public void validateVariables() throws IllegalArgumentException {
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			Matcher matcher = VARIABLE_PATTERN.matcher(entry.getKey());

			if (!matcher.matches()) {
				throw new IllegalArgumentException(String.format("Invalid variable name '%s'", entry.getKey()));
			}
		}
	}

	public void expandVariables() {
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			Pattern keyPattern = Pattern.compile(String.format("\\%s", entry.getKey()));
			String value = entry.getValue();

			Function<String, String> variableMapper = (s) -> keyPattern.matcher(s).replaceAll(value);

			setModuleName(variableMapper.apply(getModuleName()));
			setSourceRoot(variableMapper.apply(getSourceRoot()));
			setRequirements(getRequirements().stream().map(variableMapper).toList());
			setExports(getExports().stream().map(variableMapper).toList());
			setLegacy(getLegacy().stream().map(variableMapper).toList());

			for (String commentKey : comments.keySet()) {
				comments.put(commentKey, variableMapper.apply(comments.get(commentKey)));
			}

			for (OpensDeclaration open : opens) {
				open.setTargetModule(variableMapper.apply(open.getTargetModule()));
				open.setSourceModules(open.getSourceModules().stream().map(variableMapper).toList());
			}
		}
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getSourceRoot() {
		return sourceRoot;
	}

	public void setSourceRoot(String sourceRoot) {
		this.sourceRoot = sourceRoot;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public String getVariable(String key) {
		return variables.get(key);
	}

	public List<String> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<String> requirements) {
		this.requirements = requirements;
	}

	public List<String> getExports() {
		return exports;
	}

	public void setExports(List<String> exports) {
		this.exports = exports;
	}

	public List<OpensDeclaration> getOpens() {
		return opens;
	}

	public void setOpens(List<OpensDeclaration> opens) {
		this.opens = opens;
	}

	public List<String> getLegacy() {
		return legacy;
	}

	public void setLegacy(List<String> legacy) {
		this.legacy = legacy;
	}

	public Map<String, String> getComments() {
		return comments;
	}

	public void setComments(Map<String, String> comments) {
		this.comments = comments;
	}

	public String getComment(String key) {
		return comments.get(key);
	}

	public List<String> getModules() {
		return modules;
	}
}
