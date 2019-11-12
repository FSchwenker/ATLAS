package de.atlas.messagesystem;

import java.util.EventListener;

public interface VideoZoomChangedListener extends EventListener {
	public abstract void videoZoomChanged(VideoZoomEvent e);

}
