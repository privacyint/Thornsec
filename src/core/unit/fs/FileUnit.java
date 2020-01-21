/*
 * This code is part of the ThornSec project.
 *
 * To learn more, please head to its GitHub repo: @privacyint
 *
 * Pull requests encouraged.
 */
package core.unit.fs;

import java.util.ArrayList;
import java.util.Collection;

import core.unit.SimpleUnit;

/**
 * This Unit is designed for whole files.
 *
 * It will perform tests on any property which is set.
 */
public class FileUnit extends SimpleUnit {

	private final String path;
	private final Collection<String> lines;

	/**
	 * Unit for writing out a whole file, with custom fail message
	 *
	 * @param name         Name of unit test
	 * @param precondition Precondition unit test name
	 * @param path         Path to the file
	 * @param message      Custom fail message
	 */
	public FileUnit(String name, String precondition, String path, String message) {
		super(name, precondition, "sudo touch " + path, "sudo cat " + path + " 2>&1;", "", "pass", message);

		this.lines = new ArrayList<>();
		this.path = path;
	}

	/**
	 * Unit for writing out a whole file, with default fail message
	 *
	 * @param name         Name of unit test
	 * @param precondition Precondition unit test name
	 * @param path         Path to the file
	 */
	public FileUnit(String name, String precondition, String path) {
		this(name, precondition, path, "Couldn't create " + path + ".  This is a pretty serious problem!");
	}

	private void rebuildUnit() {
		String body = String.join("", this.lines);

		// Remove any trailing newline when echoing out...
		while (body.endsWith("\n")) {
			body = body.substring(0, body.length() - 1);
		}

		super.config = "sudo [ -f " + this.path + " ] || sudo touch " + this.path + ";" + "echo \"" + body
				+ "\" | sudo tee " + this.path + " > /dev/null";

		super.test = body;
	}

	/**
	 * Append text to this FileUnit, without a trailing carriage return
	 * 
	 * @param text
	 */
	public final void appendText(String text) {
		this.appendLine(text, false);
	}

	/**
	 * Append a line of text to this FileUnit.
	 *
	 * @param line
	 * @param endWithCarriageReturn
	 */
	public final void appendLine(String line, Boolean endWithCarriageReturn) {
		if (endWithCarriageReturn) {
			line += "\n";
		}
		this.lines.add(line);
		rebuildUnit();
	}

	/**
	 * Append line(s) of text to this FileUnit, each ending with a carriage return
	 *
	 * @param line
	 */
	public final void appendLine(String... lines) {
		for (final String line : lines) {
			this.appendLine(line, true);
		}
	}
	
	/**
	 * Append line(s) of text to this FileUnit, each ending with a carriage return
	 *
	 * @param line
	 */
	public final void appendLine(Collection<String> lines) {
		appendLine(lines.toArray(String[]::new));
	}

	/**
	 * Append a carriage return to this FileUnit
	 */
	public final void appendCarriageReturn() {
		this.appendLine("", true);
	}
}
