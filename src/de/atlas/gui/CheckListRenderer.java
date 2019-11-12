package de.atlas.gui;

import de.atlas.collections.AudioTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.collections.VideoTrack;

import javax.swing.*;
import java.awt.*;

/**
 * Created by smeudt on 29.03.2016.
 */
class CheckListRenderer extends JCheckBox implements ListCellRenderer {

    public CheckListRenderer() {
        setBackground(UIManager.getColor("List.textBackground"));
        setForeground(UIManager.getColor("List.textForeground"));
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        if(value instanceof String) {
            setEnabled(false);
        }else{
            setEnabled(list.isEnabled());
        }
        setFont(list.getFont());
        setText(value.toString());

        if(value instanceof ObjectLine)setSelected(((ObjectLine) value).isSendToMatlab());
        if(value instanceof VideoTrack)setSelected(((VideoTrack) value).isSendToMatlab());
        if(value instanceof AudioTrack)setSelected(((AudioTrack) value).isSendToMatlab());

        return this;
    }
}
