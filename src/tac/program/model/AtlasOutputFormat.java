package tac.program.model;

public enum AtlasOutputFormat {

	TaredAtlas("Tared: tiles packed in tar files"), UntaredAtlas(
			"Untared: tiles in seperate files");

	private final String displayName;

	private AtlasOutputFormat(String displayName) {
		this.displayName = displayName;
	}

	public String toString() {
		return displayName;
	}
}
