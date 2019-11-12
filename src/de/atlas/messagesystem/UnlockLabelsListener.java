package de.atlas.messagesystem;

import java.util.EventListener;

public interface UnlockLabelsListener extends EventListener {
	public abstract void unlockLabels(UnlockLabelsEvent e);

}
