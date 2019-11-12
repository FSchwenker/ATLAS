package de.atlas.messagesystem;

import java.awt.event.KeyEvent;

import javax.swing.event.EventListenerList;

public class MessageManager {

	private static MessageManager instance = new MessageManager();
	private static EventListenerList listeners = new EventListenerList();

	public MessageManager() {
	}

	public static MessageManager getInstance() {
		return instance;
	}

	// ZOOM
	public void addZoomChangedListener(ZoomChangedListener zel) {
		listeners.add(ZoomChangedListener.class, zel);
	}

	public void removeZoomChangedListener(ZoomChangedListener zel) {
		listeners.remove(ZoomChangedListener.class, zel);
	}

	public synchronized void zoomChanged(ZoomEvent event) {
		for (ZoomChangedListener l : listeners
				.getListeners(ZoomChangedListener.class))
			l.zoomChanged(event);
	}

	// Video ZOOM
	public void addVideoZoomChangedListener(VideoZoomChangedListener zel) {
		listeners.add(VideoZoomChangedListener.class, zel);
	}

	public void removeVideoZoomChangedListener(VideoZoomChangedListener zel) {
		listeners.remove(VideoZoomChangedListener.class, zel);
	}

	public synchronized void videoZoomChanged(VideoZoomEvent event) {
		for (VideoZoomChangedListener l : listeners.getListeners(VideoZoomChangedListener.class))
			l.videoZoomChanged(event);
	}

	// TIME
	public void addTimeChangedListener(TimeChangedListener zel) {
		listeners.add(TimeChangedListener.class, zel);
	}

	public void removeTimeChangedListener(TimeChangedListener zel) {
		listeners.remove(TimeChangedListener.class, zel);
	}

	public synchronized void timeChanged(TimeEvent event) {
		for (TimeChangedListener l : listeners
				.getListeners(TimeChangedListener.class))
			l.timeChanged(event);
	}

	// SPEED
	public void addPlaySpeedChangedListener(PlaySpeedChangedListener zel) {
		listeners.add(PlaySpeedChangedListener.class, zel);
	}

	public void removePlaySpeedChangedListener(PlaySpeedChangedListener zel) {
		listeners.remove(PlaySpeedChangedListener.class, zel);
	}

	public synchronized void playSpeedChanged(PlaySpeedEvent event) {
		for (PlaySpeedChangedListener l : listeners
				.getListeners(PlaySpeedChangedListener.class))
			l.playSpeedChanged(event);
	}

	// SLOT
	public void addSlotChangedListener(SlotChangedListener zel) {
		listeners.add(SlotChangedListener.class, zel);
	}

	public void removeSlotChangedListener(SlotChangedListener zel) {
		listeners.remove(SlotChangedListener.class, zel);
	}

	public synchronized void slotChanged(SlotEvent event) {
		for (SlotChangedListener l : listeners
				.getListeners(SlotChangedListener.class))
			l.slotChanged(event);
	}

	// SELECTION
	public void addLabelSelectionListener(LabelSelectionListener zel) {
		listeners.add(LabelSelectionListener.class, zel);
	}

	public void removeLabelSelectionListener(LabelSelectionListener zel) {
		listeners.remove(LabelSelectionListener.class, zel);
	}

	public synchronized void selectionChanged(LabelSelectionEvent event) {
		int i=0;
        for (LabelSelectionListener l : listeners.getListeners(LabelSelectionListener.class)){
            l.selectionChanged(event);
            //System.out.println(l);
        }
	}

	// REPAINT
	public void addRepaintListener(RepaintListener zel) {
		listeners.add(RepaintListener.class, zel);
	}

	public void removeRepaintListener(RepaintListener zel) {
		listeners.remove(RepaintListener.class, zel);
	}

	public synchronized void requestRepaint(RepaintEvent event) {
		for (RepaintListener l : listeners.getListeners(RepaintListener.class))
			l.repaintRequested(event);
	}

	// UPDATE TRACKS
	public void addUpdateTracksListener(UpdateTracksListener zel) {
		listeners.add(UpdateTracksListener.class, zel);
	}

	public void removeUpdateTracksListener(UpdateTracksListener zel) {
		listeners.remove(UpdateTracksListener.class, zel);
	}

	public synchronized void requestTrackUpdate(UpdateTracksEvent event) {
		for (UpdateTracksListener l : listeners
				.getListeners(UpdateTracksListener.class))
			l.updateTracks(event);
	}

	// LOOP
	public void addLoopPlayListener(LoopPlayListener zel) {
		listeners.add(LoopPlayListener.class, zel);
	}

	public void removeLoopPlayListener(LoopPlayListener zel) {
		listeners.remove(LoopPlayListener.class, zel);
	}

	public synchronized void requestLoopPlay(LoopEvent event) {
		for (LoopPlayListener l : listeners
				.getListeners(LoopPlayListener.class))
			l.loopPlay(event);
	}

	// CLASS CHANGED
	public void addClassChangedListener(ClassChangedListener zel) {
		listeners.add(ClassChangedListener.class, zel);
	}

	public void removeClassChangedListener(ClassChangedListener zel) {
		listeners.remove(ClassChangedListener.class, zel);
	}

	public synchronized void requestClassChanged(ClassChangedEvent event) {
		for (ClassChangedListener l : listeners.getListeners(ClassChangedListener.class)) {
			l.classChanged(event);
		}
	}

	// TIMETYPE

	public void addTimeTypeChangedListener(TimeTypeChangedListener zel) {
		listeners.add(TimeTypeChangedListener.class, zel);
	}

	public void removeTimeTypeChangedListener(TimeTypeChangedListener zel) {
		listeners.remove(TimeTypeChangedListener.class, zel);
	}

	public synchronized void timeTypeChanged(TimeTypeEvent event) {
		for (TimeTypeChangedListener l : listeners
				.getListeners(TimeTypeChangedListener.class))
			l.timeTypeChanged(event);
	}

	// PlayPause
	public void addPlayPauseListener(PlayPauseListener zel) {
		listeners.add(PlayPauseListener.class, zel);
	}

	public void removePlayPauseListener(PlayPauseListener zel) {
		listeners.remove(PlayPauseListener.class, zel);
	}

	public synchronized void playPause(PlayPauseEvent event) {
		for (PlayPauseListener l : listeners
				.getListeners(PlayPauseListener.class))
			l.PlayPause(event);
	}

	// unlockLabels
	public void addUnlockLabelsListener(UnlockLabelsListener zel) {
		listeners.add(UnlockLabelsListener.class, zel);
	}

	public void removeUnlockLabelsListener(UnlockLabelsListener zel) {
		listeners.remove(UnlockLabelsListener.class, zel);
	}

	public synchronized void unlockLabels(UnlockLabelsEvent event) {
		for (UnlockLabelsListener l : listeners
				.getListeners(UnlockLabelsListener.class))
			l.unlockLabels(event);
	}

	// KeyTyped
	public void addKeyTypedListener(KeyTypedListener zel) {
		listeners.add(KeyTypedListener.class, zel);
	}

	public void removeKeyTypedListener(KeyTypedListener zel) {
		listeners.remove(KeyTypedListener.class, zel);
	}

	public synchronized void keyTyped(KeyEvent event) {
		for (KeyTypedListener l : listeners
				.getListeners(KeyTypedListener.class))
			l.keyTyped(event);
	}

	// ProjectLoaded
	public void addProjectLoadedListener(ProjectLoadedListener zel) {
		listeners.add(ProjectLoadedListener.class, zel);
	}

	public void removeProjectLoadedListener(ProjectLoadedListener zel) {
		listeners.remove(ProjectLoadedListener.class, zel);
	}

	public synchronized void projectLoaded(ProjectLoadedEvent event) {
		for (ProjectLoadedListener l : listeners
				.getListeners(ProjectLoadedListener.class))
			l.projectLoaded(event);
	}

	// MinMaxChanged
	public void addMinMaxChangedListener(MinMaxChangedListener zel) {
		listeners.add(MinMaxChangedListener.class, zel);
	}

	public void removeMinMaxChangedListener(MinMaxChangedListener zel) {
		listeners.remove(MinMaxChangedListener.class, zel);
	}

	public synchronized void minMaxChanged(MinMaxChangedEvent event) {
		for (MinMaxChangedListener l : listeners
				.getListeners(MinMaxChangedListener.class))
			l.minMaxChanged(event);
	}

	// Show Label Properties Window
	public void addShowLabelprobsWindowChangedListener(
			ShowLabelprobsWindowListener zel) {
		listeners.add(ShowLabelprobsWindowListener.class, zel);
	}

	public void removeShowLabelprobsWindowChangedListener(
			ShowLabelprobsWindowListener zel) {
		listeners.remove(ShowLabelprobsWindowListener.class, zel);
	}

	public synchronized void showLabelprobsWindow(
			ShowLabelprobsWindowEvent event) {
		for (ShowLabelprobsWindowListener l : listeners
				.getListeners(ShowLabelprobsWindowListener.class))
			l.showLabelprobsWindow(event);
	}



}
