package at.dcosta.tracks.validator;

public class FixedLengthLiFoBuffer<T> {

	private int aktPos = -1;
	private int itPos, itCount;
	private boolean sizeReached;
	private final Object[] elements;

	public FixedLengthLiFoBuffer(int size) {
		elements = new Object[size];
	}

	public void add(T element) {
		aktPos++;
		if (aktPos >= elements.length) {
			sizeReached = true;
			aktPos = 0;
		}
		elements[aktPos] = element;
		itPos = aktPos;
		itCount = 0;
	}

	public void clear() {
		aktPos = -1;
		itCount = 0;
		sizeReached = false;
	}

	public boolean hasMoreElements() {
		if (sizeReached) {
			return itCount < elements.length;
		}
		return itCount <= aktPos;
	}

	public T nextElement() {
		@SuppressWarnings("unchecked")
		T elm = (T) elements[itPos];
		itCount++;
		itPos--;
		if (itPos < 0) {
			itPos = elements.length - 1;
		}
		return elm;
	}

	public int size() {
		return elements.length;
	}

}
