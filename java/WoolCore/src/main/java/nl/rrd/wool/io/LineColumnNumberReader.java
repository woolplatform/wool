package nl.rrd.wool.io;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This buffered reader keeps track of the current line number and column
 * number.
 * 
 * @author Dennis Hofs (RRD)
 */
public class LineColumnNumberReader extends Reader {
	private Reader reader;

	// "position" is the current position of this reader.
	// If there is a resetBuffer, then "position" corresponds to the character
	// at "resetOff" in the resetBuffer.
	// Otherwise "position" corresponds to the current position in "buffer" or
	// "reader".
	private long position = 0;
	
	private char[] buffer;
	private int bufferOff = 0;
	private int bufferLen = 0;
	
	private State state = new State();

	// "resetBuffer" contains characters between "position" and the actual
	// position in "buffer" or "reader". The character at "position" is at index
	// "resetOff" in "resetBuffer".
	private char[] resetBuffer = null;
	private int resetOff = 0;

	// restoreStates: states that were obtained with getRestoreState()
	private List<RestoreState> restoreStates = new ArrayList<>();
	
	// markStates: state that was saved with mark()
	private RestoreState markState = null;
	
	public LineColumnNumberReader(Reader reader) {
		this.reader = reader;
		buffer = new char[4096];
	}

	@Override
	public void close() throws IOException {
		restoreStates.clear();
		markState = null;
		resetBuffer = null;
		buffer = null;
		bufferLen = 0;
		reader.close();
	}
	
	public long getPosition() {
		return position;
	}

	public int getLineNum() {
		return state.lineNum;
	}

	public int getColNum() {
		return state.colNum;
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int nread = 0;
		if (nread < len && resetBuffer != null) {
			int copyLen = len;
			if (resetBuffer.length - resetOff < copyLen)
				copyLen = resetBuffer.length - resetOff;
			System.arraycopy(resetBuffer, resetOff, cbuf, off, copyLen);
			nread += copyLen;
			resetOff += copyLen;
			if (resetOff == resetBuffer.length) {
				resetBuffer = null;
				resetOff = 0;
			}
		}
		while (bufferLen >= 0 && nread < len) {
			if (bufferOff == bufferLen) {
				bufferOff = 0;
				bufferLen = reader.read(buffer, 0, buffer.length);
				if (bufferLen <= 0)
					break;
			}
			int copyLen = len - nread;
			if (bufferLen - bufferOff < copyLen)
				copyLen = bufferLen - bufferOff;
			System.arraycopy(buffer, bufferOff, cbuf, off + nread, copyLen);
			nread += copyLen;
			bufferOff += copyLen;
		}
		if (nread == 0)
			return bufferLen < 0 ? bufferLen : 0;
		if (markState != null)
			updateRestoreStateAfterRead(markState, position, cbuf, off, nread);
		for (RestoreState restoreState : restoreStates) {
			updateRestoreStateAfterRead(restoreState, position, cbuf,
					off, nread);
		}
		for (int i = 0; i < nread; i++) {
			char c = cbuf[off + i];
			if (c == '\r') {
				state.lineNum++;
				state.colNum = 1;
				state.isPrevCR = true;
			} else if (c == '\n') {
				if (state.isPrevCR) {
					state.isPrevCR = false;
				} else {
					state.lineNum++;
					state.colNum = 1;
				}
			} else {
				if (state.isPrevCR)
					state.isPrevCR = false;
				state.colNum++;
			}
		}
		position += nread;
		return nread;
	}
	
	private void updateRestoreStateAfterRead(RestoreState restoreState,
			long position, char[] cbuf, int off, int len) {
		int srcStart = off;
		int copyLen = len;
		if (position < restoreState.markPos) {
			int diff = (int)(restoreState.markPos - position);
			srcStart += diff;
			copyLen -= diff;
			if (copyLen <= 0)
				return;
		}
		appendToMarkBuffer(restoreState, cbuf, srcStart, copyLen);
	}

	@Override
	public boolean ready() throws IOException {
		if (resetBuffer != null || bufferOff < bufferLen)
			return true;
		else
			return super.ready();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		RestoreFixedState restoreState = new RestoreFixedState();
		restoreState.markPos = position;
		restoreState.markState = new State(state);
		restoreState.markBuffer = new char[readAheadLimit];
		restoreState.markOff = 0;
		markState = restoreState;
	}

	@Override
	public void reset() throws IOException {
		if (markState == null) {
			throw new IOException(
					"Reset failed: mark not set or already reset");
		}
		doRestoreState(markState);
		markState = null;
	}
	
	public Object getRestoreState() {
		RestoreDynamicState restoreState = new RestoreDynamicState();
		restoreState.markPos = position;
		restoreState.markState = new State(state);
		restoreState.markBuffer = new StringBuilder();
		restoreStates.add(restoreState);
		return restoreState;
	}
	
	public Object getRestoreState(int readAheadLimit) {
		RestoreFixedState restoreState = new RestoreFixedState();
		restoreState.markPos = position;
		restoreState.markState = new State(state);
		restoreState.markBuffer = new char[readAheadLimit];
		restoreState.markOff = 0;
		restoreStates.add(restoreState);
		return restoreState;
	}
	
	public void reinitRestoreState(Object state) {
		RestoreState restoreState = (RestoreState)state;
		restoreState.markPos = position;
		restoreState.markState.set(this.state);
		if (state instanceof RestoreDynamicState) {
			RestoreDynamicState dynState = (RestoreDynamicState)state;
			dynState.markBuffer.delete(0, dynState.markBuffer.length());
		} else {
			RestoreFixedState fixedState = (RestoreFixedState)state;
			fixedState.markOff = 0;
		}
	}
	
	public void clearRestoreState(Object savedState) {
		restoreStates.remove(savedState);
	}
	
	public void restoreState(Object savedState) throws IOException {
		if (!restoreStates.contains(savedState)) {
			throw new IOException(
					"Restore state failed: cleared or already restored");
		}
		RestoreState restoreState = (RestoreState)savedState;
		doRestoreState(restoreState);
		restoreStates.remove(restoreState);
	}
	
	private void doRestoreState(RestoreState restoreState) throws IOException {
		if (markState != null && markState != restoreState)
			updateRestoreStatePosition(markState, restoreState.markPos);
		for (RestoreState other : restoreStates) {
			if (other != restoreState)
				updateRestoreStatePosition(other, restoreState.markPos);
		}
		if (restoreState.markPos < position) {
			prependMarkBufferToResetBuffer(restoreState);
		} else if (restoreState.markPos > position) {
			int len = (int)(restoreState.markPos - position);
			resetOff += len;
			if (resetOff == resetBuffer.length) {
				resetBuffer = null;
				resetOff = 0;
			}
		}
		state = restoreState.markState;
		position = restoreState.markPos;
	}
	
	private void updateRestoreStatePosition(RestoreState restoreState,
			long newPos) {
		if (newPos > position && newPos > restoreState.markPos) {
			int posMoveLen = (int)(newPos - position);
			int srcStart;
			int len;
			if (position < restoreState.markPos) {
				len = (int)(newPos - restoreState.markPos);
				srcStart = resetOff + posMoveLen - len;
			} else {
				len = posMoveLen;
				srcStart = resetOff;
			}
			appendToMarkBuffer(restoreState, resetBuffer, srcStart, len);
		} else if (newPos < position && position > restoreState.markPos) {
			int posMoveLen = (int)(position - newPos);
			int len;
			if (newPos < restoreState.markPos)
				len = (int)(position - restoreState.markPos);
			else
				len = posMoveLen;
			removeFromMarkBufferEnd(restoreState, len);
		}
	}
	
	private void appendToMarkBuffer(RestoreState restoreState, char[] cbuf,
			int off, int len) {
		if (restoreState instanceof RestoreFixedState) {
			RestoreFixedState fixedState = (RestoreFixedState)restoreState;
			int bufRemain = fixedState.markBuffer.length - fixedState.markOff;
			int toCopy = len < bufRemain ? len : bufRemain;
			if (toCopy > 0) {
				System.arraycopy(cbuf, off, fixedState.markBuffer,
						fixedState.markOff, toCopy);
			}
			fixedState.markOff += len;
		} else {
			RestoreDynamicState dynState = (RestoreDynamicState)restoreState;
			dynState.markBuffer.append(cbuf, off, len);
		}
	}
	
	private void removeFromMarkBufferEnd(RestoreState restoreState, int len) {
		if (restoreState instanceof RestoreFixedState) {
			RestoreFixedState fixedState = (RestoreFixedState)restoreState;
			fixedState.markOff -= len;
		} else {
			RestoreDynamicState dynState = (RestoreDynamicState)restoreState;
			int buflen = dynState.markBuffer.length();
			dynState.markBuffer.delete(buflen - len, buflen);
		}
	}
	
	private void prependMarkBufferToResetBuffer(RestoreState restoreState)
			throws IOException {
		int copyLen = (int)(position - restoreState.markPos);
		int mergedLen = copyLen;
		if (resetBuffer != null)
			mergedLen = copyLen + resetBuffer.length - resetOff;
		char[] merged = new char[mergedLen];
		if (restoreState instanceof RestoreFixedState) {
			RestoreFixedState fixedState = (RestoreFixedState)restoreState;
			if (copyLen > fixedState.markBuffer.length) {
				String fn = restoreState == markState ? "Reset" :
						"Restore state";
				throw new IOException(fn + " failed: fixed buffer overflown");
			}
			System.arraycopy(fixedState.markBuffer, 0, merged, 0, copyLen);
		} else {
			RestoreDynamicState dynState = (RestoreDynamicState)restoreState;
			dynState.markBuffer.getChars(0, dynState.markBuffer.length(),
					merged, 0);
		}
		if (resetBuffer != null) {
			System.arraycopy(resetBuffer, resetOff, merged, copyLen,
					resetBuffer.length - resetOff);
		}
		resetBuffer = merged;
		resetOff = 0;
	}
	
	private class State {
		public boolean isPrevCR = false;
		public int lineNum = 1;
		public int colNum = 1;
		
		public State() {
		}
		
		public State(State other) {
			set(other);
		}
		
		public void set(State other) {
			isPrevCR = other.isPrevCR;
			lineNum = other.lineNum;
			colNum = other.colNum;
		}
	}
	
	private abstract class RestoreState {
		/**
		 * The input position where the mark was set.
		 */
		public long markPos;
		
		/**
		 * The state of the reader when the mark was set. This corresponds to
		 * "markPos".
		 */
		public State markState;
	}
	
	private class RestoreFixedState extends RestoreState {
		/**
		 * The buffer for characters between the mark and the current position.
		 * It is filled until "markOff".
		 */
		public char[] markBuffer;
		
		/**
		 * The index in "markBuffer" for the next character. This can be larger
		 * than the length of markBuffer to indicate that the buffer has
		 * overflown.
		 */
		public int markOff;
	}
	
	private class RestoreDynamicState extends RestoreState {
		/**
		 * The buffer for characters between the mark and the current position.
		 */
		public StringBuilder markBuffer;
	}
}
