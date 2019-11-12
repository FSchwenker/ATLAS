package de.atlas.messagesystem;

import java.util.EventListener;

public interface TimeChangedListener extends EventListener {
	public abstract void timeChanged(TimeEvent e);

}
