package de.atlas.messagesystem;

import java.util.EventListener;

public interface ZoomChangedListener extends EventListener {
	public abstract void zoomChanged(ZoomEvent e);

}
