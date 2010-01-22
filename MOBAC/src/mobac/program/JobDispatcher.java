package mobac.program;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;

import org.apache.log4j.Logger;


/**
 * Controls the worker threads that are downloading the map tiles in parallel.
 * Additionally the job queue containing the unprocessed tile download jobs can
 * be accessed via this class.
 */
public class JobDispatcher {

	private static Logger log = Logger.getLogger(JobDispatcher.class);

	protected int maxJobsInQueue = 100;
	protected int minJobsInQueue = 50;

	protected WorkerThread[] workers;

	protected PauseResumeHandler pauseResumeHandler;

	protected BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<Job>();

	public JobDispatcher(int threadCount, PauseResumeHandler pauseResumeHandler) {
		this.pauseResumeHandler = pauseResumeHandler;
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

	/**
	 * Blocks if more than 100 jobs are already scheduled.
	 * 
	 * @param job
	 * @throws InterruptedException
	 */
	public void addJob(Job job) throws InterruptedException {
		while (jobQueue.size() > maxJobsInQueue) {
			Thread.sleep(200);
			if ((jobQueue.size() < minJobsInQueue) && (maxJobsInQueue < 2000)) {
				// System and download connection is very fast - we have to
				// increase the maximum job count in the queue
				maxJobsInQueue *= 2;
				minJobsInQueue *= 2;
			}
		}
		jobQueue.put(job);
	}

	/**
	 * Adds the job to the job-queue and returns. This method will never block!
	 * 
	 * @param job
	 */
	public void addErrorJob(Job job) {
		try {
			jobQueue.put(job);
		} catch (InterruptedException e) {
			// Can never happen with LinkedBlockingQueue
		}
	}

	public int getWaitingJobCount() {
		return jobQueue.size();
	}

	public static interface Job {
		public void run(JobDispatcher dispatcher) throws Exception;
	}

	public boolean isAtLeastOneWorkerActive() {
		for (int i = 0; i < workers.length; i++) {
			WorkerThread w = workers[i];
			if (w != null) {
				if ((!w.idle) && (w.getState() != Thread.State.WAITING))
					return true;
			}
		}
		log.debug("All worker threads are idle");
		return false;
	}

	/**
	 * Each worker thread takes the first job from the job queue and executes
	 * it. If the queue is empty the worker blocks, waiting for the next job.
	 */
	protected class WorkerThread extends DelayedInterruptThread {

		Job job = null;

		boolean idle = true;

		private Logger log = Logger.getLogger(WorkerThread.class);

		public WorkerThread(int threadNum) {
			super("WorkerThread " + threadNum);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {
				executeJobs();
			} catch (InterruptedException e) {
			}
			log.trace("Thread is terminating");
		}

		protected void executeJobs() throws InterruptedException {
			while (!isInterrupted()) {
				try {
					pauseResumeHandler.pauseWait();
					idle = true;
					job = jobQueue.take();
					idle = false;
				} catch (InterruptedException e1) {
					return;
				}
				if (job == null)
					return;
				try {
					job.run(JobDispatcher.this);
					job = null;
				} catch (Exception e) {
					if (e instanceof InterruptedException)
						return;
					log.error("Unknown error occured while executing the job: ", e);
				} catch (OutOfMemoryError e) {
					log.error("", e);
					Thread.sleep(5000);
					System.gc();
				}
			}
		}

	}

}
