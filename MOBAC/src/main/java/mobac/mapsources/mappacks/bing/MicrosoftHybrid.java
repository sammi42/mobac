/**
 * 
 */
package mobac.mapsources.mappacks.bing;

import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftHybrid extends AbstractMicrosoft {

	public MicrosoftHybrid() {
		super("Microsoft Hybrid", TileImageType.JPG, 'h', HttpMapSource.TileUpdate.None);
	}

	@Override
	public String toString() {
		return "Microsoft Maps/Earth Hybrid";
	}

}