package de.atlas.misc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import javax.swing.JOptionPane;

import de.atlas.collections.LabelTrack;
import de.atlas.exceptions.FilterSizeException;
import de.atlas.data.LabelObject;

public class HelperFunctions {
	public static File testAndGenerateFile(String name){
		File tmp = new File(name);
		if(tmp.exists()){
			Object[] options = {"ok"};
			JOptionPane.showOptionDialog(null, "File already exists: "+name, "Attention!", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null,options, options[0]);
			return null;				
		}else{
			return tmp;
		}		
	}
	public static boolean fileExists(String name){
		File tmp = new File(name);
		if(tmp.exists()){
			return true;
		}else{
			return false;
		}		
	}

	public static double[] toRawFile(double[][] data, boolean hasTimeInFirstColumn, String path) {
		double[] mm = new double[2];
		mm[0]=Double.MAX_VALUE;
		mm[1]=Double.MIN_VALUE;
		try {
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileChannel fChannel = raf.getChannel();

			DoubleBuffer dB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, data.length * (Double.SIZE / 8) * data.length>0?data[0].length:0).asDoubleBuffer();
			for(int i=0;i<data.length;i++){
				for(int j=0;j<data[0].length;j++){
					dB.put(data[i][j]);
					if(j!=0||!hasTimeInFirstColumn){
						mm[0]=Double.min(mm[0],data[i][j]);
						mm[1]=Double.max(mm[1],data[i][j]);
					}
				}
			}
			raf.close();
			return mm;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public enum filterType {AVERAGE,MEDIAN}

	public static double[] signalFilter(double[] signal, int filterSize, filterType filterType) throws FilterSizeException {
		if(filterSize%2==0) throw new FilterSizeException("Filtersize not odd");
		if(filterSize>signal.length) throw new FilterSizeException("Signal shorter than filtersize");
		int filterOffset = filterSize/2;
		double[] in = new double[signal.length+2*filterOffset];
		double[] out = new double[signal.length];
		for(int i=0;i<filterOffset;i++){
//            in[i]=signal[filterOffset-i];
//            in[signal.length+filterOffset+i]=signal[signal.length-filterOffset-i];
            in[i]=signal[0];
            in[signal.length+filterOffset+i]=signal[signal.length-1];
		}
		for(int i=0;i<signal.length;i++){
			in[i+filterOffset]=signal[i];
		}

		for(int i=0;i<in.length-2*filterOffset;i++){
			double[] window = new double[filterSize];
			for(int w=0;w<window.length;w++){
				window[w]=in[i+w];
			}
			switch (filterType){
			case AVERAGE:
				out[i]=mean(window);
				break;
			case MEDIAN:
				out[i]=median(window);
				break;
			}
		}
		return out;
	}
	public static double median(double[] m) {
		java.util.Arrays.sort( m );	    
		int middle = m.length/2;
		if (m.length%2 == 1) {
			return m[middle];
		} else {
			return (m[middle-1] + m[middle]) / 2.0;
		}
	}
	public static double mean(double[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return sum / m.length;
	}
	public static double[][] transpose(double[][] array){
		double[][] out = new double[array[0].length][array.length];
		for(int row = 0; row < array.length; row++) // Loop over rows
			for(int col = 0; col < array[row].length; col++) // Loop over columns
				out[col][row] = array[row][col]; // Rotate
		return out;
	}
	public static double max(double[] ds) {
		double max = Double.MAX_VALUE*-1;
		for(int i=0;i<ds.length;i++)if(ds[i]>max)max=ds[i];
		return max;
	}
	public static int argmax(double[] ds) {
		double max = Double.MAX_VALUE*-1;
		int maxIdx = -1;
		for(int i=0;i<ds.length;i++){
			if(ds[i]>max){
				max=ds[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	public static void copyLabels(LabelTrack source, LabelTrack dst){
		if(source.getLabelClass() == dst.getLabelClass()){
			Iterator<LabelObject> iL = source.getLabels().iterator();
			while(iL.hasNext())dst.addLabel(iL.next());
		}
	}
}
