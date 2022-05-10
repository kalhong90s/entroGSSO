package com.ais.eqx.gsso.utils;

public class TimeoutCalculator {

	private long	timeout;
	private long	counterTimeStart;
	private long	counterTimeEnd;
	private long	mills	= 0;

	public boolean isOutOfTimeout() {
		boolean isOutOfTimeout;
		if ((counterTimeEnd - 1000) <= System.currentTimeMillis()) {
			isOutOfTimeout = true;
		}
		else {
			isOutOfTimeout = false;
		}
		return isOutOfTimeout;
	}

	public long getUpdateTimeout() {

		long currentTimeMillis = System.currentTimeMillis();
		long time = Math.round(Math.ceil(((counterTimeEnd - (currentTimeMillis - mills)) / 1000.0)));
//		long time = ((counterTimeEnd - (currentTimeMillis - mills)) / 1000);
		// Log.d("Timeout Mills: "+time);
		String foo = Long.toString(time);
		String[] timeMin = foo.split("\\.");
		long result = Long.parseLong(timeMin[0]);
		if (timeMin.length == 2) {
			mills = Long.parseLong(timeMin[1]);
		}
		else {
			mills = 0;
		}
		// Log.d("ExpiryTime: "+timeout);
		// Log.d("EndCounter: "+endCounter +"   Date: "+date.toString());
		// Log.d("CurrentTimeMillis: "+currentTimeMillis+"   Date: "+new
		// Date(currentTimeMillis).toString());
		// Log.d("Timeout Result: "+result);
		if (result >= 1) {
			return result;
		}
		else {
			return 1;
		}
	}

	public static TimeoutCalculator initialTransactionToExpired(long ExpiryTime) {
		TimeoutCalculator timeoutCalculator = new TimeoutCalculator();
		timeoutCalculator.timeout = ExpiryTime;
		timeoutCalculator.counterTimeEnd = ExpiryTime;
		return timeoutCalculator;
	}

	public static TimeoutCalculator initialTimeout(int timeout) {
		TimeoutCalculator timeoutCalculator = new TimeoutCalculator();
		timeoutCalculator.setTimeout(timeout);
		timeoutCalculator.setCounterTimeStart(System.currentTimeMillis());
		timeoutCalculator.counterTimeEnd = timeoutCalculator.counterTimeStart + (timeout * 1000);
		return timeoutCalculator;

	}

	public double getCounterTimeEnd() {
		return counterTimeEnd;
	}

	public void setCounterTimeEnd(long counterTimeEnd) {
		this.counterTimeEnd = counterTimeEnd;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getCounterTimeStart() {
		return counterTimeStart;
	}

	public void setCounterTimeStart(long counterTimeStart) {
		this.counterTimeStart = counterTimeStart;
	}
}
