package tac.program.download;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import tac.program.AtlasThread;
import tac.program.JobDispatcher;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadableElement;
import tac.tar.TarIndexedArchive;

/**
 * Creates the jobs for downloading tiles. If the job queue is full it will
 * block on {@link JobDispatcher#addJob(Job)}
 */
public class DownloadJobProducerThread extends Thread {

	private Logger log = Logger.getLogger(DownloadJobProducerThread.class);

	final JobDispatcher downloadJobDispatcher;

	final Enumeration<Job> jobEnumerator;

	public DownloadJobProducerThread(AtlasThread atlasThread, JobDispatcher downloadJobDispatcher,
			TarIndexedArchive tileArchive, DownloadableElement de) {
		this.downloadJobDispatcher = downloadJobDispatcher;
		jobEnumerator = de.getDownloadJobs(tileArchive, atlasThread);
		start();
	}

	@Override
	public void run() {
		try {
			while (jobEnumerator.hasMoreElements()) {
				Job job = jobEnumerator.nextElement();
				downloadJobDispatcher.addJob(job);
				log.trace("Job added: " + job);
			}
			log.debug("All download jobs has been generated");
		} catch (InterruptedException e) {
			downloadJobDispatcher.cancelOutstandingJobs();
			log.error("Download job generation interrupted");
		}
	}

	public void cancel() {
		try {
			interrupt();
		} catch (Exception e) {
		}
	}

}
