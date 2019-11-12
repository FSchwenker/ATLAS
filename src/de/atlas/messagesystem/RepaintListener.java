package de.atlas.messagesystem;

import java.util.EventListener;

public interface RepaintListener extends EventListener {
	public abstract void repaintRequested(RepaintEvent e);

}
