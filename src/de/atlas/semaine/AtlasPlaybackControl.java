package de.atlas.semaine;

import javax.jms.JMSException;

import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.PlayPauseEvent;
import de.atlas.messagesystem.PlayPauseListener;
import de.atlas.messagesystem.ProjectLoadedEvent;
import de.atlas.messagesystem.ProjectLoadedListener;
import de.atlas.messagesystem.TimeChangedListener;
import de.atlas.messagesystem.TimeEvent;
import de.atlas.misc.AtlasProperties;

import eu.semaine.components.Component;
import eu.semaine.jms.message.SEMAINEMessage;
import eu.semaine.jms.receiver.Receiver;
import eu.semaine.jms.sender.Sender;

public class AtlasPlaybackControl extends Component{

	private Receiver receiver;
	private Sender sender;
	private long lastSend = 0;
	private boolean playing = false;
	private boolean looping = false;
	private boolean ignoreNextPlayEvent = false;
	private String ID = this.getClass().getSimpleName() + " " + System.currentTimeMillis();

	public AtlasPlaybackControl() throws JMSException {
		super("AtlasPlaybackControl");		
		receiver = new Receiver("semaine.data.atlas.playbackControl");
		receivers.add(receiver);	
		sender = new Sender("semaine.data.atlas.playbackControl", "TEXT", ID);
		senders.add(sender);	

		MessageManager.getInstance().addTimeChangedListener(new TimeChangedListener(){

			@Override
			public void timeChanged(TimeEvent e) {
				if((e.getSource().getClass().toString().equals(AtlasPlaybackControl.class.toString())))return;
				if(System.currentTimeMillis()-lastSend>AtlasProperties.getInstance().getSemaineTimeSendInterval()&&(playing||looping)){
					lastSend = System.currentTimeMillis();
					try {
						sender.sendTextMessage(String.valueOf("time " + e.getTime()), meta.getTime());
					} catch (JMSException e1) {
						e1.printStackTrace();
					}
				}else if(!(playing||looping)){
					lastSend = System.currentTimeMillis();
					try {
						sender.sendTextMessage(String.valueOf("time " + e.getTime()), meta.getTime());
					} catch (JMSException e1) {
						e1.printStackTrace();
					}						
				}
			}
		});
		MessageManager.getInstance().addPlayPauseListener(new PlayPauseListener() {
			@Override
			public void PlayPause(PlayPauseEvent e) {
				if(ignoreNextPlayEvent == true){
					ignoreNextPlayEvent = false;
					return;
				}
				if((e.getSource().getClass().toString().equals(AtlasPlaybackControl.class.toString())))return;
				playing = !playing;
				if(playing){
					try {
						sender.sendTextMessage("play " + Project.getInstance().getTime() + " " + e.isLooping(),meta.getTime());
					} catch (JMSException e1) {
						e1.printStackTrace();
					}					
				}else{
					try {
						sender.sendTextMessage("pause " + Project.getInstance().getTime() + " " + e.isLooping(),meta.getTime());
					} catch (JMSException e1) {
						e1.printStackTrace();
					}					

				}
			}
		});

		MessageManager.getInstance().addProjectLoadedListener(new ProjectLoadedListener(){
			@Override
			public void projectLoaded(ProjectLoadedEvent e) {
				if((e.getSource().getClass().toString().equals(AtlasPlaybackControl.class.toString())))return;
				try {
					sender.sendTextMessage("load " + Project.getInstance().getName() + " " + e.getFileName(), meta.getTime());
				} catch (JMSException e1) {
					e1.printStackTrace();
				}									
			}

		});

	}
	protected void finalize() throws Throwable
	{
		super.finalize(); //not necessary if extending Object.
	} 


	@Override
	public void act() throws JMSException{
		//unused
	}

	@Override
	public void react(SEMAINEMessage message) throws JMSException{
		if(this.ID.equalsIgnoreCase(message.getSource()))return;
		String[] parts = message.getText().split(" ");
		if(parts[0].equalsIgnoreCase("time")){
			if(playing){
				if(java.lang.Math.abs(Long.parseLong(parts[1])-Project.getInstance().getTime())>100){
					MessageManager.getInstance().timeChanged(new TimeEvent(this, Long.parseLong(parts[1])));
				}
			}else{
				MessageManager.getInstance().timeChanged(new TimeEvent(this, Long.parseLong(parts[1])));
			}
		}else if(parts[0].equalsIgnoreCase("play")){
			playing=true;
			ignoreNextPlayEvent = true;
			MessageManager.getInstance().timeChanged(new TimeEvent(this, Long.parseLong(parts[1])));
			MessageManager.getInstance().playPause(new PlayPauseEvent(this, Boolean.parseBoolean(parts[2])));
		}else if(parts[0].equalsIgnoreCase("pause")){
			playing=false;
			ignoreNextPlayEvent = true;
			MessageManager.getInstance().playPause(new PlayPauseEvent(this, Boolean.parseBoolean(parts[2])));
			MessageManager.getInstance().timeChanged(new TimeEvent(this, Long.parseLong(parts[1])));
		}else if(parts[0].equalsIgnoreCase("load")){
			//curently unused
		}
	}
}
