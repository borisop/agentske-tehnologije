package beans;

import javax.ejb.Remote;

@Remote
public interface TimerRemote {
	public void startTimer();
	
	public void stopTimer();
}
