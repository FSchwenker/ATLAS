package de.atlas.messagesystem;

import java.util.EventListener;

public interface LoopPlayListener extends EventListener {
	public abstract void loopPlay(LoopEvent e);

}
