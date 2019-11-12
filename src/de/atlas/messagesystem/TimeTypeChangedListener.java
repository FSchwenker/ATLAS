package de.atlas.messagesystem;

import java.util.EventListener;

public interface TimeTypeChangedListener extends EventListener {
	public abstract void timeTypeChanged(TimeTypeEvent e);

}
