package de.atlas.collections;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.atlas.messagesystem.*;
import de.atlas.gui.GuiUpdater;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import de.atlas.data.Project;
import de.atlas.gui.WindowManager;
import de.atlas.misc.AtlasProperties;

public class VideoTrack extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private Canvas canvas;
	private JFrame me;
	private MediaPlayerFactory mediaPlayerFactory;
	private EmbeddedMediaPlayer mediaPlayer;
	private CanvasVideoSurface videoSurface;
	private File file;
	private String name;
	private boolean active = true;
	private long time;
	private int volume = 100;
	private TimeChangedListener tcl;
	private PlaySpeedChangedListener pscl;
	private LoopPlayListener lpl;
	private boolean sendToMatlab = false;


	public VideoTrack(String name, String url, boolean active) {
		this.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_video.gif")).getImage());
		this.file = new File(url);
		this.name = name;
		this.active = active;
		this.setTitle(this.name + " - " + this.file.getName());
		canvas = new Canvas();
		canvas.setBackground(Color.black);

		contentPane = new JPanel();
		contentPane.setBackground(Color.black);
		contentPane.setLayout(new BorderLayout());
		contentPane.add(canvas, BorderLayout.CENTER);

		this.setContentPane(contentPane);
		this.setSize(800, 600);
		// this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.me = this;

		WindowManager.getInstance().addVideoWindow(this);

		mediaPlayerFactory = new MediaPlayerFactory(
				new String[] { "--no-video-title-show" });
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		
		videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);
		//mediaPlayer.setScale(200.0f);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mediaPlayer.release();
				mediaPlayerFactory.release();
			}
		});

		this.setVisible(true);

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

		pscl = new PlaySpeedChangedListener() {

			@Override
			public void playSpeedChanged(PlaySpeedEvent e) {
				setSpeed(e.getSpeedFactor());
			}
		};
		MessageManager.getInstance().addPlaySpeedChangedListener(pscl);

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
		this.setVideoZoom(AtlasProperties.getInstance()
				.getVideoZoomFactor());

		Dimension size = mediaPlayer.getVideoDimension();
		if (size != null) {
			canvas.setSize((int) (size.width * AtlasProperties
					.getInstance().getVideoZoomFactor()),
					(int) (size.height * AtlasProperties.getInstance()
							.getVideoZoomFactor()));
			me.pack();
		}

		this.setActive(active);

	}


	public boolean isActive() {
		return active;
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
			this.setVisible(true);
		} else {
			mediaPlayer.stop();
			this.setVisible(false);
		}
	}

	public String getPath() {
		return this.file.getPath();
	}

	public String getTrackType() {
		return "VideoTrack";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.setTitle(this.name + " - " + this.file.getName());
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
		// WAIT FOR Playing
		long starttime = System.currentTimeMillis();

		while (!(mediaPlayer.getMediaPlayerState().equals(mediaPlayer
				.getMediaPlayerState().valueOf("libvlc_Playing")))) {
			if (System.currentTimeMillis() - starttime > 10000) {
				break;
			}
		}
		mediaPlayer.pause();
		this.setTime(Project.getInstance().getTime());
		while (mediaPlayer.getVideoDimension() == null) {

		}
		this.setVideoZoom(Project.getInstance().getMcoll().getVideoSize());
	}


	public void play() {
		if (this.active) {
			mediaPlayer.play();
		}
	}

	private void setSpeed(double speedFactor) {
		mediaPlayer.setRate((float)speedFactor);
	}

	public void changeFile(String url){
		this.file = new File(url);
		this.prepare();
	}

	public void pause() {
		// if(this.active){
		mediaPlayer.pause();
		// }
	}

	public void startover() {
		mediaPlayer.setTime(0);
	}

	public void setTime(long t) {
		mediaPlayer.setTime(t);
	}

	public long getLength() {
		return mediaPlayer.getLength();
	}

	public float getFPS() {
		return mediaPlayer.getFps();
	}

	public void setVideoZoom(double factor) {
		if (mediaPlayer.getVideoDimension() != null) {
			mediaPlayer.setScale((float) (factor));
			this.setSize((int) (mediaPlayer.getVideoDimension().getWidth()
					* factor + (this.getSize().getWidth() - this
							.getContentPane().getWidth())), (int) (mediaPlayer
									.getVideoDimension().getHeight() * factor + (this.getSize()
											.getHeight() - this.getContentPane().getHeight())));
		}
	}

	public void setMute(boolean bool) {
		if (bool) {
			this.volume = mediaPlayer.getVolume();
			mediaPlayer.mute();

		} else {
			mediaPlayer.setVolume(this.volume);
		}
	}
	public void dispose() {
		MessageManager.getInstance().removeTimeChangedListener(tcl);
		MessageManager.getInstance().removeLoopPlayListener(lpl);
		MessageManager.getInstance().removePlaySpeedChangedListener(pscl);
//		mediaPlayerFactory = new MediaPlayerFactory(
//				new String[] { "--no-video-title-show" });
//		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
//		videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
//		mediaPlayer.setVideoSurface(videoSurface);
		mediaPlayer.release();
		mediaPlayerFactory.release();
		
		super.dispose();
	}


}
