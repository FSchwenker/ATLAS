package de.atlas.semaine;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.collections.ScalarTrack;
import de.atlas.collections.VectorTrack;
import de.atlas.collections.DataTrack;
import de.atlas.data.*;
import de.atlas.messagesystem.*;
import de.atlas.misc.AtlasProperties;
import eu.semaine.components.Component;
import eu.semaine.jms.message.SEMAINEMessage;
import eu.semaine.jms.receiver.Receiver;
import eu.semaine.jms.sender.Sender;

import javax.jms.JMSException;
import java.util.ArrayList;

public class AtlasDataControl extends Component{

	private Receiver receiver;
	private Sender sender;
	private long lastSend = 0;
	private boolean playing = false;
	private boolean looping = false;
	private boolean ignoreNextPlayEvent = false;
	private String ID = this.getClass().getSimpleName() + " " + System.currentTimeMillis();

	public AtlasDataControl() throws JMSException {
		super("AtlasDataControl");
		receiver = new Receiver("semaine.data.atlas.dataControl");
		receivers.add(receiver);
		sender = new Sender("semaine.data.atlas.dataControl", "TEXT", ID);
		senders.add(sender);

/*		MessageManager.getInstance().addTimeChangedListener(new TimeChangedListener(){

			@Override
			public void timeChanged(TimeEvent e) {
				if((e.getSource().getClass().toString().equals(AtlasDataControl.class.toString())))return;
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
*/

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
		int pointIndex = message.getText().indexOf(".");
		int leftBraceIndex = message.getText().indexOf("(");
		int rightBraceIndex = message.getText().lastIndexOf(")");
		String trackName = message.getText().substring(0,pointIndex);
		String method = message.getText().substring(pointIndex+1,leftBraceIndex);
		String[] parameters = message.getText().substring(leftBraceIndex+1,rightBraceIndex).replace("\\s","").split(",");

		ObjectLine ol = Project.getInstance().getLcoll().getTrackByName(trackName);
		if(ol.getTrack() instanceof LabelTrack){
			LabelTrack lt = (LabelTrack)ol.getTrack();
			switch(method) {
				case "addLabel":
					String text = "";
					String comment = "";
					long start = 0l;
					long end = 0l;
					long timestamp = System.currentTimeMillis();
					double val = 1.0;
					LabelType labelType = LabelType.MANUAL;
					LabelClass labelClass = null;
					LabelClassEntity labelClassEntity = null;

					switch (parameters.length) {
						case 8:
							switch (parameters[7]) {
								case "MANUAL":
									labelType = LabelType.MANUAL;
									break;
								case "AUTOMATIC":
									labelType = LabelType.AUTOMATIC;
									break;
								case "ACCEPTED":
									labelType = LabelType.AUTO_ACCEPTED;
									break;
								case "REJECTED":
									labelType = LabelType.AUTO_REJECTED;
									break;
							}
						case 7:
							timestamp = Long.parseLong(parameters[6]);
						case 6:
							labelClassEntity = lt.getLabelClass().getEntityByName(parameters[5]);
						case 5:
							val = Double.parseDouble(parameters[4]);
						case 4:
							comment = parameters[3];
						case 3:
							labelClass = lt.getLabelClass();
							start = Long.parseLong(parameters[0]);
							end = Long.parseLong(parameters[1]);
							text = parameters[2];
							lt.addLabel(new LabelObject(text, comment, start, end, val, labelType, labelClass, labelClassEntity, timestamp));
					}
					break;
				case "removeLabels":
					lt.removeLabels(Long.parseLong(parameters[0]),Long.parseLong(parameters[1]));
					break;
			}
			MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));

		}
		if(ol.getTrack() instanceof ScalarTrack){
			ScalarTrack st = (ScalarTrack)ol.getTrack();
			ArrayList<Double> data;
			ArrayList<Double> time;
			switch (method){
				case "appendDataUnsafe":
					data = new ArrayList<>();
					for(int i=1;i<parameters.length;i++)data.add(Double.parseDouble(parameters[i]));
					st.appendDataUnsafe(Double.parseDouble(parameters[0]),data);
					break;
				case "appendDataRelative":
					data = new ArrayList<>();
					time = new ArrayList<>();
					for(int i=0;i<parameters.length;i++) {
						if (i % 2 == 0) {
							time.add(Double.parseDouble(parameters[i]));
						} else {
							data.add(Double.parseDouble(parameters[i]));
						}
					}
					st.appendDataRelative(time,data);
					break;
				case "appendDataAbsolute":
					data = new ArrayList<>();
					time = new ArrayList<>();
					for(int i=0;i<parameters.length;i++) {
						if (i % 2 == 0) {
							time.add(Double.parseDouble(parameters[i]));
						} else {
							data.add(Double.parseDouble(parameters[i]));
						}
					}
					st.appendDataAbsolute(time,data);
					break;
				case "refresh":
					st.refresh();
					break;
			}
		}
		if(ol.getTrack() instanceof VectorTrack){
			VectorTrack vt = (VectorTrack)ol.getTrack();
		}
		if(ol.getTrack() instanceof DataTrack){
			DataTrack dt = (DataTrack)ol.getTrack();
		}

	}
}
