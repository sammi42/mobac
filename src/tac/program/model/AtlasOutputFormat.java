package tac.program.model;

public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared: tiles packed in tar files"), // 
	UntaredAtlas("TrekBuddy untared: tiles in seperate files"), //
	AndNav("AndNav atlas format");

	private final String displayName;

	private AtlasOutputFormat(String displayName) {
		this.displayName = displayName;
	}

	public String toString() {
		return displayName;
	}
}
