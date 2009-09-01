package com.solidosystems.ravenous.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.*;
import java.net.URL;
import java.awt.event.*;
import java.awt.Desktop;
import java.net.URI;

import java.io.File;
import java.util.List;

public class HostListFrame{
    private JFrame frame;
    private JToolBar toolBar;
    private JButton add,edit,delete;
    private AbstractTableModel dataModel;
    JTable table;
    protected List<DevelopmentHost> lst;
    
    private JDialog addDialog;
    private JButton createHost,cancelCreate;
    private JTextField chostname,cpath,clisten,cport;
    
    private JDialog editDialog;
    private JButton updateHost,cancelEdit;
    private JTextField ehostname,epath,elisten,eport;
    
    
    public HostListFrame(){
        initFrame();
        initDialogs();
    }
    
    public void add(){
        chostname.setText("");
        cpath.setText("");
        clisten.setText("*");
        try{
            cport.setText(""+DevelopmentClient.get().getNextPort());
        }catch(Exception e){
            e.printStackTrace();
        }
        addDialog.setVisible(true);
    }
    
    public void edit(){
        if(table.getSelectedRow()>-1){
            DevelopmentHost hst=lst.get(table.getSelectedRow());
            ehostname.setText(hst.getHostName());
            epath.setText(hst.getPath());
            elisten.setText(hst.getIP());
            try{
                eport.setText(""+hst.getPort());
            }catch(Exception e){
                e.printStackTrace();
            }
            editDialog.setVisible(true);
        }
    }
    
    public void delete(){
        if(table.getSelectedRow()>-1){
            try{
                DevelopmentHost hst=lst.get(table.getSelectedRow());
                DevelopmentClient.get().deleteHost(hst.getId());
                dataModel.fireTableDataChanged();
            }catch(Exception e){
                e.printStackTrace();
            }
        } 
    }
    
    private boolean checkPort(){
        try{
            return DevelopmentClient.get().isUniquePort(cport.getText());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkPortForSelected(){
        try{
            return DevelopmentClient.get().isUniquePort(eport.getText(),lst.get(table.getSelectedRow()).getId());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkHostnameForSelected(){
        try{
            return DevelopmentClient.get().isUniqueHost(ehostname.getText(),lst.get(table.getSelectedRow()).getId());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkHostname(){
        try{
            return DevelopmentClient.get().isUniqueHost(chostname.getText());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkPath(){
        try{
            File ftest=new File(cpath.getText());
            if(ftest.isDirectory())return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkEditPath(){
        try{
            File ftest=new File(epath.getText());
            if(ftest.isDirectory())return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean createNewHost(){
        try{
             return DevelopmentClient.get().createNewHost(chostname.getText(),cpath.getText(),clisten.getText(),cport.getText());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean saveSelectedHost(){
        try{
             return DevelopmentClient.get().saveHost(lst.get(table.getSelectedRow()).getId(),ehostname.getText(),epath.getText(),elisten.getText(),eport.getText());
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private void initDialogs(){
        addDialog=new JDialog(frame,"Add Host",true);
        Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        addDialog.setBounds((int)screen.getWidth()/2-200,(int)screen.getHeight()/2-150,400,300);
        addDialog.getContentPane().setLayout(new BorderLayout());
        
        JPanel p=new JPanel();
        p.setLayout(new GridLayout(1,3));
        p.add(new JPanel());
        cancelCreate=new JButton("Cancel");
        cancelCreate.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                addDialog.setVisible(false);
            }
        });
        p.add(cancelCreate);
        createHost=new JButton("Create");
        createHost.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(chostname.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(addDialog, "Hostname can not be blank", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(clisten.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(addDialog, "You must enter a listen IP. Use * for all local IP's", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(cpath.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(addDialog, "You must enter an path for the host root!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(cport.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(addDialog, "You must enter a port!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkPort()){
                    JOptionPane.showMessageDialog(addDialog, "You must enter a unique port!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkPath()){
                    JOptionPane.showMessageDialog(addDialog, "You must enter an existing folder for the host root!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkHostname()){
                    JOptionPane.showMessageDialog(addDialog, "You must enter a unique host name!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    if(createNewHost()){
                        addDialog.setVisible(false);
                        dataModel.fireTableDataChanged();
                    }else{
                        JOptionPane.showMessageDialog(addDialog, "Could not create new host!", "Internal Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        p.add(createHost);
        addDialog.add(p,BorderLayout.SOUTH);
        
        p=new JPanel();
        p.setLayout(new GridLayout(3,1));
        JPanel sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Hostname"),BorderLayout.WEST);
        chostname=new JTextField();
        sub.add(chostname,BorderLayout.CENTER);
        p.add(sub);
        sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Path"),BorderLayout.WEST);
        cpath=new JTextField();
        sub.add(cpath,BorderLayout.CENTER);
        JButton addFileDialog=new JButton("Choose");
        addFileDialog.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                JFileChooser choose=new JFileChooser();
                choose.setDialogTitle("Select root folder for host");
                choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(choose.showDialog(addDialog, "Select folder")==JFileChooser.APPROVE_OPTION){
                    cpath.setText(choose.getSelectedFile().getAbsolutePath());
                }
            }
        });
        sub.add(addFileDialog,BorderLayout.EAST);
        p.add(sub);
        
        sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Listen"),BorderLayout.WEST);
        clisten=new JTextField();
        sub.add(clisten,BorderLayout.CENTER);
        cport=new JTextField();
        sub.add(cport,BorderLayout.EAST);
        p.add(sub);
        
        addDialog.add(p,BorderLayout.NORTH);
        
        // ******************************************************
        // Edit Dialog
        
        editDialog=new JDialog(frame,"Edit Host",true);
        editDialog.setBounds((int)screen.getWidth()/2-200,(int)screen.getHeight()/2-150,400,300);
        editDialog.getContentPane().setLayout(new BorderLayout());
        
        p=new JPanel();
        p.setLayout(new GridLayout(1,3));
        p.add(new JPanel());
        cancelEdit=new JButton("Cancel");
        cancelEdit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                editDialog.setVisible(false);
            }
        });
        p.add(cancelEdit);
        updateHost=new JButton("Save");
        updateHost.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(ehostname.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(editDialog, "Hostname can not be blank", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(elisten.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(editDialog, "You must enter a listen IP. Use * for all local IP's", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(epath.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(editDialog, "You must enter a path for the host root!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(eport.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(editDialog, "You must enter a port!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkPortForSelected()){
                    JOptionPane.showMessageDialog(editDialog, "You must enter a unique port!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkEditPath()){
                    JOptionPane.showMessageDialog(editDialog, "You must enter an existing folder for the host root!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else if(!checkHostnameForSelected()){
                    JOptionPane.showMessageDialog(editDialog, "You must enter a unique host name!", "Data Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    if(saveSelectedHost()){
                        editDialog.setVisible(false);
                        dataModel.fireTableDataChanged();
                    }else{
                        JOptionPane.showMessageDialog(editDialog, "Could not save host!", "Internal Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        p.add(updateHost);
        editDialog.add(p,BorderLayout.SOUTH);
        
        p=new JPanel();
        p.setLayout(new GridLayout(3,1));
        sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Hostname"),BorderLayout.WEST);
        ehostname=new JTextField();
        sub.add(ehostname,BorderLayout.CENTER);
        p.add(sub);
        sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Path"),BorderLayout.WEST);
        epath=new JTextField();
        sub.add(epath,BorderLayout.CENTER);
        addFileDialog=new JButton("Choose");
        addFileDialog.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                JFileChooser choose=new JFileChooser();
                choose.setDialogTitle("Select root folder for host");
                choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(choose.showDialog(addDialog, "Select folder")==JFileChooser.APPROVE_OPTION){
                    epath.setText(choose.getSelectedFile().getAbsolutePath());
                }
            }
        });
        sub.add(addFileDialog,BorderLayout.EAST);
        p.add(sub);
        
        sub=new JPanel();
        sub.setLayout(new BorderLayout());
        sub.add(new JLabel("Listen"),BorderLayout.WEST);
        elisten=new JTextField();
        sub.add(elisten,BorderLayout.CENTER);
        eport=new JTextField();
        sub.add(eport,BorderLayout.EAST);
        p.add(sub);
        
        editDialog.add(p,BorderLayout.NORTH);
        
        
    }
    
    private void initFrame(){
        frame=new JFrame("Ravenous Development Client");
        frame.getContentPane().setLayout(new BorderLayout());
        dataModel = new AbstractTableModel() {
            
            public void fireTableDataChanged(){
                try{
                    lst=DevelopmentClient.get().getHostList();
                }catch(Exception e){
                    e.printStackTrace();
                }
                super.fireTableDataChanged();
            }
            private static final long serialVersionUID=0;
            public int getColumnCount() { return 3; }
            public int getRowCount() { return lst.size();}
            public Object getValueAt(int row, int col) { 
                DevelopmentHost hst=lst.get(row);
                if(col==0)return hst.getHostName();
                if(col==1)return hst.getPort();
                // if(col==2)return new Boolean(true);
                if(col==2)return hst.getPath();
                return "<Unknown>"; }
            public String getColumnName(int col){
                switch(col){
                    case 0:return "Host name";
                    case 1:return "Port";
                    // case 2:return "Status";
                    case 2:return "Path";
                }
                return "Unknown";
            }
        };
        dataModel.fireTableDataChanged();
        table = new JTable(dataModel);
        TableColumnModel cm=table.getColumnModel();
        TableColumn tc=cm.getColumn(1);
        tc.setMaxWidth(50);
        tc.setMinWidth(50);
        // tc=cm.getColumn(2);
        // tc.setMaxWidth(50);
        // tc.setMinWidth(50);
        JScrollPane scrollpane = new JScrollPane(table);
        frame.getContentPane().add(scrollpane,BorderLayout.CENTER);
        
        toolBar = new JToolBar("Host Configuration Toolbar");
        toolBar.setFloatable(false);
        frame.getContentPane().add(toolBar,BorderLayout.PAGE_START);
        
        add=createButton("toolbarButtonGraphics/general/Add24.gif","Add host");
        toolBar.add(add);
        add.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){add();}
        });
        edit=createButton("toolbarButtonGraphics/general/Edit24.gif","Edit host");
        toolBar.add(edit);
        edit.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){edit();}
        });
        delete=createButton("toolbarButtonGraphics/general/Delete24.gif","Delete host");
        toolBar.add(delete);
        delete.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){delete();}
        });
        
        toolBar.addSeparator();
        
        JButton showBrowser=createButton("toolbarButtonGraphics/development/WebComponent24.gif","Launch browser");
        showBrowser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(table.getSelectedRow()>-1){
                    try{
                        DevelopmentHost hst=lst.get(table.getSelectedRow());
                        String uri="http://";
                        if(hst.getIP().equals("*")){
                            uri+="127.0.0.1";
                        }else uri+=hst.getIP();
                        uri+=":"+hst.getPort()+"/";
                        Desktop.getDesktop().browse(new URI(uri));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        toolBar.add(showBrowser);
        
        Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((int)screen.getWidth()/2-300,(int)screen.getHeight()/2-200,600,400);
        frame.setVisible(true);
    }
    
    private JButton createButton(String icon,String alt){
        String imgLocation = "/icons/"+icon;
        URL imageURL = DevelopmentClient.class.getResource(imgLocation);
        JButton button = new JButton();
        button.setIcon(new ImageIcon(imageURL, alt));
        return button;
    }
}