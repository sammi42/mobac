/**
 * 
 */
package mobac.mapsources.mappacks.bing;

import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftMaps extends AbstractMicrosoft {

	public MicrosoftMaps() {
		super("Microsoft Maps", TileImageType.PNG, 'r', HttpMapSource.TileUpdate.IfNoneMatch);
	}

}