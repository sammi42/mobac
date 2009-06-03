package tac.program.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import tac.program.Settings;
import tac.program.interfaces.AtlasInterface;

public class Profile implements Comparable<Profile> {

	static final Pattern p = Pattern.compile("tac-profile-([\\w _-]+).xml");

	private File file;
	private String name;

	public static Vector<Profile> getProfiles() {
		File userDir = new File(Settings.getUserDir());
		final Vector<Profile> profiles = new Vector<Profile>();
		userDir.list(new FilenameFilter() {

			public boolean accept(File dir, String fileName) {
				Matcher m = p.matcher(fileName);
				if (m.matches()) {
					String profileName = m.group(1);
					Profile profile = new Profile(new File(dir, fileName), profileName);
					profiles.add(profile);
				}
				return false;
			}
		});
		return profiles;
	}

	public Profile(String name) {
		super();
		this.file = new File(new File(Settings.getUserDir()), "tac-profile-" + name + ".xml");
		this.name = name;
	}

	protected Profile(File file, String name) {
		super();
		this.file = file;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public boolean exists() {
		return file.isFile();
	}

	public void delete() {
		if (!file.delete())
			file.deleteOnExit();
	}

	public int compareTo(Profile o) {
		return file.compareTo(o.file);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Profile))
			return false;
		Profile p = (Profile) obj;
		return file.equals(p.file);
	}

	public void save(AtlasInterface atlas) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(atlas, file);
	}

	public AtlasInterface load() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		return (AtlasInterface) um.unmarshal(file);
	}

}
