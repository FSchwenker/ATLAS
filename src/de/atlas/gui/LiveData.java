package de.atlas.gui;

import de.atlas.collections.ObjectLine;
import de.atlas.collections.ScalarTrack;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.TimeEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.AtlasProperties;
import de.atlas.misc.HelperFunctions;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by smeudt on 19.03.2018.
 */
public class LiveData extends JFrame {

    private JPanel contentPane;
    private JButton btnConnect;
    private JButton btnStart;
    private JCheckBox chkClear;
    private JCheckBox chkCreate;
    private JTextField txtUrl;
    private JTextField txtPort;
    private JCheckBoxList lstTracks;

    DefaultListModel<JCheckBox> listModel;

    private Socket socket;
    private String[] encoding;
    private String[] head;
    private ScalarTrack[] hotScalarTracks;
    private int timeIdx = 0;
    boolean stopThread = false;
    ReadThread readThread = new ReadThread();

    int guiRefreshInterval = 1000;


    public LiveData(){
        setResizable(false);
        setTitle("LiveData");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 490, 475);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblProvider = new JLabel("Data provider:");
        lblProvider.setBounds(20,20,150,20);
        contentPane.add(lblProvider);
        JLabel lblUrl = new JLabel("Server address:");
        lblUrl.setBounds(20,60,150,20);
        contentPane.add(lblUrl);
        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(370,60,150,20);
        contentPane.add(lblPort);

        txtUrl = new JTextField(AtlasProperties.getInstance().getLiveDataURL());
        txtUrl.setBounds(125,60,200,20);
        contentPane.add(txtUrl);
        txtPort = new JTextField(String.valueOf(AtlasProperties.getInstance().getLiveDataPort()));
        txtPort.setBounds(410,60,50,20);
        contentPane.add(txtPort);

        btnConnect = new JButton("Connect");
        btnConnect.setBounds(360,100,100,40);
        contentPane.add(btnConnect);
        btnStart = new JButton("Start");
        btnStart.setBounds(360,370,100,40);
        btnStart.setEnabled(false);
        contentPane.add(btnStart);

        chkClear = new JCheckBox("Clear existing DataTracks");
        chkClear.setBounds(270,200,200,30);
        contentPane.add(chkClear);
        chkCreate = new JCheckBox("Create missing DataTracks");
        chkCreate.setBounds(270,240,200,30);
        contentPane.add(chkCreate);

        JSeparator sep = new JSeparator();
        sep.setBounds(10,160,460,5);
        contentPane.add(sep);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 190, 220, 220);
        contentPane.add(scrollPane);
        listModel = new DefaultListModel<>();
        lstTracks = new JCheckBoxList(listModel);
        scrollPane.setViewportView(lstTracks);

        AtlasProperties.getInstance().addJFrameBoundsWatcher("liveData", this, true, false);

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnConnect.getText().equalsIgnoreCase("connect")){
                    connect();
                }else{
                    disconnect();
                }
            }
        });
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnStart.getText().equalsIgnoreCase("start")){
                    start();
                }else{
                    stop();
                }
            }
        });

    }

    private void connect() {
        btnConnect.setText("Disconnect");
        btnStart.setText("Start");
        btnStart.setEnabled(true);

        String host = txtUrl.getText().replace("\\s", "");
        int port = Integer.parseInt(txtPort.getText().replace("\\s", ""));

        try {
            socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String hello = in.readLine();
            if (!hello.startsWith("hello")) System.err.println("Rude server didn't said hello to me");

            String header = in.readLine();
            head = header.replaceAll("\\s", "").split(",");

            String code = in.readLine().replaceAll("!", "").replaceAll("\\s", "");
            Pattern pattern = Pattern.compile("(\\d*)\\D");
            Matcher matcher = pattern.matcher(code);
            encoding = new String[head.length];
            int column = 0;
            int index = 0;
            while (matcher.find()) {
                encoding[column++] = (code.substring(index, matcher.end()));
                index = matcher.end();
            }

            listModel.clear();

            hotScalarTracks = new ScalarTrack[head.length];
            for(int i=0;i<head.length;i++){
                JCheckBox box = new JCheckBox(head[i] + " - " + formatStringToString(encoding[i]));
                if (head[i].equalsIgnoreCase("time"))box.setEnabled(false);
                if (encoding[i].endsWith("s"))box.setEnabled(false);
                ObjectLine ol = Project.getInstance().getLcoll().getTrackByName(head[i]);
                if(ol!=null&&ol.getTrack() instanceof ScalarTrack) {
                    box.setForeground(Color.RED);
                }else{
                    box.setForeground(Color.BLUE);
                }
                listModel.addElement(box);
            }

            AtlasProperties.getInstance().setLiveDataURL(txtUrl.getText());
            AtlasProperties.getInstance().setLiveDataPort(port);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            btnConnect.setText("Connect");
            btnStart.setText("Start");
            btnStart.setEnabled(false);

        } catch (IOException e) {
            e.printStackTrace();
            btnConnect.setText("Connect");
            btnStart.setText("Start");
            btnStart.setEnabled(false);
        }
    }

    private void disconnect(){
        btnConnect.setText("Connect");
        btnStart.setText("Start");
        btnStart.setEnabled(false);
        try {
            stopThread = true;
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("bye");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void start(){
        btnStart.setText("Stop");
        for(int i=0;i<head.length;i++){
            //add oline to hot list (in same order as head)
            //if line checkBox is checked
            //    search for existing oline
            //    if found
            //        add it
            //    else
            //        create it, if checkbox is set
            //if clear checkbox is set, clear oline
            if(head[i].equalsIgnoreCase("time")){
                timeIdx = i;
            }else {
                if(listModel.elementAt(i).isSelected()){
                    ObjectLine ol = Project.getInstance().getLcoll().getTrackByName(head[i]);
                    if(ol!=null&&ol.getTrack() instanceof ScalarTrack){
                        hotScalarTracks[i] = (ScalarTrack)ol.getTrack();
                    }else{
                        if(chkCreate.isSelected()){
                            String fileName = Project.getInstance().getProjectPath() + "datatracks/" + head[i] + ".raw";
                            try {
                                File file = new File(fileName);
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Project.getInstance().getLcoll().addScalarTrack(head[i],fileName,true,false,true,encoding[i].endsWith("i")?-35000:-1,encoding[i].endsWith("i")?35000:1,Project.getInstance().getLcoll().getOlinesSize(),false,ObjectLine.MEDIUM,false);
                            ol = Project.getInstance().getLcoll().getTrackByName(head[i]);
                            hotScalarTracks[i] = (ScalarTrack)ol.getTrack();
                            listModel.getElementAt(i).setForeground(Color.RED);
                            lstTracks.repaint();
                            MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
                        }
                    }
                }
            }
            if(chkClear.isSelected()&&hotScalarTracks[i]!=null)hotScalarTracks[i].clearData();
        }
        try {
            readThread.start();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("go");


        } catch (IOException e) {
            e.printStackTrace();
            btnStart.setText("Start");
        }
    }
    private void stop(){
        btnStart.setText("Start");
        try {
            stopThread = true;
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("pause");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String formatStringToString(String formatString){
        switch(formatString){
            case "L":
                return "Long";
            case "f":
                return "Float";
            case "i":
                return "Integer";
            case "?":
                return "Boolean";
            default:
                if(formatString.endsWith("s"))return "String";
                return "Format unknown";
        }

    }
    private class ReadThread extends Thread{
        public void run(){
            try {
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                int column = 0;
                int size = 0;
                long time = 0;
                long lastTimeGuiUpdated = -1;

                while(!stopThread) {
                    if (column == timeIdx) {
                        size = 8;
                        byte[] b = new byte[size];
                        for (int i = 4; i < size; i++) b[i] = (byte) inputStream.read();
                        time = ByteBuffer.wrap(b).getLong();
                    } else {
                        switch (encoding[column]) {
                            case "L":
                                size = 8;
                                byte[] b = new byte[size];
                                for (int i = 4; i < size; i++) b[i] = (byte) inputStream.read();
                                if(hotScalarTracks[column]!=null)hotScalarTracks[column].appendDataPointUnsafe(time, (double) ByteBuffer.wrap(b).getLong());
                                break;
                            case "f":
                                size = 4;
                                b = new byte[size];
                                for (int i = 0; i < size; i++) b[i] = (byte) inputStream.read();
                                if(hotScalarTracks[column]!=null)hotScalarTracks[column].appendDataPointUnsafe(time,(double) ByteBuffer.wrap(b).getFloat());
                                break;
                            case "i":
                                size = 4;
                                b = new byte[size];
                                for (int i = 0; i < size; i++) b[i] = (byte) inputStream.read();
                                if(hotScalarTracks[column]!=null)hotScalarTracks[column].appendDataPointUnsafe(time,(double) ByteBuffer.wrap(b).getInt());
                                break;
                            case "?":
                                double val = inputStream.read() == 0 ? 0.0 : 1.0;
                                if(hotScalarTracks[column]!=null)hotScalarTracks[column].appendDataPointUnsafe(time,val);
                                break;
                            default:
                                if (encoding[column].endsWith("s")) {
                                    size = Integer.parseInt(encoding[column].substring(0, encoding[column].length() - 1));
                                    char[] c = new char[size];
                                    for (int i = 0; i < size; i++) c[i] = (char) inputStream.read();
                                    //System.out.print(new String(c) + ",");
                                } else {
                                    System.err.println("unknown encoding signature found");
                                }
                        }

                    }
                    column++;
                    if(column % head.length == 0) {
                        column = column % head.length;
                        if(lastTimeGuiUpdated+guiRefreshInterval<time){
                            lastTimeGuiUpdated = time;
                            //Project.getInstance().setProjectLength(time);
                            for(ScalarTrack st:hotScalarTracks)if(st!=null){
                                st.refresh();
                                st.repaint();
                            }
                            MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
                            MessageManager.getInstance().timeChanged(new TimeEvent(this,time));

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class JCheckBoxList extends JList<JCheckBox> {
        protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public JCheckBoxList() {
            setCellRenderer(new CellRenderer());
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {
                        JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                        if(checkbox.isEnabled())checkbox.setSelected(!checkbox.isSelected());
                        repaint();
                    }
                }
            });
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public JCheckBoxList(ListModel<JCheckBox> model){
            this();
            setModel(model);
        }


        protected class CellRenderer implements ListCellRenderer<JCheckBox> {
            public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
                JCheckBox checkbox = value;

                //Drawing checkbox, change the appearance here
                checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
                //checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
                //checkbox.setEnabled(isEnabled());
                checkbox.setFont(getFont());
                checkbox.setFocusPainted(false);
                checkbox.setBorderPainted(true);
                checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
                return checkbox;
            }
        }
    }
}
