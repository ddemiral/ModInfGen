package com.chaoticsomeone.ModInfGen;

import com.chaoticsomeone.ModInfGen.model.ModuleInfo;
import com.chaoticsomeone.ModInfGen.model.OpensDeclaration;
import com.google.gson.Gson;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleInfoGenerator {
	private static final Pattern SL_COMMENT = Pattern.compile("^\\s*//.*");
	private static final Pattern ML_COMMENT = Pattern.compile("/\\*.*\\*/");
	private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

	private final Gson gson = new Gson();
	private final String configFilePath = "module-info.json5";
	private ModuleInfo moduleInfo;

	private boolean collapseWhitespaces;

	public ModuleInfoGenerator(boolean collapseWhitespaces) {
		this.collapseWhitespaces = collapseWhitespaces;
	}

	public ModuleInfoGenerator() {
		this(false);
	}

	public String generate() {
		moduleInfo = gson.fromJson(readJsonString(configFilePath), ModuleInfo.class);

		try {
			moduleInfo.validateVariables();
			moduleInfo.expandVariables();
			moduleInfo.expandRoot();
			moduleInfo.scanForModules();

			return generateModuleInfoContent(moduleInfo);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void generateTemplate() {
		File outputFile = new File(configFilePath);

		if (outputFile.exists()) {
			return;
		}

		SmartStringBuilder outputBuilder = new SmartStringBuilder();

		outputBuilder.appendLn("{");
		outputBuilder.alterIndent(1);

		outputBuilder.appendLn("\"module\": \"\",");
		outputBuilder.appendLn("\"source-root\": \"\",\n");

		outputBuilder.appendLn("\"variables\": {");
		outputBuilder.appendLn("},\n");

		outputBuilder.appendLn("\"requires\": [");
		outputBuilder.appendLn("],\n");

		outputBuilder.appendLn("\"exports\": [");
		outputBuilder.appendLn("],\n");

		outputBuilder.appendLn("\"opens\": [");
		outputBuilder.appendLn("],\n");

		outputBuilder.appendLn("\"legacy\": [");
		outputBuilder.appendLn("],\n");

		outputBuilder.appendLn("\"comments\": {");
		outputBuilder.alterIndent(1);
		outputBuilder.appendLn("\"header\": \"\",");
		outputBuilder.appendLn("\"footer\": \"\",");
		outputBuilder.appendLn("\"requires\": \"\",");
		outputBuilder.appendLn("\"exports\": \"\",");
		outputBuilder.appendLn("\"opens\": \"\",");
		outputBuilder.appendLn("\"legacy\": \"\"");
		outputBuilder.alterIndent(-1);
		outputBuilder.appendLn("}");

		outputBuilder.alterIndent(-1);
		outputBuilder.appendLn("}");

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
			bw.write(outputBuilder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void expandWildcard(SmartStringBuilder builder, String baseline, String s) {
		String postfix = s.substring(1);
		List<String> modules = moduleInfo.getModules().stream().filter(m -> m.endsWith(postfix)).toList();

		for (String module : modules) {
			builder.appendFmtLn(baseline.replaceFirst("%s", module));
		}
	}

	private void addLine(SmartStringBuilder builder, String line, String value) {
		if (value.startsWith("*")) {
			expandWildcard(builder, line, value);
		} else {
			builder.appendFmtLn(line, value);
		}
	}

	private String generateModuleInfoContent(ModuleInfo moduleInfo) {
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
				addLine(outputBuilder, "exports %s;", export);
			}
		});

		outputBuilder.appendSectionLn(() -> {
			outputBuilder.appendComment(moduleInfo.getComment("opens"));
			for (OpensDeclaration opens : moduleInfo.getOpens()) {
				String baseline = String.format("opens %%s to %s;", opens.getTargetModule());

				for (String sourceModule : opens.getSourceModules()) {
					addLine(outputBuilder, baseline, sourceModule);
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

		return outputBuilder.toString(true);
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

	public boolean isCollapseWhitespaces() {
		return collapseWhitespaces;
	}

	private void setCollapseWhitespaces(boolean collapseWhitespaces) {
		this.collapseWhitespaces = collapseWhitespaces;
	}

	private File getOutputFile() {
		return new File(moduleInfo.getSourceRoot(), "module-info.java");
	}

	public static void main(String[] args) {
		ModuleInfoGenerator generator = new ModuleInfoGenerator(false);

		boolean doGenerate = true;
		boolean isDryRun = false;

		for (String arg : args) {
			if (arg.equalsIgnoreCase("-w")) {
				generator.setCollapseWhitespaces(true);
			} else if (arg.equalsIgnoreCase("-n")) {
				generator.generateTemplate();
				doGenerate = false;
			} else if (arg.equalsIgnoreCase("--dry-run")) {
				isDryRun = true;
			}
		}

		if (doGenerate) {
			String output = generator.generate();

			if (!isDryRun) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(generator.getOutputFile()))) {
					bw.write(output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
