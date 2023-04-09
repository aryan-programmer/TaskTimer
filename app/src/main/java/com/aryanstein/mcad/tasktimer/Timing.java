package com.aryanstein.mcad.tasktimer;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Timing implements Serializable {
	public static final long   serialVersionUID = 20210917L;
	public static final String TAG              = Timing.class.getSimpleName();

	private       long _id;
	@NonNull
	private final Task task;

	// Unit: Seconds
	private final long startTime;
	private       long duration;

	public Timing(@NonNull Task task) {
		this.task = task;
		Date currTime = new Date();
		startTime = currTime.getTime() / 1000;
		duration  = 0;
	}

	public long getId() {
		return _id;
	}

	public void setId(long _id) {
		this._id = _id;
	}

	@NonNull public Task getTask() {
		return task;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return duration;
	}

	public void updateDuration() {
		Date currTime = new Date();
		duration = (currTime.getTime()/1000)-startTime;
	}
}
