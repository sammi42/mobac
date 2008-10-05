package tac.unittest;

import junit.framework.Assert;

import org.junit.Test;

import tac.utilities.PersistentProfiles;

public class TestPersistentProfiles {
	
	@Test
	public void  testValidateCoordinate() {
			
		Assert.assertEquals(70.0, PersistentProfiles.validateCoordinate("lat", 70.0));
		Assert.assertEquals(85.0, PersistentProfiles.validateCoordinate("lat", 90.0));
		Assert.assertEquals(-85.0, PersistentProfiles.validateCoordinate("lat", -90.0));
		
		Assert.assertEquals(70.0, PersistentProfiles.validateCoordinate("long", 70.0));
		Assert.assertEquals(179.0, PersistentProfiles.validateCoordinate("long", 190.0));
		Assert.assertEquals(-179.0, PersistentProfiles.validateCoordinate("long", -190.0));
	}
	
	@Test
	public void  testValidateTileSize() {
		
		Assert.assertEquals(512, PersistentProfiles.validateTileSize(512));
		Assert.assertEquals(1792, PersistentProfiles.validateTileSize(2048));
		Assert.assertEquals(256, PersistentProfiles.validateTileSize(0));
		Assert.assertEquals(256, PersistentProfiles.validateTileSize(290));
		
	}
	
	@Test
	public void  testValidateCustomTileSize() {
		
		Assert.assertEquals(1792, PersistentProfiles.validateCustomTileSize(1800));
		Assert.assertEquals(256, PersistentProfiles.validateCustomTileSize(0));
		Assert.assertEquals(300, PersistentProfiles.validateCustomTileSize(300));
	}
}