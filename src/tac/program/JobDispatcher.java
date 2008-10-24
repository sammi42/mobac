package tac.program;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class JobDispatcher {

	private static Logger log = Logger.getLogger(JobDispatcher.class);

	protected WorkerThread[] workers;

	protected BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<Job>();

	public JobDispatcher(int threadCount) {
		workers = new WorkerThread[threadCount];
		for (int i = 0; i < threadCount; i++)
			workers[i] = new WorkerThread(i);
	}

	@Override
	protected void finalize() throws Throwable {
		terminateAllWorkerThreads();
		super.finalize();
	}

	public void terminateAllWorkerThreads() {
		cancelOutstandingJobs();
		log.trace("Killing all worker threads");
		for (int i = 0; i < workers.length; i++) {
			try {
				WorkerThread w = workers[i];
				if (w != null) {
					w.interrupt();
				}
				workers[i] = null;
			} catch (Exception e) {
				// We don't care about exception here
			}
		}
	}

	public void cancelOutstandingJobs() {
		jobQueue.clear();
	}

	public void addJob(Job job) {
		try {
			jobQueue.put(job);
		} catch (InterruptedException e) {
		}
	}

	public int getWaitingJobCount() {
		return jobQueue.size();
	}

	protected static interface Job {
		public void run() throws Exception;
	}

	protected class WorkerThread extends Thread {

		Job job;

		private Logger log = Logger.getLogger(WorkerThread.class);

		public WorkerThread(int threadNum) {
			super("WorkerThread " + threadNum);
			setDaemon(true);
			job = null;
			start();
		}

		@Override
		public void run() {
			executeJobs();
			log.debug("Thread is terminating");
		}

		protected void executeJobs() {
			while (!isInterrupted()) {
				try {
					job = jobQueue.take();
				} catch (InterruptedException e1) {
					return;
				}
				if (job == null)
					return;
				try {
					job.run();
					job = null;
				} catch (Exception e) {
					if (e instanceof InterruptedException)
						return;
					log.error("Unknown error occured while executing the job: ", e);
				}
			}
		}
	}

}
