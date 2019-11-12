package de.atlas.messagesystem;

import java.util.EventListener;

public interface ClassChangedListener extends EventListener {
	public abstract void classChanged(ClassChangedEvent e);

}
