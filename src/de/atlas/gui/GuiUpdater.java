package de.atlas.gui;

import de.atlas.collections.MediaCollection;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import de.atlas.messagesystem.*;
import de.atlas.misc.AtlasProperties;

public class GuiUpdater implements Runnable{


	private boolean playing=false;
	private boolean looping=false;
	private LabelObject activeLabel;
	private long timestamp = 0;
	private long projecttime = 0;
	private double slowMotionFactor = 1.0;
	//private long sleeptime = 1;
	

	public GuiUpdater(String threadName, MediaCollection m){
		MessageManager.getInstance().addTimeChangedListener(new TimeChangedListener(){
			@Override
			public void timeChanged(TimeEvent e) {
				if(!(e.getSource().getClass().toString().equals(GuiUpdater.class.toString()))) {
					projecttime = e.getTime();
					timestamp = System.currentTimeMillis();
				}
			}});
		MessageManager.getInstance().addPlaySpeedChangedListener(new PlaySpeedChangedListener(){
			@Override
			public void playSpeedChanged(PlaySpeedEvent e) {
					slowMotionFactor = e.getSpeedFactor();
			}});
	}

	@Override
	public void run() {
		while(true){
			if(playing){											
				MessageManager.getInstance().timeChanged(new TimeEvent(this,(long)((System.currentTimeMillis()-timestamp)*slowMotionFactor+projecttime)));
			}else if(this.looping){				
				if(this.activeLabel!=null){
					if((System.currentTimeMillis()-timestamp)*slowMotionFactor>=this.activeLabel.getEnd()-this.activeLabel.getStart()){
						timestamp = System.currentTimeMillis();
						MessageManager.getInstance().requestLoopPlay(new LoopEvent(this,this.activeLabel));
						MessageManager.getInstance().timeChanged(new TimeEvent(this,this.activeLabel.getStart())); 
					}else{
						MessageManager.getInstance().timeChanged(new TimeEvent(this,this.activeLabel.getStart()+(long)((System.currentTimeMillis()-timestamp)*slowMotionFactor)));
					}
				}else if(Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP().isUseLoopArea()){
					if((System.currentTimeMillis()-timestamp)*slowMotionFactor>=Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP().getLoopEnd()-Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP().getLoopStart()){
						timestamp = System.currentTimeMillis();
						MessageManager.getInstance().requestLoopPlay(new LoopEvent(this,null));
						MessageManager.getInstance().timeChanged(new TimeEvent(this,Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP().getLoopStart())); 
					}else{
						MessageManager.getInstance().timeChanged(new TimeEvent(this,Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP().getLoopStart()+(long)((System.currentTimeMillis()-timestamp)*slowMotionFactor)));
					}
				}	
			}
			else{
				projecttime = Project.getInstance().getTime();
				timestamp = System.currentTimeMillis();
			}
			try {
				Thread.sleep(AtlasProperties.getInstance().getUpdateTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public void setActiveLabel(LabelObject lo){
		this.activeLabel=lo;
	}
	public long getSleeptime() {
		return AtlasProperties.getInstance().getUpdateTime();
	}

	public void setSleeptime(long sleeptime) {
		AtlasProperties.getInstance().setUpdateTime(sleeptime);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
