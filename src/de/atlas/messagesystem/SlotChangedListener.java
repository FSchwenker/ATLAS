package de.atlas.messagesystem;

import java.util.EventListener;

public interface SlotChangedListener extends EventListener {
	public abstract void slotChanged(SlotEvent e);

}
