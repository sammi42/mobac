package mobac.program.model;

public enum ProxyType {
	SYSTEM("Use standard Java proxy settings"), APP_SETTINGS("Use application properties"), CUSTOM(
			"Use custom proxy (user defined)");

	private String text;

	private ProxyType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}
