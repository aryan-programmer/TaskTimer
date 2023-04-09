package com.aryanstein.mcad.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanstein.mcad.tasktimer.db.Tasks;

import java.util.Objects;

class TaskCursorRVAdapter
	extends RecyclerView.Adapter<TaskCursorRVAdapter.TaskViewHolder> {
	private static final String TAG = "TaskCursorRVAdapter";

	private Context           context;
	private Cursor            cursor;
	private TaskClickListener listener;

	public TaskCursorRVAdapter(Context context,
	                           Cursor cursor,
	                           TaskClickListener listener) {
		this.context  = context;
		this.cursor   = cursor;
		this.listener = listener;
	}

	@NonNull @Override
	public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
		                          .inflate(R.layout.task_list_item, parent, false);
		return new TaskViewHolder(view);
	}

	@Override public void onBindViewHolder(@NonNull TaskViewHolder holder, int i) {
		if(isCursorEmpty()) {
			holder.name.setText(R.string.instructions_heading);
			holder.description.setText(R.string.instructions_descriptive);
			holder.edit.setVisibility(View.GONE);
			holder.delete.setVisibility(View.GONE);
		} else {
			if(!cursor.moveToPosition(i)) {
				throw new IllegalStateException("Couldn't move cursor to position " + i);
			}
			int    col_id         = cursor.getColumnIndex(Tasks._id);
			int    colName        = cursor.getColumnIndex(Tasks.Name);
			int    colDescription = cursor.getColumnIndex(Tasks.Description);
			int    colSortOrder   = cursor.getColumnIndex(Tasks.SortOrder);
			String name           = cursor.getString(colName);
			String description    = cursor.getString(colDescription);
			Task task = new Task(cursor.getLong(col_id),
			                     name,
			                     description,
			                     cursor.getInt(colSortOrder));
			holder.name.setText(name);
			holder.description.setText(description);
			holder.edit.setVisibility(View.VISIBLE);
			holder.delete.setVisibility(View.VISIBLE);
			if(listener != null) {
				holder.edit.setOnClickListener(view -> listener.onTaskEditClick(task));
				holder.delete.setOnClickListener(view -> listener.onTaskDeleteClick(task));
				holder.view.setOnLongClickListener(view -> {
					listener.onTaskLongClick(task);
					return true;
				});
			}
		}
	}

	private boolean isCursorEmpty() {
		return cursor == null || cursor.getCount() == 0;
	}

	public int getActualItemCount() {
		return isCursorEmpty() ? 0 : cursor.getCount();
	}

	@Override public int getItemCount() {
		return isCursorEmpty() ? 1 : cursor.getCount();
	}

	public TaskClickListener getListener() {
		return listener;
	}

	public void setListener(TaskClickListener listener) {
		this.listener = listener;
	}

	/**
	 * Swaps in a new Cursor, returning the old Cursor.
	 * The returned old Cursor is <em>not</em> closed;
	 *
	 * @param cursor The new cursor
	 * @return The previous cursor, null if there wasn't.
	 * If the given cursor id the same instance as the old Cursor, null is returned
	 * @see Cursor
	 */
	Cursor swapCursor(Cursor cursor) {
		if(Objects.equals(cursor, this.cursor)) {
			return null;
		}
		int    oldCount = getItemCount();
		Cursor t        = this.cursor;
		this.cursor = cursor;
		if(cursor != null) {
			notifyDataSetChanged();
		} else {
			notifyItemRangeRemoved(0, oldCount);
		}
		return t;
	}

	static class TaskViewHolder extends RecyclerView.ViewHolder {
		private static final String TAG = "TaskViewHolder";

		@NonNull
		final View        view;
		final TextView    name;
		final TextView    description;
		final ImageButton edit;
		final ImageButton delete;

		public TaskViewHolder(@NonNull View view) {
			super(view);
			this.view   = view;
			name        = view.findViewById(R.id.task_list_item__name);
			description = view.findViewById(R.id.task_list_item__description);
			edit        = view.findViewById(R.id.task_list_item__edit);
			delete      = view.findViewById(R.id.task_list_item__delete);
		}
	}

	public interface TaskClickListener {
		void onTaskLongClick(@NonNull Task task);

		void onTaskEditClick(@NonNull Task task);

		void onTaskDeleteClick(@NonNull Task task);
	}
}
