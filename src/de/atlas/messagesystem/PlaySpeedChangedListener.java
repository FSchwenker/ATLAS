package de.atlas.messagesystem;

import java.util.EventListener;

public interface PlaySpeedChangedListener extends EventListener {
	public abstract void playSpeedChanged(PlaySpeedEvent e);

}
