package mobac.ts_util;

public class TileStoreUtil {

	public static void main(String[] args) {
		testForMobacJar();
		Main.main(args);
	}

	private static void testForMobacJar() {
		try {
			Class.forName("mobac.StartMOBAC");
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to find \"Mobile_Atlas_Creator.jar\".\n"
					+ "Please make sure that \"ts-util.jar\" is located in the same directory.");
			System.exit(1);
		}
	}
}
