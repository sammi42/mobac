package tac.program.model;

public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared atlas"), // 
	UntaredAtlas("TrekBuddy untared atlas"), //
	AndNav("AndNav atlas format"), //
	OziPng("Each map as one PNG image");

	private final String displayName;

	private AtlasOutputFormat(String displayName) {
		this.displayName = displayName;
	}

	public String toString() {
		return displayName;
	}
}
