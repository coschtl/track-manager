package at.dcosta.android.fw.db;

import java.util.Iterator;

import android.database.Cursor;

public abstract class AbstractCursorIterator<T> implements Iterator<T> {

	public static final <T> Iterator<T> emptyIterator() {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return false;
			};

			@Override
			public T next() {
				return null;
			};

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove is not supported for " + getClass().getName());
			}

		};
	}

	protected Cursor cursor;
	private boolean moved, hasNext;

	public AbstractCursorIterator(Cursor cursor) {
		moved = true;
		hasNext = cursor == null ? false : cursor.moveToFirst();
		this.cursor = cursor;
	}

	public abstract T createObject();

	@Override
	public boolean hasNext() {
		moveIfNecessary();
		return hasNext;
	}

	private void moveIfNecessary() {
		if (!moved) {
			hasNext = moveToNextObject(cursor);
			moved = true;
		}
		if (!hasNext) {
			cursor.close();
		}
	}

	protected boolean moveToNextObject(Cursor cursor) {
		return cursor.move(1);
	}

	@Override
	public T next() {
		moveIfNecessary();
		moved = false;
		return createObject();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove is not supported for " + getClass().getName());
	}

}