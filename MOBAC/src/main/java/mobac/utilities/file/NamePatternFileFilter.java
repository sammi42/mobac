package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamePatternFileFilter implements FileFilter {

	protected final Pattern pattern;

	public NamePatternFileFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	public NamePatternFileFilter(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	public boolean accept(File pathname) {
		Matcher m = pattern.matcher(pathname.getName());
		return m.matches();
	}

}
