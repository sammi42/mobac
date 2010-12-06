/**
 * 
 */
package mobac.mapsources.mappacks.bing;

import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftVirtualEarth extends AbstractMicrosoft {

	public MicrosoftVirtualEarth() {
		super("Microsoft Virtual Earth", TileImageType.JPG, 'a', HttpMapSource.TileUpdate.IfNoneMatch);
	}

}