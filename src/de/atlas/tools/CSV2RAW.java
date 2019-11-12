package de.atlas.tools; /**
 * Created by smeudt on 15.10.14.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static de.atlas.misc.HelperFunctions.testAndGenerateFile;

public class CSV2RAW {
    private static String csvSplitRegex_ = "[,;\t]";

    public static void main( String[] args){
        if(args.length<1){
            System.out.println("no input file");
            return;
        }
        if (!args[0].endsWith(".csv")){
            System.out.println("not a CSV");
            return;
        }
        RandomAccessFile in;
        try {
            ArrayList<DoubleBuffer> files = new ArrayList<DoubleBuffer>();
            ArrayList<RandomAccessFile> rafs = new ArrayList<RandomAccessFile>();

            in = new RandomAccessFile(args[0],"r");

            String line = "";
            int length = 1;
            int dimension = in.readLine().split(csvSplitRegex_).length - 1;
            double[] minimas = new double[dimension];
            double[] maximas = new double[dimension];

            while ((line = in.readLine()) != null) {
                length++;
            }

            in.seek(0);

            ArrayList<String> names = new ArrayList<String>();
            boolean hasHeaderLine = false;
            String[] splitLine = in.readLine().split(csvSplitRegex_);
            for (int i = 0; i < dimension; i++) {
                try {
                    Double.parseDouble(splitLine[i + 1]);
                } catch (Exception e) {
                    // not parsable as a double - must be header line
                    // with names
                    hasHeaderLine = true;
                }
            }

            for (int i = 0; i < dimension; i++) {
                minimas[i] = Double.MAX_VALUE;
                maximas[i] = -1.0 * Double.MAX_VALUE;
                String trackName = "";
                // first splitLine entry is dummy, so length must be
                // dimension +1
                // just check for greater dimension and ignore following
                // entries -
                if (hasHeaderLine && splitLine.length > dimension
                        && !splitLine[i + 1].isEmpty()) {
                    trackName = splitLine[i + 1];
                } else {
                    if (dimension == 1) {
                        trackName = args[0];
                    } else {
                        trackName = args[0]  + "_" + i;
                    }
                }
                if (trackName.isEmpty()) {
                    trackName = "ERROR track " + i;
                    System.err.println(trackName);
                }
                names.add(trackName);

                File file;
                if ((file = testAndGenerateFile(names.get(i) + ".raw")) == null) {
                    return;
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                rafs.add(raf);
                FileChannel fChannel = raf.getChannel();
                DoubleBuffer dB = fChannel.map(
                        FileChannel.MapMode.READ_WRITE, 0,
                        length * (Double.SIZE / 8) * 2)
                        .asDoubleBuffer();
                files.add(dB);
            }

            // redo first line if it was not header line
            if (!hasHeaderLine) {
                in.seek(0);
            }

            while ((line = in.readLine()) != null) {
                String[] vals = line.split(csvSplitRegex_);
                if (vals.length != dimension + 1) {
                    continue;
                }
                for (int i = 1; i < vals.length; i++) {
                    files.get(i - 1).put(Double.parseDouble(vals[0]));// zeitpunkt;
                    files.get(i - 1).put(Double.parseDouble(vals[i]));// wert;

                    // if(count>1){
                    if (Double.parseDouble(vals[i]) > maximas[i - 1]) {
                        maximas[i - 1] = Double.parseDouble(vals[i]);
                    }
                    if (Double.parseDouble(vals[i]) < minimas[i - 1]) {
                        minimas[i - 1] = Double.parseDouble(vals[i]);
                    }
                    // }
                }
                // count++;
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
