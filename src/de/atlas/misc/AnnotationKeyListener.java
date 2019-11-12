package de.atlas.misc;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.messagesystem.*;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;

public class AnnotationKeyListener extends KeyAdapter{

	JFrame parent;
	private static AnnotationKeyListener instance = new AnnotationKeyListener();

	/*public AnnotationKeyListener (JFrame frame){
		parent = frame;
	}*/
	LabelSelectionEvent lastLabelSelectionEvent;

	public AnnotationKeyListener (){
		MessageManager.getInstance().addLabelSelectionListener(new LabelSelectionListener() {
			@Override
			public void selectionChanged(LabelSelectionEvent e) {
				if (e.getSource() != this) {
					lastLabelSelectionEvent = e;
				}
			}
		});
	}

	@Override
	public synchronized void  keyPressed(KeyEvent e) {
		MessageManager.getInstance().keyTyped(e);
		if(e.getKeyCode()==KeyEvent.VK_SPACE){
			MessageManager.getInstance().playPause(new PlayPauseEvent(this,false));
		}else if(e.getKeyCode()==KeyEvent.VK_L){
			MessageManager.getInstance().playPause(new PlayPauseEvent(this,true));					
		}else if(e.getKeyCode()==KeyEvent.VK_CONTROL){
			MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,true));
		}else if(e.getKeyCode()==KeyEvent.VK_ENTER){

			for(ObjectLine oL : Project.getInstance().getLcoll().getList()){

				if(oL.getType().equals("LabelTrack") && oL.isActive()){
					long begin = Project.getInstance().getTime();
					long end = (long)(begin + ((1000.0/Project.getInstance().getProjectFPS())/Project.getInstance().getZoom()/4));

					if(Project.getInstance()!=null && end-begin<(1000.0/Project.getInstance().getProjectFPS())){
						end = (int) (begin + (1000.0/Project.getInstance().getProjectFPS()));
					}									
					LabelObject tmp = new LabelObject(								
							"label "+(((LabelTrack)oL.getTrack()).getLabels().size()+1),//text
							"",//comment
							begin, //start
							end,//end
							1.0,//value
							LabelType.MANUAL,//type
							((LabelTrack)oL.getTrack()).getLabelClass(),null,//LClass & Entity
							System.currentTimeMillis());//timestamp									
					((LabelTrack)oL.getTrack()).addLabel(tmp);
					((LabelTrack)oL.getTrack()).triggerSelectionUpdate();					
					break;
				}
			}
		}else if(e.getKeyCode()==KeyEvent.VK_F){

			for(ObjectLine oL : Project.getInstance().getLcoll().getList()) {

				if (oL.getType().equals("LabelTrack") && oL.isActive()) {
					long begin = Project.getInstance().getTime();
					long end = begin + 1;

					if (Project.getInstance() != null && end - begin < (1000.0 / Project.getInstance().getProjectFPS())) {
						end = (int) (begin + (1000.0 / Project.getInstance().getProjectFPS()));
					}
					LabelObject tmp = new LabelObject(
							"label " + (((LabelTrack) oL.getTrack()).getLabels().size() + 1),//text
							"",//comment
							begin, //start
							end,//end
							1.0,//value
							LabelType.MANUAL,//type
							((LabelTrack) oL.getTrack()).getLabelClass(), null,//LClass & Entity
							System.currentTimeMillis());//timestamp
					tmp.setShowAsFlag(true);
					((LabelTrack) oL.getTrack()).addLabel(tmp);
					((LabelTrack) oL.getTrack()).triggerSelectionUpdate();
					break;
				}
			}
		}else if(e.getKeyCode()==KeyEvent.VK_PLUS){
			Project.getInstance().getLcoll().getTimeTrack_().zoomIn();
		}else if(e.getKeyCode()==KeyEvent.VK_MINUS){
			Project.getInstance().getLcoll().getTimeTrack_().zoomOut();
		}else if(e.getKeyCode()==KeyEvent.VK_UP){
			ArrayList<LabelTrack> trackList= new ArrayList<LabelTrack>();
			LabelTrack track = null;
			for(ObjectLine oL : Project.getInstance().getLcoll().getList()){
				if(oL.getType().equals("LabelTrack") && oL.isActive()){
					trackList.add((LabelTrack)oL.getTrack());
					if(((LabelTrack) oL.getTrack()).getSelectedLabel()!=null){
						track = (LabelTrack) oL.getTrack();
					}
				}
			}
			if(trackList.indexOf(track)>0)trackList.get(trackList.indexOf(track)-1).setSelectedLabel(trackList.get(trackList.indexOf(track)-1).getLabelClosestTo((track.getSelectedLabel().getEnd()-track.getSelectedLabel().getStart())/2+track.getSelectedLabel().getStart()));
		}else if(e.getKeyCode()==KeyEvent.VK_DOWN){
			ArrayList<LabelTrack> trackList= new ArrayList<LabelTrack>();
			LabelTrack track = null;
			for(ObjectLine oL : Project.getInstance().getLcoll().getList()){
				if(oL.getType().equals("LabelTrack") && oL.isActive()){
					trackList.add((LabelTrack)oL.getTrack());
					if(((LabelTrack) oL.getTrack()).getSelectedLabel()!=null){
						track = (LabelTrack) oL.getTrack();
					}
				}
			}
			if(trackList.indexOf(track)<trackList.size()-1)trackList.get(trackList.indexOf(track)+1).setSelectedLabel(trackList.get(trackList.indexOf(track)+1).getLabelClosestTo((track.getSelectedLabel().getEnd()-track.getSelectedLabel().getStart())/2+track.getSelectedLabel().getStart()));
		}else if(e.getKeyCode()==KeyEvent.VK_A){
			if(lastLabelSelectionEvent!=null&&lastLabelSelectionEvent.getLabelObject().getLabelType()!=LabelType.MANUAL){
				lastLabelSelectionEvent.getLabelObject().setLabelType(LabelType.AUTO_ACCEPTED);
				MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this,lastLabelSelectionEvent.getPrevObject(),lastLabelSelectionEvent.getLabelObject(),lastLabelSelectionEvent.getPostObject()));
			}
		}else if(e.getKeyCode()==KeyEvent.VK_R){
			if(lastLabelSelectionEvent!=null&&lastLabelSelectionEvent.getLabelObject().getLabelType()!=LabelType.MANUAL){
				lastLabelSelectionEvent.getLabelObject().setLabelType(LabelType.AUTO_REJECTED);
				MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this,lastLabelSelectionEvent.getPrevObject(),lastLabelSelectionEvent.getLabelObject(),lastLabelSelectionEvent.getPostObject()));
			}
		}

	}

	@Override
	public synchronized void keyReleased(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_CONTROL){
			MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,false));
		}
	}

	public static AnnotationKeyListener getInstance() {
		return instance;
	}

}
