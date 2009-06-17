package tac.program.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.AtlasObject;

public class Profile implements Comparable<Profile> {

	static final Pattern p = Pattern.compile("tac-profile-([\\w _-]+).xml");

	private File file;
	private String name;
	private static Vector<Profile> profiles = new Vector<Profile>();

	public static void updateProfiles() {
		File userDir = new File(Settings.getUserDir());
		final Set<Profile> deletedProfiles = new HashSet<Profile>();
		deletedProfiles.addAll(profiles);
		userDir.list(new FilenameFilter() {

			public boolean accept(File dir, String fileName) {
				Matcher m = p.matcher(fileName);
				if (m.matches()) {
					String profileName = m.group(1);
					Profile profile = new Profile(new File(dir, fileName), profileName);
					deletedProfiles.remove(profile);
					profiles.add(profile);
				}
				return false;
			}
		});
		for (Profile p : deletedProfiles)
			profiles.remove(p);
	}

	public static Vector<Profile> getProfiles() {
		updateProfiles();
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

	public void save(AtlasInterface atlasInterface) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(atlasInterface, file);
	}

	public AtlasInterface load() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		AtlasInterface newAtlas = (AtlasInterface) um.unmarshal(file);
		return newAtlas;
	}

	public static boolean checkAtlas(AtlasInterface atlasInterface) {
		return checkAtlasObject(atlasInterface);
	}

	private static boolean checkAtlasObject(Object o) {
		boolean result = false;
		if (o instanceof AtlasObject) {
			result |= ((AtlasObject) o).checkData();
		}
		if (o instanceof Iterable<?>) {
			Iterable<?> it = (Iterable<?>) o;
			for (Object ao : it) {
				result |= checkAtlasObject(ao);
			}
		}
		return result;
	}
}
