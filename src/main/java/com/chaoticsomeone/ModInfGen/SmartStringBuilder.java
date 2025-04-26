package com.chaoticsomeone.ModInfGen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartStringBuilder {
	private final StringBuilder sb = new StringBuilder();
	private int indent = 0;

	public SmartStringBuilder() {

	}

	public void append(Object o) {
		sb.append("\t".repeat(indent));
		sb.append(o);
	}

	public void appendLn(Object o) {
		append(o + "\n");
	}

	public void appendLn() {
		append("\n");
	}

	public void appendFmt(String format, Object... args) {
		append(String.format(format, args));
	}

	public void appendFmtLn(String format, Object... args) {
		appendFmt(String.format(format + "\n", args));
	}

	public void appendSectionLn(Runnable section) {
		appendSection(section);
		appendLn();
	}

	public void appendSection(Runnable section) {
		section.run();
	}

	public void appendComment(String comment) {
		appendComment(comment, 0, 1);
	}

	public void appendComment(String comment, int newLines) {
		appendComment(comment, newLines, 1);
	}

	public void appendComment(String comment, int newLinesBefore, int newLinesAfter) {
		if (comment != null && !comment.isBlank()) {
			append("\n".repeat(newLinesBefore));
			appendFmt("// %s", comment);
			append("\n".repeat(newLinesAfter));
		}
	}

	public void alterIndent(int offset) {
		indent = Math.max(0, indent + offset);
	}

	@Override
	public String toString() {
		Matcher matcher = Pattern.compile("\t+").matcher(sb.toString());
		return matcher.replaceAll("\t");
	}
}
