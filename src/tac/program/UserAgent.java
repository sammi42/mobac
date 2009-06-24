package tac.program;

public class UserAgent {

	public static final String IE7_XP = "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; "
			+ "Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

	public static final String IE6_XP = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

	public static final String FF2_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.8.1.17) Gecko/20080829";

	public static final String FF3_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11";

	private static final String OPERA9_XP = "Opera/9.6 (Windows NT 5.1; U; de) Presto/2.1.1";

	public static final UserAgent[] USER_AGENTS = new UserAgent[] {
			new UserAgent("Internet Explorer 7 WinXP", IE7_XP),
			new UserAgent("Internet Explorer 6 WinXP", IE6_XP),
			new UserAgent("Firefox 2 WinXP", FF2_XP), // 
			new UserAgent("Opera 9.6 WinXP", OPERA9_XP) };

	private String name;
	private String userAgent;

	protected UserAgent(String name, String userAgent) {
		super();
		this.name = name;
		this.userAgent = userAgent;
	}

	public String getName() {
		return name;
	}

	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public String toString() {
		return name;
	}

}
