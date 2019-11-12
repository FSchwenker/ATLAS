package de.atlas.collections;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.jna.NativeLibrary;

import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.TimeEvent;
import de.atlas.messagesystem.VideoZoomChangedListener;
import de.atlas.messagesystem.VideoZoomEvent;
import uk.co.caprica.vlcj.binding.LibX11;
import de.atlas.data.Project;
import de.atlas.misc.HelperFunctions;

public class MediaCollection {

	private ArrayList<VideoTrack> vtracks = new ArrayList<VideoTrack>(); 
	private ArrayList<AudioTrack> atracks = new ArrayList<AudioTrack>();

	private Iterator<VideoTrack> vI;
	private Iterator<AudioTrack> aI;

	private double videoSize = 1.0;


	public MediaCollection(){
		if(System.getProperty("os.name").startsWith("Linux")){
			System.setProperty("jna.library.path", "/usr/lib");
			LibX11.INSTANCE.XInitThreads();
		}

		if(System.getProperty("os.name").startsWith("Windows")){
			NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");
			NativeLibrary.addSearchPath("libvlc", "C:\\Program Files (x86)\\VideoLAN\\VLC");
		}
		if(System.getProperty("os.name").startsWith("Mac")){
			System.setProperty("jna.library.path", "/Applications/VLC.app/Contents/MacOS/lib");
		}
		MessageManager.getInstance().addVideoZoomChangedListener(new VideoZoomChangedListener() {
			@Override
			public void videoZoomChanged(VideoZoomEvent e) {
				setVideoSize(e.getFactor());
			}
		});

	}
	public ArrayList<AudioTrack> getAudioList(){
		return atracks;
	}
	public ArrayList<VideoTrack> getVideoList(){
		return vtracks;
	}
	public void reset(){
		for( VideoTrack vT : this.vtracks){
			vT.dispose();
		}
		for(AudioTrack aT : this.atracks){
			aT.dispose();
		}
		this.vtracks.clear();
		this.atracks.clear();		
		
	}

	public void addVideoTrack(String name, String url, boolean active, boolean sendToMatlab){
		if(HelperFunctions.fileExists(url)){//given path correct
            // do nothing
		}else if(HelperFunctions.fileExists(Project.getInstance().getProjectPath()+"media/"+new File(url).getName())){ //search in media folder
			url=Project.getInstance().getProjectPath()+"media/"+new File(url).getName();
		}else if(HelperFunctions.fileExists(Project.getInstance().getMediaPath()+new File(url).getName())){ //search in alternative path
			url = Project.getInstance().getMediaPath()+new File(url).getName();
		}else{//ask user
			JOptionPane.showMessageDialog(null,"File "+url +" not found");
			JFileChooser fc = new JFileChooser(Project.getInstance().getProjectPath());
			FileFilter ff = null;
			ff = new FileNameExtensionFilter("Media", "avi", "mp4", "m4v","mpg", "mpeg", "wmv", "mov", "FLV");

			if(ff!=null){
				fc.setFileFilter(ff);
			}

			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				url=fc.getSelectedFile().getPath();
				Project.getInstance().setMediaPath(fc.getSelectedFile().getParent()+"/");
			}else{
				return;
			}
		}
		final VideoTrack tmp = new VideoTrack(name, url, active);
		tmp.setSendToMatlab(sendToMatlab);
		vtracks.add(tmp);
		//tmp.prepare();
		Project.getInstance().setProjectLength(tmp.getLength());
		Project.getInstance().setProjectFPS(tmp.getFPS());
		
	}
	public void addAudioTrack(String name, String url, boolean active, boolean sendToMatlab){
		if(HelperFunctions.fileExists(url)){//given path correct
		}else if(HelperFunctions.fileExists(Project.getInstance().getProjectPath()+"media/"+new File(url).getName())){ //search in media folder
			url=Project.getInstance().getProjectPath()+"media/"+new File(url).getName();
		}else if(HelperFunctions.fileExists(Project.getInstance().getMediaPath()+new File(url).getName())){ //search in alternative path
			url = Project.getInstance().getMediaPath()+new File(url).getName();
		}else{//ask user
			JOptionPane.showMessageDialog(null,"File "+url +" not found");
			JFileChooser fc = new JFileChooser(Project.getInstance().getProjectPath());
			FileFilter ff = null;
			ff = new FileNameExtensionFilter("Media", "wav", "mp3");

			if(ff!=null){
				fc.setFileFilter(ff);
			}

			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				url=fc.getSelectedFile().getPath();
				Project.getInstance().setMediaPath(fc.getSelectedFile().getParent()+"/");
			}else{
				return;
			}

		}
		final AudioTrack tmp = new AudioTrack(name, url, active);
		tmp.setSendToMatlab(sendToMatlab);
		atracks.add(tmp);		
		Project.getInstance().setProjectLength(tmp.getLength());
	}

	public void setTime(long t){
		if(vtracks!=null){
			Iterator<VideoTrack> vI = vtracks.iterator();
			while(vI.hasNext()){
				vI.next().setTime(t);
			}
		}		
		if(atracks!=null){
			Iterator<AudioTrack> aI = atracks.iterator();
			while(aI.hasNext()){				
				aI.next().setTime(t);
			}
		}				

	}

	public void nextFrame(){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			VideoTrack vt = vI.next();
			if(vt.isActive()){
				MessageManager.getInstance().timeChanged(new TimeEvent(this, (long)(Project.getInstance().getTime()+(1.0/vt.getFPS())*1000)));
				break;
			}
		}				
	}
	public void prevFrame(){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			VideoTrack vt = vI.next();
			if(vt.isActive()){
				MessageManager.getInstance().timeChanged(new TimeEvent(this, (long)(Project.getInstance().getTime()-(1.0/vt.getFPS())*1000)));
				break;
			}
		}				
	}
	public void startover(){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			vI.next().startover();	    	
		}
		aI =  atracks.iterator();
		while (aI.hasNext()) {
			aI.next().startover();
		}
	}
	public void pause(){		
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			vI.next().pause();	    	
		}
		aI =  atracks.iterator();
		while (aI.hasNext()) {
			aI.next().pause();
		}
	}
	public void play(){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			vI.next().play();
		}
		aI =  atracks.iterator();
		while (aI.hasNext()) {
			aI.next().play();
		}
	}
	public void setVideoSize(double factor){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			vI.next().setVideoZoom(factor);
		}
		this.videoSize = factor;
	}
	public void removeTrack(Object ot){
		if(ot instanceof VideoTrack){
			((VideoTrack) ot).dispose();
			vtracks.remove((VideoTrack)ot);
		}else if(ot instanceof AudioTrack){
			((AudioTrack) ot).pause();
			atracks.remove((AudioTrack)ot);
		}
	}
	public void muteVideo(boolean bool){
		vI =  vtracks.iterator();
		while (vI.hasNext()) {
			vI.next().setMute(bool);
		}
	}
	public boolean hasAudioTrackWithName(String name){
		boolean found = false;
		Iterator<AudioTrack> ai = atracks.iterator();
		while (ai.hasNext()){
			if(ai.next().getName().equalsIgnoreCase(name))found=true;
		}
		return found;
	}
	public boolean hasVideoTrackWithName(String name){
		boolean found = false;
		Iterator<VideoTrack> vi = vtracks.iterator();
		while (vi.hasNext()){
			if(vi.next().getName().equalsIgnoreCase(name))found=true;
		}
		return found;
	}
	public AudioTrack getAudioTrackByName(String name){
		Iterator<AudioTrack> ai = atracks.iterator();
		while (ai.hasNext()){
			AudioTrack a= ai.next();
			if(a.getName().equalsIgnoreCase(name))return a;
		}
		return null;
	}
	public VideoTrack getVideoTrackByName(String name){
		Iterator<VideoTrack> vi = vtracks.iterator();
		while (vi.hasNext()){
			VideoTrack v= vi.next();
			if(v.getName().equalsIgnoreCase(name))return v;
		}
		return null;
	}

	public double getVideoSize() {
		return videoSize;
	}
}
