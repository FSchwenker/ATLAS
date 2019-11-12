package de.atlas.collections;

import java.io.File;

import de.atlas.gui.GuiUpdater;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import de.atlas.data.Project;
import de.atlas.messagesystem.LoopEvent;
import de.atlas.messagesystem.LoopPlayListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.TimeChangedListener;
import de.atlas.messagesystem.TimeEvent;

public class AudioTrack {

	private MediaPlayerFactory mediaPlayerFactory;
	private EmbeddedMediaPlayer mediaPlayer;
	private File file;
	private String name;
	private boolean active = true;
	// private long vlcDelay = 0;
	private long time = 0;
	private TimeChangedListener tcl;
	private LoopPlayListener lpl;
	private boolean sendToMatlab = false;

	public AudioTrack(String name, String url, boolean active) {
		this.file = new File(url);
		this.name = name;
		this.active = active;
		mediaPlayerFactory = new MediaPlayerFactory(
				new String[] { "--no-video-title-show" });
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();

		tcl = new TimeChangedListener() {
			@Override
			public void timeChanged(TimeEvent e) {
				if (!(e.getSource().getClass().toString()
						.equals(GuiUpdater.class
								.toString()))) {
					if (e.getTime() == 0) {
						prepare();
					}
					setTime(e.getTime());
					time = e.getTime();
				} else {
					time = e.getTime();
				}

			}
		};
		MessageManager.getInstance().addTimeChangedListener(tcl);
		lpl = new LoopPlayListener() {

			@Override
			public void loopPlay(LoopEvent e) {
				if (e.getActiveLabel() != null) {
					setTime(e.getActiveLabel().getStart());
					time = e.getActiveLabel().getStart();
				} else {
					setTime(Project.getInstance().getLcoll()
							.getTimeTrack_().getTimetrackP()
							.getLoopStart());
					time = Project.getInstance().getLcoll()
							.getTimeTrack_().getTimetrackP()
							.getLoopStart();
				}
			}
		};
		MessageManager.getInstance().addLoopPlayListener(lpl);
		this.prepare();
		this.setActive(active);
	}

	public boolean isActive() {
		return active;
	}
	public File getFile() {
		return file;
	}

	public boolean isSendToMatlab() {
		return sendToMatlab;
	}

	public void setSendToMatlab(boolean sendToMatlab) {
		this.sendToMatlab = sendToMatlab;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (this.active) {
			this.prepare();
			this.setTime(this.time);
			mediaPlayer.setVolume(100);
		} else {
			mediaPlayer.stop();
		}
	}

	public String getPath() {
		return this.file.getPath();
	}

	public String getType() {
		return "AudioTrack";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		if (this.active) {
			return "<html><font color= \"006e00\">" + name + "</font></html>";
		} else {
			return "<html><font color= \"6e0000\">" + name + "</font></html>";
		}
	}

	@SuppressWarnings("static-access")
	public void prepare() {
		mediaPlayer.prepareMedia(this.file.getPath());
		mediaPlayer.play();
		long starttime = System.currentTimeMillis();
		// WAIT FOR Playing
		while (!(mediaPlayer.getMediaPlayerState().equals(mediaPlayer
				.getMediaPlayerState().valueOf("libvlc_Playing")))) {
			if (System.currentTimeMillis() - starttime > 10000) {
				break;
			}
		}
		mediaPlayer.pause();
		this.setTime(Project.getInstance().getTime());
	}

	public void play() {
		if (this.active) {
			mediaPlayer.play();
		}
	}
	public void changeFile(String url){
		this.file = new File(url);
		this.prepare();

	}
	public void pause() {
		mediaPlayer.pause();
	}

	public void startover() {
		mediaPlayer.setTime(0);
	}

	/*
	 * public long getTime(){ if(useVLC){ return mediaPlayer.getTime()+vlcDelay;
	 * }else{ return audioPlayer.getMediaTime().getNanoseconds()/1000000; }
	 * 
	 * }
	 */
	public void setTime(long t) {
		mediaPlayer.setTime(t);
	}

	public void dispose() {
		MessageManager.getInstance().removeTimeChangedListener(tcl);
		MessageManager.getInstance().removeLoopPlayListener(lpl);
		mediaPlayer.release();
		mediaPlayerFactory.release();
	}

	public long getLength() {
		return mediaPlayer.getLength();
	}
}
