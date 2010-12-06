/**
 * 
 */
package mobac.mapsources.mappacks.bing;

import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftMapsHillShade extends AbstractMicrosoft {

	public MicrosoftMapsHillShade() {
		super("Microsoft Maps with hill shade", TileImageType.PNG, 'r', HttpMapSource.TileUpdate.IfNoneMatch);
		urlAppend = "?g=563&shading=hill";
	}

}