package mobac.program.tilestore.berkeleydb;

/**
 * The Berkeley DB has some problems when someone interrupts the thread that is
 * currently performing IO activity. Therefore before executing any DB we allow
 * to disable the {@link #interrupt()} method via {@link #pauseInterrupt()}.
 * After the "interrupt sensitive section" {@link #resumeInterrupt()} restores
 * the regular behavior. If the thread has been interrupted while interrupt was
 * disabled {@link #resumeInterrupt()} catches up this.
 */
public class DelayedInterruptThread extends Thread {

	private boolean interruptPaused = false;
	private boolean interruptedWhilePaused = false;

	public DelayedInterruptThread(String name) {
		super(name);
	}

	public DelayedInterruptThread(Runnable target) {
		super(target);
	}

	@Override
	public void interrupt() {
		if (interruptPaused)
			interruptedWhilePaused = true;
		else
			super.interrupt();
	}

	public void pauseInterrupt() {
		interruptPaused = true;
	}

	public void resumeInterrupt() {
		interruptPaused = false;
		if (interruptedWhilePaused)
			this.interrupt();
	}

	public boolean interruptedWhilePaused() {
		return interruptedWhilePaused;
	}

}
