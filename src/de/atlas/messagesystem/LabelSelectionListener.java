package de.atlas.messagesystem;

import java.util.EventListener;

public interface LabelSelectionListener extends EventListener {
	public abstract void selectionChanged(LabelSelectionEvent e);

}
