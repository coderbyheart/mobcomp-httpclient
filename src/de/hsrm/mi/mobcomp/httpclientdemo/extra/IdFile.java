package de.hsrm.mi.mobcomp.httpclientdemo.extra;

import java.io.File;

/**
 * Hilfsklasse zum speichern einer Id und einer Datei
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class IdFile {
	private String id;
	private File file;

	public IdFile(String id, File file) {
		this.id = id;
		this.file = file;
	}

	public IdFile(String id) {
		this.id = id;
	}

	public IdFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
