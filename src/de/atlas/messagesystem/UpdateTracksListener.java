package de.atlas.messagesystem;

import java.util.EventListener;

public interface UpdateTracksListener extends EventListener {
	public abstract void updateTracks(UpdateTracksEvent e);

}
