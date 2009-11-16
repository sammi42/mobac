package tac.program;

public class PauseResumeHandler {

	protected boolean paused = false;
	protected Object pauseLock = new Object();

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
		synchronized (pauseLock) {
			pauseLock.notifyAll();
		}
	}

	public void pauseWait() throws InterruptedException {
		if (paused) {
			synchronized (pauseLock) {
				if (paused)
					pauseLock.wait();
			}
		}
	}

}
