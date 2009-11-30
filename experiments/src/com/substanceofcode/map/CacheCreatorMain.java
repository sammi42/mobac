package com.substanceofcode.map;

import java.io.File;
import java.io.IOException;

public class CacheCreatorMain {
	static final String version = "0.03";

	public static void main(String[] args) throws IOException {

		File indir = null, outdir = null;
		if (args.length < 1) {
			usage();
		}

		String cache = args[0];
		if (args.length > 1) {
			indir = new File(args[1]);
			outdir = indir;
		}
		if (args.length > 2) {
			outdir = new File(args[2]);
		}
		if (!outdir.isDirectory() || !indir.isDirectory())
			usage();
		
		System.out.println("cache:  " + cache);
		System.out.println("indir:  " + indir);
		System.out.println("outdir: " + outdir);
		CacheCreator cc = new CacheCreator(cache, outdir, indir);
		cc.run();
	}

	private static void usage() {
		System.out.println("MTE Tile Cache Creator version: " + version);
		System.out.println("------------------------------------");
		System.out.println("This program creates a map cache file for the Mobile Trail Explorer application");
		System.out.println("Use this if you don't want to incur data charges on your mobile from downloading maps\n");
		System.out.println("Usage:");
		System.out.println("java -jar CacheCreator.jar cachetype inputdir [outputdir]\n");

		System.out.println("cachetype - must be one of the supported MTE map types ");
		System.out.println("	currently 'osmmaps','tahmaps' or 'LocalSwissMap'");

		System.out.println("inputdir - must point to a directory of downloaded map images");
		System.out.println("arranged in the following format: zoomlevel/latitude/longitude.png");
		System.out.println("eg c:\\tiles\\15\\1345\\1695.png\n");
		System.out.println("Images can be downloaded in this format from \n"
				+ "http://wiki.openstreetmap.org/index.php/JTileDownloader");
		System.out.println("If outputdir is omitted, the cache file will be created in the input directory");
		System.out.println("If a cache file already exists at this location");
		System.out.println("the program will append any new images to it.");
		System.exit(0);
	}
}
