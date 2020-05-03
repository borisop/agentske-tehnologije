package beans;

import java.util.Collection;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Singleton
public class TimerBean implements TimerRemote{
	@Resource
	TimerService timerService;
	
	@Override
	public void startTimer() {
		timerService.createTimer(1000*10, 1000*10, "Argument");
	}
	
	@Override
	public void stopTimer() {
		Collection<Timer> timers = timerService.getTimers();
		for (Timer t: timers) {
			t.cancel();
		}
	}
	
	@Timeout
	public void akcija(Timer timer) {
		
	}
}
