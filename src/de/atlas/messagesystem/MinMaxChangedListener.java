package de.atlas.messagesystem;

import java.util.EventListener;

public interface MinMaxChangedListener extends EventListener {
	public abstract void minMaxChanged(MinMaxChangedEvent e);

}
