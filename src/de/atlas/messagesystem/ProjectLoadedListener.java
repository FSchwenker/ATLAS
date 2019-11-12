package de.atlas.messagesystem;

import java.util.EventListener;

public interface ProjectLoadedListener extends EventListener {
	public abstract void projectLoaded(ProjectLoadedEvent e);

}
