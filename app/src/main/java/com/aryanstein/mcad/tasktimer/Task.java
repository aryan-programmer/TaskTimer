package com.aryanstein.mcad.tasktimer;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Task implements Serializable {
	public static final long serialVersionUID = 20210909L;
	public static final String TAG = Task.class.getSimpleName();

	private       long    _id;
	@NonNull
	private final String  name;
	private final String  description;
	private final Integer sortOrder;

	public Task(long _id, @NonNull String name, String description, Integer sortOrder) {
		this._id         = _id;
		this.name        = name;
		this.description = description;
		this.sortOrder   = sortOrder;
	}

	public long getId() {
		return _id;
	}

	public void setId(long _id) {
		this._id = _id;
	}

	@NonNull public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	@Override public String toString() {
		return "Task{" +
		       "_id=" + _id +
		       ", name='" + name + '\'' +
		       ", description='" + description + '\'' +
		       ", sortOrder=" + sortOrder +
		       '}';
	}
}
