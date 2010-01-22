package mobac.program.interfaces;

import java.util.Enumeration;

import mobac.program.JobDispatcher.Job;
import mobac.utilities.tar.TarIndexedArchive;


/**
 * Classes that implement this interface identify themselves as responsible for
 * specifying what tiles should be downloaded.
 * 
 * In general this interface should be implemented in combination with
 * {@link MapInterface}, {@link LayerInterface} or {@link AtlasInterface}.
 * 
 */
public interface DownloadableElement {

	/**
	 * 
	 * @param tileArchive
	 * @param listener
	 * @return An enumeration that returns {@link Job} objects. Each job should
	 *         download one map tile from the providing web server (or from the
	 *         tile cache).
	 */
	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive,
			DownloadJobListener listener);
	
}
