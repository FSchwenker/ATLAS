package de.atlas.messagesystem;

import java.util.EventListener;

public interface PlayPauseListener extends EventListener {
	public abstract void PlayPause(PlayPauseEvent e);

}
