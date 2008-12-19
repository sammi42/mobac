package tac.program.interfaces;

import java.io.File;
import java.util.Enumeration;

import tac.program.JobDispatcher.Job;

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
	 * @param downloadDestinationDir
	 * @param listener
	 * @return An enumeration that returns {@link Job} objects. Each job should
	 *         download one map tile from the providing web server (or from the
	 *         tile cache).
	 */
	public Enumeration<Job> getDownloadJobs(File downloadDestinationDir,
			DownloadJobListener listener);
}
