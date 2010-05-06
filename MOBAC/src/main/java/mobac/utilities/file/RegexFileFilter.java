package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RegexFileFilter implements FileFilter {

	private Pattern p;

	public RegexFileFilter(String regex) {
		p = Pattern.compile(regex);
	}

	public boolean accept(File pathname) {
		return p.matcher(pathname.getName()).matches();
	}

}
