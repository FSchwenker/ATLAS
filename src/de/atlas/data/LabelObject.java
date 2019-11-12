package de.atlas.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import de.atlas.misc.*;

public class LabelObject implements Comparable<LabelObject>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/*private float hue=0.3f;
	private float sat=0.5f;
	private float bri=0.85f;*/
	private double value=1.0;
	private Long start=0l;
    private Long end=0l;
    private Long oldEnd=0l;

    //private Color color = Color.getHSBColor(hue, sat, bri);
	private String text="";
	private String comment="";		
	private LabelClassEntity labelClassEntity;
	private LabelClass labelClass;
	private LabelType labelType = LabelType.MANUAL;
	private long timestamp;
	private ArrayList<SamplePoint> samplePoints = new ArrayList<SamplePoint>();
    private Interpolator interpolator;
    private boolean interpolationValid = false;
    private int interpolationType;
    private boolean showAsFlag = false;


    public LabelObject(String txt, long start, long end, long timestamp){
		this.text=txt;
		this.start=start;
		this.end=end;
		this.timestamp=timestamp;
        initSpline();
					
	}
	
	public LabelObject(String txt, long start, long end, LabelType type, long timestamp){
		this.text=txt;
		this.start=start;
		this.end=end;
		this.timestamp=timestamp;							
		this.labelType=type;
        initSpline();
	}
	
	public LabelObject(String txt, String cmt, long start, long end,double val, LabelType type, LabelClassEntity cl, long timestamp){
		this.text=txt;
		this.comment=cmt;
		this.start=start;
		this.end=end;
		this.timestamp=timestamp;
		this.value=val;
		this.labelType=type;
		this.labelClassEntity=cl;
        initSpline();
	}
	public LabelObject(String txt, String cmt, long start, long end,double val, LabelType type, LabelClass lc, LabelClassEntity lce, long timestamp){
		this.text=txt;
		this.comment=cmt;
		this.start=start;
		this.end=end;
		this.timestamp=timestamp;
		this.value=val;
		this.labelType=type;
		this.labelClass=lc;
		this.labelClassEntity=lce;
        initSpline();
	}
	private void initSpline(){
        samplePoints.add(new SamplePoint(0,0.5));
        samplePoints.add(new SamplePoint((end-start)/2,0.5));
        samplePoints.add(new SamplePoint(end-start,0.5));
        interpolationValid = false;
	}
    public void setSamplePoints(ArrayList<SamplePoint> samplePoints){
        this.samplePoints = samplePoints;
        interpolationValid = false;

    }
	public LabelType getLabelType() {
		return labelType;
	}

	public long getTimestamp() {
		return timestamp;
	}


	public void setLabelType(LabelType labelType) {
		this.labelType = labelType;
	}


	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
	public LabelClassEntity getLabelClassEntity() {
		return labelClassEntity;
	}

	public void setLabelClassEntity(LabelClassEntity entity) {
		this.labelClassEntity = entity;
	}
	
	public LabelClass getLabelClass() {
		return labelClass;
	}

	public void setLabelClass(LabelClass val) {
		this.labelClass = val;
	}
	
	public Color getColor(){	
		if(this.labelClassEntity!=null){
			return this.labelClassEntity.getColor();
		}else{
			return AtlasProperties.getInstance().getStandardObjectColor();
		}
	}
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

    public boolean isShowAsFlag() {
        return showAsFlag;
    }

    public void setShowAsFlag(boolean showAsFlag) {
        if(this.showAsFlag==false&&showAsFlag==true){
            oldEnd=end;
            end = start+1;
        }
        if(this.showAsFlag==true&&showAsFlag==false&&oldEnd>0){
            end=oldEnd;
        }
        this.showAsFlag = showAsFlag;
    }

    public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(long start) {
        if(start>=this.end)return;
        double diff = (this.end-this.start)-(this.end-start);
        Iterator<SamplePoint> iS = samplePoints.iterator();
        for(int i=0; iS.hasNext(); i++) {
            SamplePoint sp = iS.next();
            sp.setX(sp.getX() - i * (diff / (samplePoints.size()-1)));

        }
        interpolationValid = false;

        this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(long end) {
        if(this.start>=end)return;
        double diff = (this.end-this.start)-(end-this.start);
        Iterator<SamplePoint> iS = samplePoints.iterator();
        for(int i=0; iS.hasNext(); i++) {
            SamplePoint sp = iS.next();
            sp.setX(sp.getX() - i * (diff / (samplePoints.size() - 1)));

        }
        interpolationValid = false;

        this.end = end;
	}

	@Override
	public int compareTo(LabelObject n) {
		 int lastCmp = this.getStart().compareTo(n.getStart());
	     return (lastCmp != 0 ? lastCmp : this.getStart().compareTo(n.getStart()));
	}

	public boolean hasTimePoint(long time) {
		if(time>=this.start&&time<=this.end){
			return true;
		}else{
			return false;
		}
	}

    public ArrayList<SamplePoint> getSamplePoints() {
        return samplePoints;
    }
    public SamplePoint getSamplePoint(int i) {
        if(i>=0&&i< samplePoints.size()){
            return samplePoints.get(i);
        }
        return null;
    }

    public boolean hasSampleCloseTo(double xInTime, double yInSpace, double dx, double dy) {
        return getSamplePoint(xInTime, yInSpace, dx, dy)!=null;
    }

    public SamplePoint getSamplePoint(double xInTime, double yInSpace, double dx, double dy) {
        Iterator<SamplePoint> iS = samplePoints.iterator();
        while (iS.hasNext()) {
            SamplePoint sp = iS.next();
            if (xInTime - dx < sp.getX() && xInTime + dx >= sp.getX() &&
                    yInSpace - dy < sp.getY() && yInSpace + dy >= sp.getY())
                return sp;
        }
        return null;
    }

    public void addSamplePoint(SamplePoint newSamplePoint) {
        // x between which two samplePoints?
        Iterator<SamplePoint> iS = samplePoints.iterator();
        while (iS.hasNext()) {
            SamplePoint sp = iS.next();
            if(sp.getX()> newSamplePoint.getX()){
                int idx = samplePoints.indexOf(sp);
                if( idx>0&&idx< samplePoints.size()){
                    samplePoints.add(idx, newSamplePoint);
                    interpolationValid = false;
                    break;
                }
            }
        }
    }
    public double getInterpolationValueAt(double time, int interpolationType){
        //System.out.println(interpolationValid);
        if((!interpolationValid||this.interpolationType!=interpolationType)&&samplePoints.size()>1){
            switch(interpolationType){
                case Interpolator.LINEAR:
                    interpolator = new LinearInterpolator(samplePoints);
                    break;
                case Interpolator.BSPLINE:
                    interpolator = new SplineInterpolator(samplePoints);
                    break;
                case Interpolator.MEAN:
                    interpolator = new MeanInterpolator(samplePoints);
                    break;
                case Interpolator.STAIRS:
                    interpolator = new StairInterpolator(samplePoints);
                    break;
            }
            interpolationValid = true;
            this.interpolationType = interpolationType;
        }
        if(interpolator!=null&&interpolator.isValidPoint(time))return interpolator.getInterpolationValueAt(time);
        return .5;
    }

    public void interpolationValid(boolean b) {
        this.interpolationValid = b;
    }
}
