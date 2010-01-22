package mobac.program;

/**
 * Central instance that allows to pause/resume multiple threads at once. Used
 * in MOBAC for pausing/resuming map tile download and map creation process.
 */
public class PauseResumeHandler {

	protected boolean paused = false;

	public boolean isPaused() {
		return paused;
	}

	/**
	 * Enters the pause state.
	 */
	public void pause() {
		paused = true;
	}

	/**
	 * End the pause state and resumes all waiting threads.
	 */
	public void resume() {
		paused = false;
		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * If {@link #isPaused()}== <code>true</code> this method will not return
	 * until {@link #resume()} has been called. Otherwise this method returns
	 * immediately.
	 * 
	 * @throws InterruptedException
	 *             Thrown if the calling {@link Thread} is interrupted while
	 *             waiting for resume
	 */
	public void pauseWait() throws InterruptedException {
		if (paused) {
			synchronized (this) {
				if (paused)
					this.wait();
			}
		}
	}

}
