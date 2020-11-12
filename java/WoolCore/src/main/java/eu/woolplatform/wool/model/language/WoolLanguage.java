package eu.woolplatform.wool.model.language;

public class WoolLanguage {

	private String name;
	private String folder;

	// ----- Constructors

	public WoolLanguage() { }

	public WoolLanguage(String name, String folder) {
		this.name = name;
		this.folder = folder;
	}

	// ----- Getters

	public String getName() {
		return name;
	}

	public String getFolder() {
		return folder;
	}

	// ----- Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	// ----- Methods

	public String toString() {
		return "[name:"+name+"] [folder:"+folder+"]";
	}
}
