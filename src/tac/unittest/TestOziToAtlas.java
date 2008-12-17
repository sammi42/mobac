package tac.unittest;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tac.gui.preview.MapSources;
import tac.program.OziToAtlas;
import tac.program.model.SubMapProperties;

public class TestOziToAtlas {

	private OziToAtlas ota;

	@Before
	public void init() {
		ota = new OziToAtlas(new File(""), new File(""), 256, 256, "testMap",
				new MapSources.Mapnik(), 1);
	}

	@Test
	public void testCalculateMapSectionsNoSubMap4096() {

		// Test one column map area 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(4096, 0, 15, 0, 15);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());

		l = ota.calculateMapSections(4096, 15, 30, 15, 30);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(15, l.get(0).getXMin());
		Assert.assertEquals(30, l.get(0).getXMax());
		Assert.assertEquals(15, l.get(0).getYMin());
		Assert.assertEquals(30, l.get(0).getYMax());

		l = ota.calculateMapSections(4096, 16, 31, 16, 31);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(16, l.get(0).getXMin());
		Assert.assertEquals(31, l.get(0).getXMax());
		Assert.assertEquals(16, l.get(0).getYMin());
		Assert.assertEquals(31, l.get(0).getYMax());

		l = ota.calculateMapSections(4096, 0, 10, 0, 15);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(10, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
	}

	@Test
	public void testCalculateMapSectionsNoSubMap2048() {

		// Test one column map area 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(2048, 0, 7, 0, 7);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());

		l = ota.calculateMapSections(2048, 15, 22, 15, 22);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(15, l.get(0).getXMin());
		Assert.assertEquals(22, l.get(0).getXMax());
		Assert.assertEquals(15, l.get(0).getYMin());
		Assert.assertEquals(22, l.get(0).getYMax());

		l = ota.calculateMapSections(2048, 8, 15, 8, 15);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(8, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(8, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());

		l = ota.calculateMapSections(2048, 0, 5, 0, 7);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(5, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
	}

	@Test
	public void testCalculateMapSectionsNoSubMap1024() {

		// Test one column map area 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(1024, 0, 3, 0, 3);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());

		l = ota.calculateMapSections(1024, 4, 7, 4, 7);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(4, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(4, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());

		l = ota.calculateMapSections(1024, 0, 2, 0, 3);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(2, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
	}

	@Test
	public void testCalculateMapSectionsNoSubMap512() {

		// Test one column map area 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(512, 0, 1, 0, 1);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());

		l = ota.calculateMapSections(512, 2, 3, 2, 3);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(2, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(2, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());

		l = ota.calculateMapSections(512, 0, 0, 0, 1);

		Assert.assertEquals(1, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(0, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneColumn4096() {

		// Test one column map area 2 sub maps
		List<SubMapProperties> l = ota.calculateMapSections(4096, 0, 15, 0, 31);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(31, l.get(1).getYMax());

		l = ota.calculateMapSections(4096, 0, 10, 0, 31);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(10, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(10, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(31, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(4096, 0, 15, 0, 32);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(31, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(15, l.get(2).getXMax());
		Assert.assertEquals(32, l.get(2).getYMin());
		Assert.assertEquals(32, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(4096, 0, 15, 0, 40);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(31, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(15, l.get(2).getXMax());
		Assert.assertEquals(32, l.get(2).getYMin());
		Assert.assertEquals(40, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneColumn2048() {

		// Test one column map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(2048, 0, 7, 0, 15);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());

		l = ota.calculateMapSections(2048, 0, 5, 0, 15);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(5, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(5, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(2048, 0, 7, 0, 16);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(7, l.get(2).getXMax());
		Assert.assertEquals(16, l.get(2).getYMin());
		Assert.assertEquals(16, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(2048, 0, 7, 0, 20);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(7, l.get(2).getXMax());
		Assert.assertEquals(16, l.get(2).getYMin());
		Assert.assertEquals(20, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneColumn1024() {

		// Test one column map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(1024, 0, 3, 0, 7);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());

		l = ota.calculateMapSections(1024, 0, 2, 0, 7);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(2, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(2, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());

		l = ota.calculateMapSections(1024, 0, 0, 0, 7);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(0, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(0, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(1024, 0, 3, 0, 8);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(3, l.get(2).getXMax());
		Assert.assertEquals(8, l.get(2).getYMin());
		Assert.assertEquals(8, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(1024, 0, 3, 0, 10);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(3, l.get(2).getXMax());
		Assert.assertEquals(8, l.get(2).getYMin());
		Assert.assertEquals(10, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneColumn512() {

		// Test one column map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(512, 0, 1, 0, 3);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(1, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());

		l = ota.calculateMapSections(512, 0, 0, 0, 3);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(0, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(0, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(512, 0, 1, 0, 4);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(1, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(1, l.get(2).getXMax());
		Assert.assertEquals(4, l.get(2).getYMin());
		Assert.assertEquals(4, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(512, 0, 1, 0, 5);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(1, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(0, l.get(2).getXMin());
		Assert.assertEquals(1, l.get(2).getXMax());
		Assert.assertEquals(4, l.get(2).getYMin());
		Assert.assertEquals(5, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneRow4096() {

		// Test one row map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(4096, 0, 31, 0, 15);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(16, l.get(1).getXMin());
		Assert.assertEquals(31, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());

		l = ota.calculateMapSections(4096, 0, 31, 0, 10);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(10, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(16, l.get(1).getXMin());
		Assert.assertEquals(31, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(10, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(4096, 0, 32, 0, 15);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(16, l.get(1).getXMin());
		Assert.assertEquals(31, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(32, l.get(2).getXMin());
		Assert.assertEquals(32, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(15, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(4096, 0, 40, 0, 15);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(16, l.get(1).getXMin());
		Assert.assertEquals(31, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(32, l.get(2).getXMin());
		Assert.assertEquals(40, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(15, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneRow2048() {

		// Test one row map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(2048, 0, 15, 0, 7);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(8, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());

		l = ota.calculateMapSections(2048, 0, 15, 0, 5);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(5, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(8, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(5, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(2048, 0, 16, 0, 7);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(8, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(16, l.get(2).getXMin());
		Assert.assertEquals(16, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(7, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(2048, 0, 20, 0, 7);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(8, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(16, l.get(2).getXMin());
		Assert.assertEquals(20, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(7, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneRow1024() {

		// Test one row map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(1024, 0, 7, 0, 3);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(4, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());

		l = ota.calculateMapSections(1024, 0, 7, 0, 2);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(2, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(4, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(2, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(1024, 0, 8, 0, 3);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(4, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(8, l.get(2).getXMin());
		Assert.assertEquals(8, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(3, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(1024, 0, 10, 0, 3);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(4, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(8, l.get(2).getXMin());
		Assert.assertEquals(10, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(3, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSectionsOneRow512() {

		// Test one row map area 2 sub map
		List<SubMapProperties> l = ota.calculateMapSections(512, 0, 3, 0, 1);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(2, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(1, l.get(1).getYMax());

		l = ota.calculateMapSections(512, 0, 3, 0, 0);

		Assert.assertEquals(2, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(0, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(2, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(0, l.get(1).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(512, 0, 4, 0, 1);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(2, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(1, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(4, l.get(2).getXMin());
		Assert.assertEquals(4, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(1, l.get(2).getYMax());

		// Test one column map area 3 sub map
		l = ota.calculateMapSections(512, 0, 5, 0, 1);

		Assert.assertEquals(3, l.size());
		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(2, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(0, l.get(1).getYMin());
		Assert.assertEquals(1, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(4, l.get(2).getXMin());
		Assert.assertEquals(5, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(1, l.get(2).getYMax());
	}

	@Test
	public void testCalculateMapSections4096() {

		// Test one column and one row map area = 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(4096, 0, 15, 0, 15);

		Assert.assertEquals(1, l.size());

		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());

		// Test two column and two row map area = 4 sub maps
		l = ota.calculateMapSections(4096, 0, 31, 0, 31);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(31, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(16, l.get(2).getXMin());
		Assert.assertEquals(31, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(15, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(16, l.get(3).getXMin());
		Assert.assertEquals(31, l.get(3).getXMax());
		Assert.assertEquals(16, l.get(3).getYMin());
		Assert.assertEquals(31, l.get(3).getYMax());

		// Test two column and two row map area = 4 sub maps
		l = ota.calculateMapSections(4096, 0, 16, 0, 16);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(16, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(16, l.get(2).getXMin());
		Assert.assertEquals(16, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(15, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(16, l.get(3).getXMin());
		Assert.assertEquals(16, l.get(3).getXMax());
		Assert.assertEquals(16, l.get(3).getYMin());
		Assert.assertEquals(16, l.get(3).getYMax());

		// Test two column and two row map area = 4 sub maps
		l = ota.calculateMapSections(4096, 0, 23, 0, 23);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(15, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(15, l.get(0).getYMax());
		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(15, l.get(1).getXMax());
		Assert.assertEquals(16, l.get(1).getYMin());
		Assert.assertEquals(23, l.get(1).getYMax());
		// ... third sub map properties
		Assert.assertEquals(16, l.get(2).getXMin());
		Assert.assertEquals(23, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(15, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(16, l.get(3).getXMin());
		Assert.assertEquals(23, l.get(3).getXMax());
		Assert.assertEquals(16, l.get(3).getYMin());
		Assert.assertEquals(23, l.get(3).getYMax());

		// Test four column and four row map area = 16 sub maps
		l = ota.calculateMapSections(4096, 0, 63, 0, 63);

		Assert.assertEquals(16, l.size());

		// Test four column and four row map area = 16 sub maps
		l = ota.calculateMapSections(4096, 1, 64, 2, 65);

		Assert.assertEquals(16, l.size());
	}

	@Test
	public void testCalculateMapSections2048() {

		// Test one column and one row map area = 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(2048, 0, 7, 0, 7);

		Assert.assertEquals(1, l.size());

		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(2048, 0, 15, 0, 15);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(15, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(8, l.get(2).getXMin());
		Assert.assertEquals(15, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(7, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(8, l.get(3).getXMin());
		Assert.assertEquals(15, l.get(3).getXMax());
		Assert.assertEquals(8, l.get(3).getYMin());
		Assert.assertEquals(15, l.get(3).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(2048, 0, 10, 0, 9);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(7, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(7, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(7, l.get(1).getXMax());
		Assert.assertEquals(8, l.get(1).getYMin());
		Assert.assertEquals(9, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(8, l.get(2).getXMin());
		Assert.assertEquals(10, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(7, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(8, l.get(3).getXMin());
		Assert.assertEquals(10, l.get(3).getXMax());
		Assert.assertEquals(8, l.get(3).getYMin());
		Assert.assertEquals(9, l.get(3).getYMax());

		// Test three column and three row map area = 9 sub map
		l = ota.calculateMapSections(2048, 0, 23, 0, 23);

		Assert.assertEquals(9, l.size());
	}

	@Test
	public void testCalculateMapSections1024() {

		// Test one column and one row map area = 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(1024, 0, 3, 0, 3);

		Assert.assertEquals(1, l.size());

		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(1024, 0, 7, 0, 7);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(7, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(4, l.get(2).getXMin());
		Assert.assertEquals(7, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(3, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(4, l.get(3).getXMin());
		Assert.assertEquals(7, l.get(3).getXMax());
		Assert.assertEquals(4, l.get(3).getYMin());
		Assert.assertEquals(7, l.get(3).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(1024, 0, 6, 0, 5);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(3, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(3, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(3, l.get(1).getXMax());
		Assert.assertEquals(4, l.get(1).getYMin());
		Assert.assertEquals(5, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(4, l.get(2).getXMin());
		Assert.assertEquals(6, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(3, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(4, l.get(3).getXMin());
		Assert.assertEquals(6, l.get(3).getXMax());
		Assert.assertEquals(4, l.get(3).getYMin());
		Assert.assertEquals(5, l.get(3).getYMax());

		// Test three column and three row map area = 9 sub map
		l = ota.calculateMapSections(1024, 0, 8, 0, 8);

		Assert.assertEquals(9, l.size());
	}

	@Test
	public void testCalculateMapSections512() {

		// Test one column and one row map area = 1 sub map
		List<SubMapProperties> l = ota.calculateMapSections(512, 0, 1, 0, 1);

		Assert.assertEquals(1, l.size());

		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(512, 0, 3, 0, 3);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(1, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(3, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(2, l.get(2).getXMin());
		Assert.assertEquals(3, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(1, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(2, l.get(3).getXMin());
		Assert.assertEquals(3, l.get(3).getXMax());
		Assert.assertEquals(2, l.get(3).getYMin());
		Assert.assertEquals(3, l.get(3).getYMax());

		// Test two column and two row map area = 4 sub map
		l = ota.calculateMapSections(512, 0, 3, 0, 2);

		Assert.assertEquals(4, l.size());

		// ... first sub map properties
		Assert.assertEquals(0, l.get(0).getXMin());
		Assert.assertEquals(1, l.get(0).getXMax());
		Assert.assertEquals(0, l.get(0).getYMin());
		Assert.assertEquals(1, l.get(0).getYMax());

		// ... second sub map properties
		Assert.assertEquals(0, l.get(1).getXMin());
		Assert.assertEquals(1, l.get(1).getXMax());
		Assert.assertEquals(2, l.get(1).getYMin());
		Assert.assertEquals(2, l.get(1).getYMax());

		// ... third sub map properties
		Assert.assertEquals(2, l.get(2).getXMin());
		Assert.assertEquals(3, l.get(2).getXMax());
		Assert.assertEquals(0, l.get(2).getYMin());
		Assert.assertEquals(1, l.get(2).getYMax());

		// ... fourth sub map properties
		Assert.assertEquals(2, l.get(3).getXMin());
		Assert.assertEquals(3, l.get(3).getXMax());
		Assert.assertEquals(2, l.get(3).getYMin());
		Assert.assertEquals(2, l.get(3).getYMax());

		// Test three column and three row map area = 9 sub map
		l = ota.calculateMapSections(512, 0, 4, 0, 4);

		Assert.assertEquals(9, l.size());

	}
}