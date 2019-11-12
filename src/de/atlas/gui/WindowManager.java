package de.atlas.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import de.atlas.collections.VideoTrack;
import de.atlas.misc.AtlasProperties;

public class WindowManager {

	private static final long serialVersionUID = 1L;
	private static WindowManager instance = new WindowManager();
	private ArrayList<VideoTrack> videoWindows = new ArrayList<VideoTrack>();
	//private ArrayList<Point> points = new ArrayList<Point>();
	private Dimension screenDim; 
	private int xres;
	


	

	public WindowManager(){
		screenDim = Toolkit.getDefaultToolkit().getScreenSize();		
		xres=(int) screenDim.getWidth();
		if(xres>2560){
			xres=2560;
		}
	}

	public static WindowManager getInstance() {
		return instance;
	}

	public void addVideoWindow(VideoTrack f){
		videoWindows.add(f);
		if(AtlasProperties.getInstance().isAutoarranging()){
			autoarrange();
		}
	}
/*	private void arrangeVideos(Rectangle area, ArrayList<VideoTrack> windows){
		//sort windows by areasize
		//ersten punkt ansetzen
		points.add(area.getLocation());
		
		Collections.sort(windows, new Comparator<VideoTrack>(){					
			@Override
			public int compare(VideoTrack o1, VideoTrack o2) {
				//compare over area
				if (o1.getWidth()*o1.getHeight() < o2.getWidth()*o2.getHeight()){
					return-1;
				}
				if (o1.getWidth()*o1.getHeight() > o2.getWidth()*o2.getHeight()){
					return 1;
				}
				return 0;	
			}
		});
		double videoArea = 0.0;
		for(VideoTrack f : videoWindows){			
			if(f.isVisible()){
				videoArea+=f.getWidth()*f.getHeight();
			}
		}
		double screenArea = screenDim.getHeight()*screenDim.getWidth();
		
		if((screenArea - videoArea)<=0){

		}else{
			for(VideoTrack f : windows){
				if(f.isVisible()){//nur sichtbare fenster kommen in frage
					// mit dem größten fenster anfangen
					
					for(Point p : points){
						f.setLocation(p);
						//bewerten...
						if(true){
							//alten entfernen
							points.remove(p);
							// neue hinzufügen
							points.add( new Point( ((int)p.getX()+f.getWidth()),((int)p.getY()) ) );
							
						}
						//neue dockpunkte hinzufügen
						

						
					}
				}				
			}
			
		}
		
	}
	*/
	public void autoarrange(){
		//fläche eines viertelbildschirmsberechnen
		double screenArea = screenDim.getHeight()*screenDim.getWidth();

		//fläche der vorhandenen angezeigten videos berechnen
		double videoArea = 0.0;
		for(VideoTrack f : videoWindows){			
			if(f.isVisible()){
				videoArea+=f.getWidth()*f.getHeight();
			}
		}
		if((screenArea-videoArea)>0){//nur dann is genug platz...
			int x = 0;
			int y = 0;

			for(VideoTrack f : videoWindows){

				if(f.isVisible()){

					double width = f.getSize().getWidth();
					double height = f.getSize().getHeight();
					f.setLocation(x, y);

					//calculate new x
					x = (int) (x+width);

					if(x>=xres*3/4){
						//calculate new y
						x=0;
						y = (int) (y+height);
					}


				}
			}
		}

	}
}
