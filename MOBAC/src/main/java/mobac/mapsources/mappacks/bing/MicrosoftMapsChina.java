/**
 * 
 */
package mobac.mapsources.mappacks.bing;

import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftMapsChina extends AbstractMicrosoft {

	public MicrosoftMapsChina() {
		super("Microsoft Maps China", TileImageType.PNG, 'r', HttpMapSource.TileUpdate.IfNoneMatch);
		urlBase = ".tiles.ditu.live.com/tiles/";
		urlAppend = "?g=1";
		maxZoom = 18;
	}

}