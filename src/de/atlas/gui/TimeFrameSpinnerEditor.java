package de.atlas.gui;

import java.text.DecimalFormat;


import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class TimeFrameSpinnerEditor extends JSpinner.DefaultEditor{

	 private static final long serialVersionUID = 1L;

     private DecimalFormat format;
     private JSpinner spinner;

     public TimeFrameSpinnerEditor(JSpinner spinner) {
         super(spinner);    
         this.spinner=spinner;
         format = new DecimalFormat();
         initTextField();
     }

     public TimeFrameSpinnerEditor(JSpinner spinner, String dateFormatPattern) {
         super(spinner);
         this.spinner=spinner;
         
         format = new DecimalFormat(dateFormatPattern);
         initTextField();
     }

     public DecimalFormat getFormat() {
         return format;
     }

     public SpinnerDateModel getModel() {
         return (SpinnerDateModel) this.spinner.getModel();
     }

     private void initTextField() {
    	 NumberFormatter formatter = new NumberFormatter(format);
         JFormattedTextField textField = getTextField();
         textField.setFormatterFactory(new DefaultFormatterFactory(formatter));
         textField.setEditable(true);
     }
}
