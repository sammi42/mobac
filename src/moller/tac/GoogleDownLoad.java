package moller.tac;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleDownLoad {

	public static void getDownloadString() {
		
		String googleDownloadSite = "";
		
		for (int i=0; i < 2; i++) {
			
			if (i == 0) {
				googleDownloadSite = "maps.google.com";
			}
			else if (i == 1) {
				googleDownloadSite = "ditu.google.com";
			}
			
			String url = "http://" + googleDownloadSite;
			
			StringBuffer response = new StringBuffer(4096);

			try {
				URL               u   = new URL(url) ;
				HttpURLConnection huc = (HttpURLConnection)u.openConnection() ;

				huc.setRequestMethod( "GET" ) ;
				huc.connect();

				InputStream is   = huc.getInputStream() ;
				int         code = huc.getResponseCode() ;

				String searchString = "";
				
				if (googleDownloadSite.equals("maps.google.com")) {
					searchString = "http://mt0.google.com/mt?n";
				}
				else if (googleDownloadSite.equals("ditu.google.com")) {
					searchString = "http://mt1.google.cn/googlechina/maptile?v";
				}
				
				if (code == HttpURLConnection.HTTP_OK) {
					byte[]           buffer       = new byte [ 4096 ] ;

					int bytes = 0;

					boolean foundString = false;

					while((bytes=is.read(buffer))>0){

						for (int j = 0; j < bytes; j++) {
							response.append((char)buffer[j]);
						}

						int startIndex = response.indexOf(searchString);
						int stopIndex  = 0;

						if (foundString == true) {
							stopIndex = response.indexOf("\"", startIndex);
							
							// Clean DownloadString
							String stringToClean = response.substring(startIndex, stopIndex);
							
							// Remove all escaped <equals-sign> \x3D and
							//                    <ampersand>   \x26
							// from the string found in the download string
							stringToClean = stringToClean.replace("\\x3d", "=");
							stringToClean = stringToClean.replace("\\x26", "&");
							
							Settings s = Settings.getInstance();
							
							
							if (i == 0) {
								s.setMapsGoogleCom(stringToClean);
							}
							else if (i == 1) {
								s.setDituGoogleCom(stringToClean);
							}
							break;
						}

						if (startIndex > -1) {
							foundString = true;
						}
					}
				}
				huc.disconnect();
			}
			catch(IOException e){
				e.printStackTrace();
				System.out.println( "Exception\n" + e ) ;
			}
		}		
	}
}