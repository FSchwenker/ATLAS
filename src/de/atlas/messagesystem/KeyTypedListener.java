package de.atlas.messagesystem;

import java.awt.event.KeyEvent;
import java.util.EventListener;

public interface KeyTypedListener extends EventListener {
	public abstract void keyTyped(KeyEvent e);

}
