package featuretests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

public class SimpleHttpClient implements Runnable {

	static SecureRandom RND = new SecureRandom();

	public static void main(String[] args) throws InterruptedException {

		int threadCount = 8;
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new SimpleHttpClient(), "Worker " + i);
			t.start();
			threads[i] = t;
		}
		for (Thread t : threads)
			t.join();
	}

	public void run() {
		try {
			for (int i = 0; i < 100; i++)
				load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load() throws MalformedURLException, IOException {
		String url = String.format("http://localhost/tile?x=%d&y=%d&z=8", RND.nextInt(1000), RND.nextInt(1000));
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		conn.connect();

		int code = conn.getResponseCode();
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(32000);
		byte[] buffer = new byte[2049];
		do {
			int read = in.read(buffer);
			if (read <= 0)
				break;
			bout.write(buffer, 0, read);
		} while (true);
		System.out.println(Thread.currentThread().getName() + " retrieved " + bout.size() + " bytes - url: " + url);
	}
}
