package moller.tar;

import java.io.File;

public class TestTar {

	public static void main (String [] args) {
		//TarArchive ta = new TarArchive(new File ("C:\\Kopia avtesttesttesttset skylt1.png"), new File("C:\\test.tar"));
				//TarArchive ta = new TarArchive(new File ("C:\\test"), new File("C:\\test_java.tar"));
				TarArchive ta = new TarArchive(new File ("D:\\J A V A\\[ JavaProgrammering ]\\eclipseworkspace\\TrekBuddy Atlas Creator 0.6\\src\\atlases\\20071226_100330\\Tvaaker14\\Tvaaker14000001"), new File("C:\\Tvaaker14000001_java.tar"));

				
		//ta.createArchive();
		ta.createCRTarArchive();
	}
}