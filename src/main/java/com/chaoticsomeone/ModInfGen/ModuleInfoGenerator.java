package com.chaoticsomeone.ModInfGen;

import com.chaoticsomeone.ModInfGen.model.ModuleInfo;
import com.chaoticsomeone.ModInfGen.model.OpensDeclaration;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleInfoGenerator {
	private static final Pattern SL_COMMENT = Pattern.compile("^\\s*//.*");
	private static final Pattern ML_COMMENT = Pattern.compile("/\\*.*\\*/");
	private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

	private final Gson gson = new Gson();
	private final String configFilePath = "module-info.json5";
	private final ModuleInfo moduleInfo;

	private final boolean collapseWhitespaces;

	public ModuleInfoGenerator(boolean collapseWhitespaces) {
		this.collapseWhitespaces = collapseWhitespaces;

		moduleInfo = gson.fromJson(readJsonString(configFilePath), ModuleInfo.class);

		try {
			moduleInfo.validateVariables();
			moduleInfo.expandVariables();
			moduleInfo.expandRoot();

			System.out.println(generateModuleInfoContent());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public ModuleInfoGenerator() {
		this(false);
	}

	private String generateModuleInfoContent() {
		SmartStringBuilder outputBuilder = new SmartStringBuilder();

		outputBuilder.appendComment(moduleInfo.getComment("header"), 0, 2);

		outputBuilder.appendFmtLn("module %s {", moduleInfo.getModuleName());
		outputBuilder.alterIndent(1);

		outputBuilder.appendSectionLn(() -> {
			outputBuilder.appendComment(moduleInfo.getComment("requires"));
			for (String requirement : moduleInfo.getRequirements()) {
				outputBuilder.appendFmtLn("requires %s;", requirement);
			}
		});

		outputBuilder.appendSectionLn(() -> {
			outputBuilder.appendComment(moduleInfo.getComment("exports"));
			for (String export : moduleInfo.getExports()) {
				outputBuilder.appendFmtLn("exports %s;", export);
			}
		});

		outputBuilder.appendSectionLn(() -> {
			outputBuilder.appendComment(moduleInfo.getComment("opens"));
			for (OpensDeclaration opens : moduleInfo.getOpens()) {
				String baseLine = String.format("opens %s to %s;", "%s", opens.getTargetModule());

				for (String sourceModule : opens.getSourceModules()) {
					outputBuilder.appendFmtLn(baseLine, sourceModule);
				}
			}
		});

		outputBuilder.appendSection(() -> {
			outputBuilder.appendComment(moduleInfo.getComment("legacy"));
			for (String legacyLine : moduleInfo.getLegacy()) {
				String end = legacyLine.endsWith(";") ? "" : ";";
				outputBuilder.appendLn(legacyLine + end);
			}
		});

		outputBuilder.alterIndent(-1);
		outputBuilder.append("}");

		outputBuilder.appendComment(moduleInfo.getComment("footer"), 2);

		return outputBuilder.toString();
	}

	private String readJsonString(String path) {
		StringBuilder jsonBuilder = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;

			while ((line = br.readLine()) != null) {
				// Filter out single line comments for compat between json5 and json
				if (SL_COMMENT.matcher(line).matches()) {
					continue;
				}

				if (collapseWhitespaces) {
					Matcher matcher = MULTI_WHITESPACE.matcher(line);
					String reducedLine = matcher.replaceAll(" ");
					jsonBuilder.append(reducedLine);
				} else {
					jsonBuilder.append(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// filter out all multi-line comments for compat
		Matcher matcher = ML_COMMENT.matcher(jsonBuilder.toString());
		return matcher.replaceAll("");
	}


	public static void main(String[] args) {
		boolean collapseWhitespaces = false;

		for (String arg : args) {
			if (arg.equalsIgnoreCase("-w")) {
				collapseWhitespaces = true;
			}
		}

		ModuleInfoGenerator generator = new ModuleInfoGenerator(collapseWhitespaces);

	}
}
