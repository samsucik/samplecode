//http://www.java-forums.org/new-java/40627-best-way-solve-multidimentional-array-strings-keys.html
//http://javarevisited.blogspot.sk/2012/01/get-set-default-character-encoding.html
//http://docs.oracle.com/javase/7/docs/api/java/awt/Graphics.html
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class Base extends JFrame implements ActionListener 
{
    public static Base base;
    public static int canvas_x = 0, canvas_y = 0, canvas_width = 900, canvas_height = 768, unit = 23, angleUnit = 10,maxNoOfObstacles = 50, noOfPossibleColors = 5;
    public static double lineConstant = 0.05, marginPercent = 0.9, dogSpeed = 1.5, sct = 0;
    public static JPanel obstacleButtonsPanel = new JPanel(), menuButtonsPanel = new JPanel(), checkBoxPanel = new JPanel();
    public static JButton button, dogHeight;
    public static int noOfObstacles = 12, noOfMenu = 5;
    public static ObstacleButton[] obstacleButtons = new ObstacleButton[noOfObstacles];
    public static MenuButton[] menuButtons = new MenuButton[noOfMenu];
    public static Canvas canvas;
    public static JSlider unitSlider, dogSpeedSlider;
    public static Color colorMain = Color.blue, colorEnds = Color.red, colorArrows = Color.black;
	public static Color[] possibleColors = new Color[noOfPossibleColors];
    public static JCheckBox[] checkBoxButtons = new JCheckBox[noOfPossibleColors+2];
    public static boolean[] drawPath = new boolean[noOfPossibleColors];
    public static boolean addObstaclesEnabled = true, isFileDialogOpened = false, saved = true, paintGrid = true, isLabelActivated = false;
    public static File directory = null;
    public static double[] pathLength = {0,0,0,0,0};
    public static int[] noOfTables = {0,0,0,0,0};
    
    
    //The basis for the application, only one instance of this class is used each time the application is run
    public Base()
    {
        super("My DACM Creator");
        //setting the dimensions of the window, taking into regard the width of the taskbar (this may differ for different operating systems)
        //http://stackoverflow.com/questions/10123735/get-effective-screen-size-from-java
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int taskBarSize = scnMax.top;
        
        this.setSize((int)screenSize.width,(int)screenSize.height-2*taskBarSize);
        //setting the size of the canvas according to the window dimensions
        Base.canvas_height = (int)screenSize.height-2*taskBarSize;
        Base.canvas_width = Math.min((int)screenSize.width-(50+5*65+10),(int)(Base.canvas_height*(297/210.0)));
        Base.unit = (int)(Base.canvas_height/32.0);
        
        //http://stackoverflow.com/questions/479523/java-swing-maximize-window
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);        
        
        this.setLayout(null);
        this.setVisible(true);
     
        Base.obstacleButtonsPanel.setSize(5*65+10,4*65+20);

        //initialising the zoom slider
        unitSlider = new JSlider(JSlider.VERTICAL,20,60,23);
        unitSlider.setMajorTickSpacing(5);
        unitSlider.setMinorTickSpacing(1);
        unitSlider.setPaintLabels(true);
        unitSlider.setPaintTicks(true);
        unitSlider.setSnapToTicks(true);
        unitSlider.setFocusable(false);   
    	unitSlider.setBounds(4*65+5,5,65,3*65-5);
    	unitSlider.setToolTipText("Zoom");
        
    	//initialising the dog speed (travel rate) slider
        dogSpeedSlider = new JSlider(JSlider.VERTICAL,150,370,150);
        dogSpeedSlider.setMajorTickSpacing(20);
        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
        for (int i = 0; i < 1+220/20;i++){
        	labelTable.put(new Integer(150+i*20), new JLabel(Double.toString((150+i*20)/100.0) + " mps"));
        }
        dogSpeedSlider.setLabelTable(labelTable);
        
        dogSpeedSlider.setValue(150);
        dogSpeedSlider.setMinorTickSpacing(10);
        dogSpeedSlider.setPaintLabels(true);
        dogSpeedSlider.setPaintTicks(true);
        dogSpeedSlider.setSnapToTicks(true);
        dogSpeedSlider.setFocusable(false); 
        dogSpeedSlider.setToolTipText("Rate of travel");
        dogSpeedSlider.setBounds(230,5,obstacleButtonsPanel.getWidth()-230-5,(2+noOfPossibleColors)*40-5);        
        dogSpeedSlider.addChangeListener(new ChangeListener() 
        {
            public void stateChanged(ChangeEvent e) 
            {
              Base.dogSpeed = dogSpeedSlider.getValue()/100.0;
              canvas.repaint();
            }
        });
        Base.checkBoxPanel.add(dogSpeedSlider);
    	 
        //initialising the application colours for paths
		possibleColors[0] = new Color(40,39,45); 
		possibleColors[1] = new Color(230,32,37); 
		possibleColors[2] = new Color(8,162,223); 
		possibleColors[3] = new Color(41,174,80);
		possibleColors[4] = new Color(248,237,24);
		
		//initialising the path tickboxes and the Add obstacles on clisk and Paint grid tickboxes
        for (int i = 0; i < noOfPossibleColors; i++){
        	checkBoxButtons[i] = new JCheckBox("",false);
        	checkBoxButtons[i].setForeground(new Color(255,255,255));
        	checkBoxButtons[i].setBackground(possibleColors[i]);
        	checkBoxButtons[i].setBounds(5,i*40+5,220,35);
        	checkBoxButtons[i].setVisible(true);
        	checkBoxPanel.add(checkBoxButtons[i]);
        	checkBoxButtons[i].addActionListener(this);
        	drawPath[i] = false;
        	// 45
        }
        checkBoxButtons[noOfPossibleColors] = new JCheckBox("Add obstacles on click?", true);
        checkBoxButtons[noOfPossibleColors].setBackground(new Color(210,210,210));
        checkBoxButtons[noOfPossibleColors].setBounds(5,noOfPossibleColors*40+5,220,35);
        checkBoxButtons[noOfPossibleColors].setVisible(true);
        checkBoxPanel.add(checkBoxButtons[noOfPossibleColors]);
        checkBoxButtons[noOfPossibleColors].addActionListener(this);

        checkBoxButtons[noOfPossibleColors+1] = new JCheckBox("Display grid?", true);
        checkBoxButtons[noOfPossibleColors+1].setBackground(new Color(210,210,210));
        checkBoxButtons[noOfPossibleColors+1].setBounds(5,(noOfPossibleColors+1)*40+5,220,35);
        checkBoxButtons[noOfPossibleColors+1].setVisible(true);
        checkBoxPanel.add(checkBoxButtons[noOfPossibleColors+1]);
        checkBoxButtons[noOfPossibleColors+1].addActionListener(this);
        
        checkBoxPanel.setSize(obstacleButtonsPanel.getWidth(),(2+noOfPossibleColors)*40+5);
        checkBoxPanel.setVisible(true);
        checkBoxPanel.setLayout(null);
        checkBoxPanel.setBackground(Color.white);	
  
        //adding listener to the zoom slider
        //http://www.java2s.com/Code/JavaAPI/javax.swing/JSlideraddChangeListenerChangeListenerl.htm
        unitSlider.addChangeListener(new ChangeListener() 
        {
            public void stateChanged(ChangeEvent e) 
            {
              Base.unit = unitSlider.getValue();
              
              Base.canvas.repaint();
            }
        });
        unitSlider.setVisible(true);
    }  
    
    //handling the action events fired by the tickboxes (checkboxes)
    public void actionPerformed(ActionEvent e){
    	JCheckBox checkBox = (JCheckBox)e.getSource();
    	for (int i = 0; i < noOfPossibleColors; i++){
    		if (checkBox == checkBoxButtons[i] && checkBox.isSelected()){
        		drawPath[i] = true;
        		Base.canvas.repaint();
        	}
    		if (checkBox == checkBoxButtons[i] && !checkBox.isSelected()){
        		drawPath[i] = false;
        		Base.canvas.repaint();
        	}
    	} 
    	if (checkBox == checkBoxButtons[noOfPossibleColors]){
    		if (checkBox.isSelected()){
    			addObstaclesEnabled = true;
    		}else{
    			addObstaclesEnabled = false;
    		}
    	}
    	if (checkBox == checkBoxButtons[noOfPossibleColors+1]){
    		if (checkBox.isSelected()){
    			paintGrid = true;
    			Base.canvas.repaint();
    		}else{
    			paintGrid = false;
    			Base.canvas.repaint();
    		}
    	}    	
    }       
    
    
    //the main method
    public static void main(String[] args)
    {    	
    	base = new Base();
    	
    	//adding window listener to the instance of Base, so as to handle closing window and related actions ("Save unsaved contents?")
    	//http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
    	base.addWindowListener(new java.awt.event.WindowAdapter() {
    	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    	        if (Base.saved){System.exit(0);} else{
	    	    	if (JOptionPane.showConfirmDialog(base, "Close without saving?", "Unsaved content", 
	    	    			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
	    	    	   ){
	    	            System.exit(0);
	    	        }else{
	    	        	Base.saveToFile();
	    	        	System.exit(0);
	    	        }
    	        }
    	    }
    	});
    	
    	checkBoxPanel.setLocation(canvas_width+canvas_x + 20,(int)Math.max(base.getHeight()*0.4,base.getHeight()*0.1+3*65+5)+90);
        base.getContentPane().add(checkBoxPanel);
        
        // ADDING THE PANEL WITH THE OBSTACLE BUTTONS, READING THE LIST OF OBSTACLES AND LOADING THE ICONS
        String absName = "obstacles.txt";
        Class c=null;
        try {
          c = Class.forName("Base");
        } catch (Exception ex) {
        }
        InputStream s = c.getResourceAsStream(absName);
       
        try{
        	BufferedReader br = new BufferedReader(new InputStreamReader(s));
      		for (int i = 0; i < noOfObstacles; i++)
            {
                String name = "" + br.readLine();
                obstacleButtons[i] = new ObstacleButton(name,i);
                obstacleButtons[i].setVisible(true);
                obstacleButtons[i].setBounds(5+(i%4)*65,5+65*(i/4),60,60);
                obstacleButtonsPanel.add(obstacleButtons[i]);  
                obstacleButtons[i].setToolTipText(name);
            }

            (obstacleButtons[ObstacleButton.buttonPressed]).setBorder(BorderFactory.createLineBorder(Color.green,2));
            br.close();
        }catch(Exception e){
        }
    	
        obstacleButtonsPanel.setVisible(true);
        obstacleButtonsPanel.setLayout(null);
        obstacleButtonsPanel.setLocation(canvas_width+canvas_y+20,(int)(base.getHeight()*0.1));
        obstacleButtonsPanel.add(Base.unitSlider);
        obstacleButtonsPanel.add(menuButtonsPanel);
        obstacleButtonsPanel.setBackground(new Color(240,253,255));
        base.getContentPane().add(obstacleButtonsPanel);
        
        //ADD THE PANEL WITH THE MENU BUTTONS, READING LIST, LOADING ICONS
        absName = "menu.txt";
        c=null;
        try {
            c = Class.forName("Base");
        } catch (Exception ex) {}
        s = c.getResourceAsStream(absName);
         
        try{
        	BufferedReader br = new BufferedReader(new InputStreamReader(s));
            for (int i = 0; i < 5; i++)
            {            	
                String name = "" + br.readLine();
                menuButtons[i] = new MenuButton(name,i);
                menuButtons[i].setVisible(true);
                menuButtons[i].setBounds(5+65*i,5,60,60);
                menuButtonsPanel.add(menuButtons[i]);  
                menuButtons[i].setToolTipText(name);
            }
            br.close();
        }catch(Exception e){
        }
        menuButtonsPanel.setVisible(true);
        menuButtonsPanel.setLayout(null);
        menuButtonsPanel.setBounds(5,3*65+15,5*65+5,70);
        menuButtonsPanel.setBackground(new Color(240,253,255));
                
        //ADDING THE CANVAS INSTANCE
        canvas = new Canvas();
        base.add(canvas);
        canvas.setDoubleBuffered(true);
        
        base.pack();
    }  

    //OPENING DACM FILES
    public static void openFile()
    {
    	if (isFileDialogOpened) {return;}
    	if (!Base.saved) Base.reset();
    	isFileDialogOpened = true;
    	JFileChooser fileOpenChooser = new JFileChooser();
    	FileNameExtensionFilter filter = new FileNameExtensionFilter("DACM files (*.dacm)", "dacm");
    	fileOpenChooser.setFileFilter(filter);
    	int returnVal = fileOpenChooser.showOpenDialog(fileOpenChooser);
    	if (returnVal == JFileChooser.APPROVE_OPTION) {
    		File file = fileOpenChooser.getSelectedFile();
    		try {
    			FileReader fr = new FileReader(file.getAbsoluteFile());
    			BufferedReader br = new BufferedReader(fr);
    			Canvas.read(br);
    			//READING OBSTACLE PARAMETERS
    			for (int i = 0; i < Base.noOfObstacles; i++)
    			{
    				String str = br.readLine();
    				Canvas.counter[i] = Integer.parseInt(str);
    				for (int j = 0; j < Integer.parseInt(str); j++)
    				{
    					switch(i)
    					{
	    					case 0: Aframe.read(br);break;
	    					case 1: CollapsibleTunnel.read(br);break;
	    					case 2: Dogwalk.read(br);break;
	    					case 3: Jump.read(br);break;
	    					case 4: LongJump.read(br);break;
	    					case 5: PipeTunnel.read(br);break;
	    					case 6: Seesaw.read(br);break;
	    					case 7: SpreadJump.read(br);break;
	    					case 8: Table.read(br);break;
	    					case 9: TyreJump.read(br);break;
	    					case 10: WallJump.read(br);break;
	    					case 11: WeavePoles.read(br);break;
    					}
    				}
    			}
    			Base.saved = true;
    			br.close();
     
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	Base.canvas.repaint();
    	isFileDialogOpened = false;
    }
    
    //SAVING TO DACM FILES
    public static void saveToFile()
    {
    	if (isFileDialogOpened) {return;}
    	isFileDialogOpened = true;
    	JFileChooser fileSaveChooser = new JFileChooser();
    	if (!(directory == null)){fileSaveChooser.setCurrentDirectory(directory); directory = null;}
    	FileNameExtensionFilter filter = new FileNameExtensionFilter("DACM source files (*.dacm)", "dacm");
    	fileSaveChooser.setFileFilter(filter);
    	int returnVal = fileSaveChooser.showSaveDialog(fileSaveChooser);
    	if (returnVal == JFileChooser.APPROVE_OPTION) {
    		File file = new File("");
    		File file1 = fileSaveChooser.getSelectedFile();
        	String extension = "";
        	//CHECKING IF THE EXTENSION HAS BEEN APPENDED BY USER
    		if (file1.getName().length() > 4){
    			extension = file1.getName().substring(file1.getName().length()-5,file1.getName().length());
    		}
    		if (!extension.equals(".dacm")){
    			extension = ".dacm";
    			String path = new String(file1.getAbsoluteFile() + extension);
    			file = new File(path);
    		}else{
    			file = new File(file1.getAbsolutePath());
    		}
        	//http://www.mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-example/
        	try {
        		//IF THE FILE ALREADY EXISTS
        		if (file.exists()) {
    				int result = JOptionPane.showConfirmDialog((Component) null, "The file already exists. Do you want to overwrite it?","Warning", JOptionPane.NO_OPTION);
    				if (result == 0){
    	    			FileWriter fw = new FileWriter(file.getAbsoluteFile());
    	    			BufferedWriter bw = new BufferedWriter(fw);
    	    			Canvas.print(bw);
    	    			//WRITING OBSTACLE PARAMETERS TO FILE
    	    			for (int i = 0; i < Base.noOfObstacles; i++)
    	    			{
    	    				bw.write(Integer.toString(Canvas.counter[i]));
    	    				bw.newLine();
    	    				for (int j = 0; j < Canvas.counter[i]; j++)
    	    				{
    	    					switch(i)
    	    					{
    		    					case 0: Canvas.aframeObstacles[j].print(bw);break;
    		    					case 1: Canvas.collapsibleTunnelObstacles[j].print(bw);break;
    		    					case 2: Canvas.dogwalkObstacles[j].print(bw);break;
    		    					case 3: Canvas.jumpObstacles[j].print(bw);break;
    		    					case 4: Canvas.longJumpObstacles[j].print(bw);break;
    		    					case 5: Canvas.pipeTunnelObstacles[j].print(bw);break;
    		    					case 6: Canvas.seesawObstacles[j].print(bw);break;
    		    					case 7: Canvas.spreadJumpObstacles[j].print(bw);break;
    		    					case 8: Canvas.tableObstacles[j].print(bw);break;
    		    					case 9: Canvas.tyreJumpObstacles[j].print(bw);break;
    		    					case 10: Canvas.wallJumpObstacles[j].print(bw);break;
    		    					case 11: Canvas.weavePolesObstacles[j].print(bw);break;
    	    					}
    	    					bw.newLine();
    	    				}
    	    			}
    	    			Base.saved = true;
    	    			bw.close();
    				}
    				if (result == 1)
    				{
    					directory = file;
    					isFileDialogOpened = false;
    					Base.saveToFile();
    				}
    				if (result == 2)
    				{
    					isFileDialogOpened = false;
    				}
    			}else{
    				//IF THE FILE DOES NOT EXIST YET
    				file.createNewFile();
        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			BufferedWriter bw = new BufferedWriter(fw);
        			Canvas.print(bw);
        			for (int i = 0; i < Base.noOfObstacles; i++)
        			{
        				bw.write(Integer.toString(Canvas.counter[i]));
        				bw.newLine();
        				for (int j = 0; j < Canvas.counter[i]; j++)
        				{
        					switch(i)
        					{
    	    					case 0: Canvas.aframeObstacles[j].print(bw);break;
    	    					case 1: Canvas.collapsibleTunnelObstacles[j].print(bw);break;
    	    					case 2: Canvas.dogwalkObstacles[j].print(bw);break;
    	    					case 3: Canvas.jumpObstacles[j].print(bw);break;
    	    					case 4: Canvas.longJumpObstacles[j].print(bw);break;
    	    					case 5: Canvas.pipeTunnelObstacles[j].print(bw);break;
    	    					case 6: Canvas.seesawObstacles[j].print(bw);break;
    	    					case 7: Canvas.spreadJumpObstacles[j].print(bw);break;
    	    					case 8: Canvas.tableObstacles[j].print(bw);break;
    	    					case 9: Canvas.tyreJumpObstacles[j].print(bw);break;
    	    					case 10: Canvas.wallJumpObstacles[j].print(bw);break;
    	    					case 11: Canvas.weavePolesObstacles[j].print(bw);break;
        					}
        					bw.newLine();
        				}
        			}
        			Base.saved = true;
        			bw.close();
    			}     
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	} 	
       	isFileDialogOpened = false;
    }
    
    //EXPORTING TO PNG, GIF, PDF
    public static void export()
    {
    	if (isFileDialogOpened) {return;}
    	isFileDialogOpened = true;
    	JFileChooser fileSaveChooser = new JFileChooser();
    	
    	//CHOOSING THE FORMAT, SETTING THE EXTENSION
    	if (!(directory == null)){fileSaveChooser.setCurrentDirectory(directory); directory = null;}
    	fileSaveChooser.setDialogTitle("Export DACM");
    	FileNameExtensionFilter filterPDF = new FileNameExtensionFilter("PDF files (*.pdf)", "pdf", "PDF");
    	fileSaveChooser.setFileFilter(filterPDF);
    	FileNameExtensionFilter filterPNG = new FileNameExtensionFilter("PNG files (*.png)", "png", "PNG");
    	fileSaveChooser.addChoosableFileFilter(filterPNG);
    	FileNameExtensionFilter filterGIF = new FileNameExtensionFilter("GIF files (*.gif)", "gif", "GIF");
    	fileSaveChooser.addChoosableFileFilter(filterGIF);    	   	
    	
    	int returnVal = fileSaveChooser.showSaveDialog(fileSaveChooser);
    	if (returnVal == JFileChooser.APPROVE_OPTION) {
    		File file = new File("");
    		File file1 = fileSaveChooser.getSelectedFile();
    		
        	String extension = "";
    		if (file1.getName().length() > 3){
    			extension = file1.getName().substring(file1.getName().length()-4,file1.getName().length());
    		}
    		if (!extension.equals(".pdf") && !extension.equals(".gif") && !extension.equals(".png")){
    	    	if (fileSaveChooser.getFileFilter().toString().equals(filterPNG.toString())) {extension = ".png";}
    	    	if (fileSaveChooser.getFileFilter().toString().equals(filterGIF.toString())) {extension = ".gif";}
    	    	if (fileSaveChooser.getFileFilter().toString().equals(filterPDF.toString())) {extension = ".pdf";}
    			String path = new String(file1.getAbsoluteFile() + extension);
    			file = new File(path);
    		}else{
    			file = new File(file1.getAbsolutePath());
    		}
    		
	    	extension = "pdf";
	    	if (fileSaveChooser.getFileFilter().toString().equals(filterPNG.toString())) {extension = "png";}
	    	if (fileSaveChooser.getFileFilter().toString().equals(filterGIF.toString())) {extension = "gif";}
	    	if (fileSaveChooser.getFileFilter().toString().equals(filterPDF.toString())) {extension = "pdf";}

	    	try {
	    		//IF FILE EXISTS
    			if (file.exists()) {
    				int result = JOptionPane.showConfirmDialog((Component) null, "The file already exists. Do you want to overwrite it?","Warning", JOptionPane.NO_OPTION);
    				if (result == 0){
    					if (extension == "pdf")
    					{
    						exportPDF(file);
     					}else{
	    					BufferedImage bi = new BufferedImage(Base.canvas.getSize().width, Base.canvas.getSize().height, BufferedImage.TYPE_INT_ARGB); 
	    	    	    	Graphics g = bi.createGraphics();
	    	    	    	Base.canvas.paint(g);
	    	    	    	g.dispose();
	    	    	    	try{
	    	    	    		ImageIO.write(bi,extension,file);
	    	    	    	}catch (Exception e) {}
	    				}
    				}
    				if (result == 1)
    				{
    					directory = file;
    					isFileDialogOpened = false;
    					Base.export();
    				}
    				if (result == 2)
    				{
    					isFileDialogOpened = false;
    				}
    			}else{
    				//IF FILE DOES NOT EXIST YET
    				if (extension == "pdf")
    				{
    					exportPDF(file);
    				}else{
    				file.createNewFile();
    				BufferedImage bi = new BufferedImage(Base.canvas.getSize().width, Base.canvas.getSize().height, BufferedImage.TYPE_INT_ARGB); 
        	    	Graphics g = bi.createGraphics();
        	    	Base.canvas.paint(g);
        	    	g.dispose();
        	    	try{
        	    		ImageIO.write(bi,extension,file);
        	    	}catch (Exception e) {}
    				}
    			}        	
        	} catch (IOException e) {
    			e.printStackTrace();
    		}
    	} 	
       	isFileDialogOpened = false;    
    }
    public static void exportPDF(File file){
    	//http://stackoverflow.com/questions/4517907/how2-add-a-jpanel-to-a-document-then-export-to-pdf
		Document document = new Document(PageSize.A4.rotate());
		try {
			//SET UP MARGIN WIDTH MESSAGE BOW
			JOptionPane optionPane = new JOptionPane();
	        final JSlider marginSlider = new JSlider(0,100,90);
	        marginSlider.setMajorTickSpacing(20);
	        marginSlider.setPaintTicks(true);
	        marginSlider.setPaintLabels(true);
	        marginSlider.setMinorTickSpacing(10);
	        marginSlider.setSnapToTicks(true);
	        ChangeListener changeListener = new ChangeListener() {
	          public void stateChanged(ChangeEvent changeEvent) {
	        	  Base.marginPercent = (double)((marginSlider.getValue()/2+50)/100.0);
	          }
	        };
	        
	        marginSlider.addChangeListener(changeListener);   					        
	        optionPane.setMessage(new Object[] { "Select the width of the course map (% of the full page).", marginSlider });
	        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	        JDialog dialog = optionPane.createDialog(Base.base, "");
	        dialog.setVisible(true);
	       
	        //CREATING TEMPLATE, SCALING, TRANSLATING THE CANVAS
	        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
		    document.setMargins((float)(document.getPageSize().getWidth()*(1-Base.marginPercent)),
		    		(float)(document.getPageSize().getWidth()*(1-Base.marginPercent)),
		    		(float)(document.getPageSize().getHeight()*(1-Base.marginPercent)),
		    		(float)(document.getPageSize().getHeight()*(1-Base.marginPercent))	);			    
		    document.open();
		    double ratio = (document.getPageSize().getHeight()-document.topMargin()-document.bottomMargin())/(1.0*Base.canvas_height);
		    PdfContentByte contentByte = writer.getDirectContent();
		    PdfTemplate template = contentByte.createTemplate(document.getPageSize().getWidth()-document.leftMargin()-document.rightMargin(),
		    		document.getPageSize().getHeight()-document.topMargin()-document.bottomMargin());
		    Graphics2D g2 = template.createGraphics((int)(ratio*Base.canvas_width), (int)(ratio*Base.canvas_height));
		    g2.translate(0,0);
		    g2.scale(ratio,ratio);
		    g2.fillRect(0,0,1366,768);
		    Base.canvas.printAll(g2);
		    g2.dispose();
		    contentByte.addTemplate(template,document.leftMargin(),document.bottomMargin());
		    
		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
		finally{
		    if(document.isOpen()){
		        document.close();
		    }
		}
    }
    public static void print(){
    	//http://stackoverflow.com/questions/750310/how-can-i-print-a-single-jpanels-contents
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setJobName("Print DACM");
		PageFormat pageFormat = printerJob.defaultPage();
		
		//DEFAULT PARAMETERS
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		Paper paper = new Paper();
		paper.setSize(72*21/2.54, 72*29.7/2.54);
		paper.setImageableArea(0.5*72, 0.5*72, 72*(-1+21/2.54), 72*(-1+29.7/2.54));
		pageFormat.setPaper(paper);
		printerJob.defaultPage(pageFormat);
		
		PageFormat pf = printerJob.pageDialog(pageFormat);
        printerJob.setPrintable(new PrintDialogExample(), pf);
        if (printerJob.printDialog()) {
            try {
                 printerJob.print();
            } catch (PrinterException ex) {
         
            }
        }
   	}
    //CLEARS THE CANVAS, RESETS THE APPLICATION - NEW
    public static void reset(){
    	int result = 2;
    	if (!Base.saved){ 
    		result = sureAboutNotSaving();
    	} else {result = 1;}
    	if (result == 0){saveToFile();}
    	if (result == 1){
    		Base.canvas.removeAll();
    		for (int i = 0; i < Base.noOfObstacles; i++)
    		{
    			Canvas.counter[i] = 0;
    		}
    		for (int i = 0; i < Base.noOfPossibleColors; i++){
    			for (int j = 0; j < Base.maxNoOfObstacles; j++){
    				Canvas.pathPoints[i][j].exists = false;
    			}
    		}
    		
    		Base.canvas.repaint();
    	}
    }
    //WARNS THE USER THAT THERE IS SOME UNSAVED CONTENT
    public static int sureAboutNotSaving(){
    	return JOptionPane.showConfirmDialog((Component) null, "Do you want to save the unsaved DACM?","You may loose some unsaved data", JOptionPane.YES_OPTION);
    }
}
class PrintDialogExample implements Printable {	 
	//SCALE AND FIT THE CANVAS TO THE GRAPHICS ABOUT TO BE PRINTED 
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        double ratio = pf.getImageableHeight()/Base.canvas_height;
        g2d.scale(ratio,ratio);
        Base.canvas.print(g2d);
        return PAGE_EXISTS;
    }
}

// ADD THE CANVAS PANEL FOR PLACING THE OBSTACLES
// http://docs.oracle.com/javase/tutorial/uiswing/painting/step2.html
class Canvas extends JPanel implements MouseListener
{
    public static int x,y,width,height;
    public static Grid grid = new Grid();
    public static Integer[] counter = new Integer[Base.noOfObstacles];
    public static Aframe[] aframeObstacles = new Aframe[Base.maxNoOfObstacles];
    public static CollapsibleTunnel[] collapsibleTunnelObstacles = new CollapsibleTunnel[Base.maxNoOfObstacles];
    public static Dogwalk[] dogwalkObstacles = new Dogwalk[Base.maxNoOfObstacles];
    public static Jump[] jumpObstacles = new Jump[Base.maxNoOfObstacles];
    public static LongJump[] longJumpObstacles = new LongJump[Base.maxNoOfObstacles];
    public static PipeTunnel[] pipeTunnelObstacles = new PipeTunnel[Base.maxNoOfObstacles];
    public static Seesaw[] seesawObstacles = new Seesaw[Base.maxNoOfObstacles];
    public static SpreadJump[] spreadJumpObstacles = new SpreadJump[Base.maxNoOfObstacles];
    public static Table[] tableObstacles = new Table[Base.maxNoOfObstacles];
    public static TyreJump[] tyreJumpObstacles = new TyreJump[Base.maxNoOfObstacles];
    public static WallJump[] wallJumpObstacles = new WallJump[Base.maxNoOfObstacles];
    public static WeavePoles[] weavePolesObstacles = new WeavePoles[Base.maxNoOfObstacles];
    public static PathPoint[][] pathPoints = new PathPoint[Base.noOfPossibleColors][Base.maxNoOfObstacles];
    
    public Canvas() 
    {
    	//SETTING UP DIMENSIONS, LOCATION, COLOUR, ETC.
        x = Base.canvas_x;
        y = Base.canvas_y;
        width = Base.canvas_width;
        height = Base.canvas_height;
        this.setBounds(x,y,width,height);
        this.setVisible(true);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        setBackground(new Color(155,250,155));
        
        for (int i = 0; i < Base.noOfPossibleColors; i++)
        {
        	for (int j = 0; j < Base.maxNoOfObstacles; j++) {
        		pathPoints[i][j] = new PathPoint(false);
        		pathPoints[i][j].begin = new Point();
        		pathPoints[i][j].end = new Point();
        		}
        }
        
        for (int i = 0; i < Base.noOfObstacles; i++){counter[i] = 0;}
        this.addMouseListener(this);         
    }
    //PAINTING OF ALL COMPONENTS ON CANVAS - OBSTACLES, LABELS, ...
    public void paintComponent(Graphics g) 
    {   
    	/* THE OBSTACLES SORTED IN DESCENDING ORDER (BY SIZE)
    	 dogwalk
    	 seesaw
    	 aframe
    	 weavepoles
    	 pipetunnel
    	 collapsible
    	 longjump
    	 spreadjump
    	 table
    	 jump
    	 tyrejump
    	 wall
    	 */

    	super.paintComponent(g);
        grid.paintGrid(g,x,y,width,height); 
        
        //ADDING THE OBSTACLES TO THE CANVAS FROM SMALL TO LARGE SIZE, SO AS TO PUT THE SMALL ONES IN THE FOREGROUND
        for (int i = 0; i < Canvas.counter[10]; i++)
        {
                Base.canvas.add(Canvas.wallJumpObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[9]; i++)
        {
                Base.canvas.add(Canvas.tyreJumpObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[3]; i++)
        {
                Base.canvas.add(Canvas.jumpObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[8]; i++)
        {
                Base.canvas.add(Canvas.tableObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[7]; i++)
        {
                Base.canvas.add(Canvas.spreadJumpObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[4]; i++)
        {
                Base.canvas.add(Canvas.longJumpObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[1]; i++)
        {
                Base.canvas.add(Canvas.collapsibleTunnelObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[5]; i++)
        {
                Base.canvas.add(Canvas.pipeTunnelObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[11]; i++)
        {
                Base.canvas.add(Canvas.weavePolesObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[0]; i++)
        {
                Base.canvas.add(Canvas.aframeObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[6]; i++)
        {
                Base.canvas.add(Canvas.seesawObstacles[i]);
        }
        for (int i = 0; i < Canvas.counter[2]; i++)
        {
                Base.canvas.add(Canvas.dogwalkObstacles[i]);
        }
        
        //PAINTING THE OBSTACLES, THE LARGEST FIRST, SO AS TO PAINT THE SMALLEST IN THE FOREGROUND AND THE LARGEST BEHIND THEM
        for (int i = 0; i < Canvas.counter[2]; i++)
        {
        	Canvas.dogwalkObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[6]; i++)
        {
        	Canvas.seesawObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[0]; i++)
        {
        	Canvas.aframeObstacles[i].repaint();	
        }
        for (int i = 0; i < Canvas.counter[11]; i++)
        {
        	Canvas.weavePolesObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[5]; i++)
        {
        	Canvas.pipeTunnelObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[1]; i++)
        {
        	Canvas.collapsibleTunnelObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[4]; i++)
        {
        	Canvas.longJumpObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[7]; i++)
        {
        	Canvas.spreadJumpObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[8]; i++)
        {
        	Canvas.tableObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[3]; i++)
        {
        	Canvas.jumpObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[9]; i++)
        {
        	Canvas.tyreJumpObstacles[i].repaint();
        }
        for (int i = 0; i < Canvas.counter[10]; i++)
        {
        	Canvas.wallJumpObstacles[i].repaint();
        }
                      
        Graphics2D g2 = (Graphics2D) g;
        
        //PAINTING THE PATHS, CALCULATING THEIR LENGTHS

        for (int i = 0; i < Base.noOfPossibleColors; i++){Base.noOfTables[i] = 0;}
        for (int i = 0; i < Canvas.counter[8]; i++)
        {
        	for (int j = 0; j < Table.noOfDirections; j++)
        	{
        		for (int k = 0; k < 4; k++){
        			if (Canvas.tableObstacles[i].oneDirection[j].labels[k].visible){
        				Base.noOfTables[Canvas.tableObstacles[i].oneDirection[j].labels[k].currentColor]++;
        			}
        		}
        	}
        }        
        for (int i = 0; i < Base.noOfPossibleColors; i++)
        {
        	Base.pathLength[i] = 0;
        	if (!Base.drawPath[i]) {
        		Base.checkBoxButtons[i].setText("");
        		continue;
        	}
        	g2.setColor(Base.possibleColors[i]);
        	g2.setStroke(new BasicStroke(2));
        	for (int j = 0; j < Base.maxNoOfObstacles-1; j++)
        	{
        		if (Canvas.pathPoints[i][j].exists && Canvas.pathPoints[i][j+1].exists)
        		{
        			Base.pathLength[i] += Canvas.pathPoints[i][j].obstacleIncrement;
        			Base.pathLength[i] += Math.sqrt((Canvas.pathPoints[i][j].end.x-Canvas.pathPoints[i][j+1].begin.x)*(Canvas.pathPoints[i][j].end.x-Canvas.pathPoints[i][j+1].begin.x)+(Canvas.pathPoints[i][j].end.y-Canvas.pathPoints[i][j+1].begin.y)*(Canvas.pathPoints[i][j].end.y-Canvas.pathPoints[i][j+1].begin.y))/(1.0*Base.unit);
        			if (j == Base.maxNoOfObstacles-2) 
        				{
        					Base.pathLength[i] += Canvas.pathPoints[i][j+1].obstacleIncrement;
        				}else{
        					if (!Canvas.pathPoints[i][j+2].exists){
        						Base.pathLength[i] += Canvas.pathPoints[i][j+1].obstacleIncrement;
        					}
        				}
        			g2.drawLine(Canvas.pathPoints[i][j].end.x,Canvas.pathPoints[i][j].end.y,Canvas.pathPoints[i][j+1].begin.x,Canvas.pathPoints[i][j+1].begin.y);
        		}
        	}
        	Base.sct = (int)(Math.round(Base.pathLength[i])/Base.dogSpeed)+3*Base.noOfTables[i];
        	Base.checkBoxButtons[i].setText("Length: " + Math.round(Base.pathLength[i]) + " m | SCT: " + Base.sct + " s");
        }
    }  
    //REMOVING OBSTACLE FOREVER
    public static void removeObstacle(int noOfType, int index){
    	Base.saved = false;
    	switch (noOfType)
    	{
			case 0:{
				Canvas.aframeObstacles[index] = Canvas.aframeObstacles[Canvas.counter[noOfType]-1];
				Canvas.aframeObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 1:{
				Canvas.collapsibleTunnelObstacles[index] = Canvas.collapsibleTunnelObstacles[Canvas.counter[noOfType]-1];
				Canvas.collapsibleTunnelObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 2:{
				Canvas.dogwalkObstacles[index] = Canvas.dogwalkObstacles[Canvas.counter[noOfType]-1];
				Canvas.dogwalkObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 3:{
				Canvas.jumpObstacles[index] = Canvas.jumpObstacles[Canvas.counter[noOfType]-1];
				Canvas.jumpObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 4:{
				Canvas.longJumpObstacles[index] = Canvas.longJumpObstacles[Canvas.counter[noOfType]-1];
				Canvas.longJumpObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 5:{
				Canvas.pipeTunnelObstacles[index] = Canvas.pipeTunnelObstacles[Canvas.counter[noOfType]-1];
				Canvas.pipeTunnelObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 6:{
				Canvas.seesawObstacles[index] = Canvas.seesawObstacles[Canvas.counter[noOfType]-1];
				Canvas.seesawObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 7:{
				Canvas.spreadJumpObstacles[index] = Canvas.spreadJumpObstacles[Canvas.counter[noOfType]-1];
				Canvas.spreadJumpObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 8:{
				Canvas.tableObstacles[index] = Canvas.tableObstacles[Canvas.counter[noOfType]-1];
				Canvas.tableObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 9:{
				Canvas.tyreJumpObstacles[index] = Canvas.tyreJumpObstacles[Canvas.counter[noOfType]-1];
				Canvas.tyreJumpObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 10:{
				Canvas.wallJumpObstacles[index] = Canvas.wallJumpObstacles[Canvas.counter[noOfType]-1];
				Canvas.wallJumpObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
			case 11:{
				Canvas.weavePolesObstacles[index] = Canvas.weavePolesObstacles[Canvas.counter[noOfType]-1];
				Canvas.weavePolesObstacles[index].index = index;
				Canvas.counter[noOfType]--;
				break;
			}
		}
    	
    }   
    //WRITING CANVAS PARAMETERS TO TEXT FILE
    public static void print(BufferedWriter bw) throws IOException{
    	String str = Integer.toString(Base.unit) + ";";
    	for (int i = 0; i < Base.noOfPossibleColors+2; i++){str = str + Boolean.toString(Base.checkBoxButtons[i].isSelected()) + ";";}
    	str = str + Double.toString(Base.dogSpeed) + ";";
    	bw.write(str);
    	bw.newLine();
    }
    //READ PARAMETERS FROM TEXT FILE
    public static void read(BufferedReader br) throws IOException{
    	String line = br.readLine();
    	String[] parsed = line.split(";");
    	Base.unit = Integer.parseInt(parsed[0]);
    	Base.unitSlider.setValue(Base.unit);
    	int i = 0;
    	for (i = 0; i < Base.noOfPossibleColors; i++){
    		Base.checkBoxButtons[i].setSelected(Boolean.parseBoolean(parsed[i+1])); 
    		Base.drawPath[i] = Boolean.parseBoolean(parsed[i+1]);
    	}
    	Base.checkBoxButtons[Base.noOfPossibleColors].setSelected(Boolean.parseBoolean(parsed[Base.noOfPossibleColors+1])); 
    	Base.addObstaclesEnabled = Boolean.parseBoolean(parsed[Base.noOfPossibleColors+1]);
    	Base.checkBoxButtons[Base.noOfPossibleColors+1].setSelected(Boolean.parseBoolean(parsed[Base.noOfPossibleColors+2]));
    	Base.paintGrid = Boolean.parseBoolean(parsed[Base.noOfPossibleColors+2]);
    	
    	Base.dogSpeed = Double.parseDouble(parsed[Base.noOfPossibleColors+3]);
    	Base.dogSpeedSlider.setValue((int)(100*Base.dogSpeed));
    }
    
    public void mouseClicked(MouseEvent e)
	{
    	//ADDING OBSTACLES ON CLICK
    	if (Label.isAnythingActivated){return;}
		if (Base.addObstaclesEnabled){
			switch (ObstacleButton.buttonPressed)
			{
				case 0: {Canvas.aframeObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new Aframe(
							e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;}
				case 1: Canvas.collapsibleTunnelObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new CollapsibleTunnel(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 2: Canvas.dogwalkObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new Dogwalk(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 3: Canvas.jumpObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new Jump(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 4: Canvas.longJumpObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new LongJump(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 5: Canvas.pipeTunnelObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new PipeTunnel(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 6: Canvas.seesawObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new Seesaw(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 7: Canvas.spreadJumpObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new SpreadJump(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 8: Canvas.tableObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new Table(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 9: Canvas.tyreJumpObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new TyreJump(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 10: Canvas.wallJumpObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new WallJump(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
				case 11: Canvas.weavePolesObstacles[Canvas.counter[ObstacleButton.buttonPressed]] = new WeavePoles(
						e.getX(),e.getY(),Canvas.counter[ObstacleButton.buttonPressed]); break;
			}
			Canvas.counter[ObstacleButton.buttonPressed]++;    			
		}
	}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
}

// THE GRID
class Grid
{
    public void paintGrid(Graphics g, int x, int y, int width, int height)
    {
    	int density = 2;
        if (Base.unit >= 34){density = 1;}
        
    	int numx = Base.canvas_width/(density*Base.unit);
        int numy = Base.canvas_height/(density*Base.unit);
        int textheight = 3;
        
        //VERTICAL GRID
        if (!Base.paintGrid){return;}
        for (int i = 1; i <= numx+1; i++){
            g.setColor(Color.gray.brighter());
            g.drawLine((int)(density*(i+0.5)*Base.unit),0,(int)(density*(i+0.5)*Base.unit),Base.canvas_height);
        	g.setColor(Color.black);
            g.drawLine(density*i*Base.unit,0,density*i*Base.unit,Base.canvas_height);
            Integer coordinate_x = (i-1)*density;
            g.drawString(Integer.toString(coordinate_x) + " m",density*(i-1)*Base.unit+2,Base.canvas_height-textheight);
        }
        //HORIZONTAL GRID
        for (int i = 1; i <= numy+1; i++){
            g.setColor(Color.gray.brighter());
            g.drawLine(0,(int)(Base.canvas_height-density*(i+0.5)*Base.unit),Base.canvas_width,(int)(Base.canvas_height-density*(i+0.5)*Base.unit));
        	g.setColor(Color.black);
        	g.drawLine(0,Base.canvas_height-density*i*Base.unit,Base.canvas_width,Base.canvas_height-density*i*Base.unit);
            Integer coordinate_y = (i-1)*density;
            g.drawString(Integer.toString(coordinate_y) + " m",2,Base.canvas_height-density*(i-1)*Base.unit-textheight);
        }
    } 
}

// THE OBSTACLE BUTTONS
class ObstacleButton extends JButton
{
    final String address;
    public static int buttonPressed = 0;
    public int number;
    
    public ObstacleButton(String address,int number)
    {
        this.address = address;
        this.number = number;
        this.setSize(60,60);
        this.setLayout(null);
        
        //LOADING ICON
        ClassLoader cl = this.getClass().getClassLoader();
        String url = address.replace(" ", "") + "-ico.png";
        
        ImageIcon icon  = new ImageIcon(cl.getResource(url));
        //System.out.println(url);
        this.setIcon(icon);
        this.setBackground(Color.white);
        this.setVisible(true);
        this.setBorder(BorderFactory.createLineBorder(Color.white,0));
        //http://docs.oracle.com/javase/tutorial/uiswing/components/tooltip.html
        this.setToolTipText(address);
        //final String name = this.address;
        
        //HANDLING CLICKS
        //http://docs.oracle.com/javase/7/docs/api/java/awt/event/MouseAdapter.html
        this.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e){
        		//Base.saved = false;
        		for (int i = 0; i < Base.noOfObstacles; i++){
        			if (e.getSource() == Base.obstacleButtons[i])
        			{
        				Base.obstacleButtons[buttonPressed].setBorder(BorderFactory.createLineBorder(Color.white,0));
        				buttonPressed = i;
        				Base.obstacleButtons[buttonPressed].setBorder(BorderFactory.createLineBorder(Color.green,2));
        			}
        		}       
        	}
        });
     }   
}

// MENU BUTTONS
class MenuButton extends JButton
{
    final String address;
    public static int ButtonPressed = 0;
    public int number;
    public MenuButton(String address,int number)
    {
    	//SETTING DEFAULT PARAMETERS, LOADING ICONS
        this.address = address;
        this.number = number;
        this.setSize(60,60);
        this.setLayout(null);

        ClassLoader cl = this.getClass().getClassLoader();
        ImageIcon icon  = new ImageIcon(cl.getResource(address + ".png"));
        this.setIcon(icon);
        this.setBackground(Color.white);
        this.setVisible(true);
        this.setBorder(BorderFactory.createLineBorder(Color.white,0));
        //http://docs.oracle.com/javase/tutorial/uiswing/components/tooltip.html
        this.setToolTipText(address);
        final String name = this.address;
        
        //HANDLING CLICKS
        //http://docs.oracle.com/javase/7/docs/api/java/awt/event/MouseAdapter.html
        this.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e){
        		for (int i = 0; i < Base.noOfMenu; i++){
        			if (e.getSource() == Base.menuButtons[i])
        			{
        				ButtonPressed = i;
        				switch (i)
        				{
        				case 0: {
        					Base.openFile();
        					break;
        				}
        				case 1:
        				{
        					Base.reset();
        					break;
        				}
        				case 2:
        				{
        					
        					Base.saveToFile();
        					break;
        				}
        				case 3:
        				{
        					Base.export();
        					break;
        				}
        				case 4:
        				{
        					Base.print();
        					break;
        				}
        				}
        			}
        		}
        	}
        });
     }   
}

//PANEL WITH 4 LABELS, FOR A SPECIFIC DIRECTION OF PASSING AN OBSTACLE
class OneDirection extends Object
{
	public Label[] labels = new Label[4];
	public Point begin = new Point(), end = new Point(), paintAt = new Point();
	public JPanel panel = new JPanel();
	
	//INITIAL VALUES
	public OneDirection(double obstacleIncrement)
	{
		for (int i = 0; i < 4; i++){
			this.labels[i] = new Label(0,obstacleIncrement);
			this.labels[i].setParametersL();
			this.panel.add(this.labels[i]);
			this.labels[i].setBounds((1-(i+1)%2)*Label.size,((i/2)%2)*Label.size,Label.size,Label.size);
		}
		this.panel.setOpaque(false);
    	this.panel.setVisible(true);
    	this.panel.setLayout(null);
    	Base.canvas.add(this.panel);
    	
	}
	//CHANGES AS THE OBSTACLE ROTATES
	public void setParameters(double obstacleIncrement)
	{
		this.panel.setBounds(this.paintAt.x, this.paintAt.y, Label.size*2, Label.size*2);
		for (int i = 0; i < 4; i++){
			this.labels[i].obstacleIncrement = obstacleIncrement;
			this.labels[i].setParametersL();
			this.labels[i].setBounds((1-(i+1)%2)*Label.size,((i/2)%2)*Label.size,Label.size,Label.size);
			this.labels[i].begin = this.begin;
			this.labels[i].end = this.end;
		}
	}
}

//ONE 'PASS' THROUGH AN OBSTACLE - FROM BEGIN TO END, DISTANCE TRAVELLED = OBSTACLEINCREMENT
class PathPoint extends Point 
{
	public boolean exists = false;
	public double obstacleIncrement = 0;
	public Point begin, end;
	public PathPoint(boolean exists)
	{
		this.exists = exists;
	}
}

//SIMPLY OVERRIDEN METHODS OF NEW POPUPMENULISTENER
//http://www.java2s.com/Code/JavaAPI/javax.swing/JComponentsetComponentPopupMenuMyPopupMenupopup.htm
class MyPopupMenuListener implements PopupMenuListener {
	  public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {}
	  public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {}
	  public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {}
}

//LABELS
class Label extends JButton implements KeyListener,MouseListener
{
	public static int size = (int)(12+Base.unit*0.15);
	public static Font labelFont;
	public static int fontSizeToUse = (int)(7+Base.unit*0.1);
	public static boolean isAnythingActivated = false;
	public boolean changeNumberActivated = false, visible = false;
	public int number = 0, currentColor = 0;
	public Color color, defaultColor = new Color(0,0,0,8);
	public String str;
	public Point begin = new Point(), end = new Point();
	public double obstacleIncrement = 0;
	
	//SETTING UP, INITIALISING VALUES
	public Label(int number, double obstacleIncrement)
	{
		this.obstacleIncrement = obstacleIncrement;
		this.number = number;
		this.color = this.defaultColor;
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.white,0));
		this.setBorderPainted(false);
		this.setContentAreaFilled(false);
		//http://stackoverflow.com/questions/9361658/disable-jbutton-focus-border
		this.setFocusPainted(false);
		this.setFocusable(true);
		this.setForeground(Color.white);
		this.setVisible(true);
		if (this.visible){
			str = new String(Integer.toString(this.number+1));
		}else{str = "";}
		this.setText(str);
		labelFont = this.getFont();
        fontSizeToUse = (int)(7+Base.unit*0.1);
        this.setFont(new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse));
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
    	this.addMouseListener(this);   
    	this.addKeyListener(this);
	}
	
	//REFRESHING VALUES AFTER A CHANGE
	public void setParametersL()
	{		
		size = (int)(12+Base.unit*0.15);
		this.setBackground(new Color(0,0,0,0));
		this.setForeground(Color.white);
		//IF IS VISIBLE
		if (this.visible){
			Canvas.pathPoints[this.currentColor][this.number].exists = true;
			Canvas.pathPoints[this.currentColor][this.number].begin = this.begin;
			Canvas.pathPoints[this.currentColor][this.number].end = this.end;
			Canvas.pathPoints[this.currentColor][this.number].obstacleIncrement = this.obstacleIncrement;
			str = new String(Integer.toString(this.number+1));
			this.color = Base.possibleColors[this.currentColor];
		}else{
			//IF IS NOT VISIBLE
			str = "";
			this.color = this.defaultColor;
		}
		this.setText(str);
		fontSizeToUse = (int)(7+Base.unit*0.1);
		labelFont = new Font("Arial", Font.PLAIN, fontSizeToUse);
	}
	
	//CHANGE COLOUR (KEYS LEFT, RIGHT)
	public void changeColor(int increment)
	{
		Base.saved = false;
		Canvas.pathPoints[this.currentColor][this.number].exists = false;
		this.currentColor += increment;
		if (this.currentColor < 0) this.currentColor = Base.noOfPossibleColors-1;
		this.currentColor = this.currentColor%Base.noOfPossibleColors;
		this.color = Base.possibleColors[this.currentColor];
		this.setParametersL();
		this.repaint();
	}
	
	//CHANGE NUMBER (KEYS UP, DOWN)
	public void changeNumber(int increment)
	{
		Base.saved = false;
		Canvas.pathPoints[this.currentColor][this.number].exists = false;
		this.number += increment;
		if (this.number < 0) this.number = Base.maxNoOfObstacles-1;
		if (this.number > Base.maxNoOfObstacles-1) this.number = 0;
		this.setParametersL();
		this.repaint();
		this.setText(this.str);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		if (this.changeNumberActivated){
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.yellow);
			g2.drawOval(0, 0, getSize().width,getSize().height);
		}
		g2.setColor(this.color);
		g2.fillOval(0, 0, getSize().width,getSize().height);
		// THANKS TO THIS, THE NUMBER IS PAINTED ABOVE THE BACKGROUND CIRCLE
		super.paintComponent(g2);
	}	
	
	//HANDLING KEY ACTIONS - CHANGING NUMBER AND COLOUR, MAKING INVISIBLE, DEACTIVATING
	public void keyReleased(KeyEvent e){
		//http://stackoverflow.com/questions/10876491/how-to-use-keylistener
		int key = e.getKeyCode();

	    if (key == KeyEvent.VK_UP && this.changeNumberActivated == true) 
	    {
	        this.changeNumber(1);
	    }
	    if (key == KeyEvent.VK_DOWN && this.changeNumberActivated == true) 
	    {
	    	this.changeNumber(-1);
	    }
	    if (key == KeyEvent.VK_LEFT && this.changeNumberActivated == true) 
	    {
	        this.changeColor(-1);
	    }
	    if (key == KeyEvent.VK_RIGHT && this.changeNumberActivated == true) 
	    {
	    	this.changeColor(1);
	    }
	    if (key == KeyEvent.VK_ENTER && this.changeNumberActivated == true) 
	    {
	    	this.changeNumberActivated = false;
	    	isAnythingActivated = false;
	    	this.repaint();
	    }
	    if (key == KeyEvent.VK_DELETE && this.changeNumberActivated == true)
	    {
	    	this.visible = false;
	    	this.color = this.defaultColor;
	    	this.changeNumberActivated = false;
	    	isAnythingActivated = false;
	    	this.setParametersL();
	    }
	}
	public void keyPressed(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	//HANDLING MOUSE CLICKS (ACTIVATING, MAKING VISIBLE)
	public void mouseClicked(MouseEvent e)
    {
     	if (e.getButton() == MouseEvent.BUTTON1)
    	{
     		if (visible  && isAnythingActivated == false){
     			isAnythingActivated = true;
     			changeNumberActivated = true;   			
     			repaint();
     		}
     		if (e.getClickCount() == 2 && !e.isConsumed() && !isAnythingActivated){
     			e.consume();
     			changeNumberActivated = false;
     			this.visible = true;
            	this.setParametersL();
            	Base.saved = false;
     		}
    	}
    }	
	
    public void mousePressed(MouseEvent e){}
    //HIGHLIGHTING ON MOUSE ENTER
    public void mouseEntered(MouseEvent e){
    	
    	if (this.visible == false){
    		this.color = new Color(this.defaultColor.getRed(),this.defaultColor.getGreen(),this.defaultColor.getBlue(),this.defaultColor.getAlpha()+40);
    		this.repaint();
    	}    	
    }
    public void mouseReleased(MouseEvent e){}
    //DEHIGHLIGHTING ON MOUSE EXIT
    public void mouseExited(MouseEvent e){
    	
    	if (this.visible == false){
    		this.color = this.defaultColor;
    		this.repaint();
    	}    	
    }    
}

//THE A-FRAME OBSTACLE
/*
 * ONLY THIS OBSTACLE CLASS CONTAINS DETAILED COMMENTS, SINCE THE OTHER OBSTACLE CLASSES ARE VERY SIMILAR.
 */
class Aframe extends JPanel implements ActionListener
{
	/* DIMENSIONS AND SIZE CLASSES AS SET BY UKA RULES:	 * 
	 * 
	 * Constructed of two ramps wide hinged at the apex.
	Length of ramp: 2.74m (9’).
	Width of ramp: 914mm (3’). The base of the ramp can be 1.2m (4’)	
	The heights available must be: 1.7m (5’7”) and 1.6m. (5’3”)
	Slat depth: between 9mm and 15 mm.
	The last 1.067m (3ft 6ins) from the bottom of each ramp should be in different colour. Each ramp to
	have a non-slip rubber surface approved by UK Agility, and anti-slip slats at intervals of approximately
	279mm (11”) but not within 152mm (6”)of the start of the contact area.
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0,xx,yy;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM, widthM, heightM, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, toy, midi, standard, maxi, micro;

	public MouseAdapter myAdapter = new MouseAdapter() 
	{
		//HANDLING MOUSE CLICKS - ROTATING AND DISPLAYING MENU
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		//repaint();
        		Base.canvas.repaint();
    	    }	
    		
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
    	//LABELS DARKENING ON MOUSE ENTER
    	public void mouseEntered(MouseEvent e){
    		for (int i = 0; i < noOfDirections; i++){
    			for (int j = 0; j < 4; j++)
    			{
    				oneDirection[i].labels[j].defaultColor = new Color(oneDirection[i].labels[j].defaultColor.getRed(),
    						oneDirection[i].labels[j].defaultColor.getGreen(),oneDirection[i].labels[j].defaultColor.getBlue(),
    						(oneDirection[i].labels[j].defaultColor.getAlpha()+40)%255);
    				oneDirection[i].labels[j].setParametersL();
    				oneDirection[i].labels[j].repaint();
    			}
    		}
    	}
    	//LABELS LIGHTENING AFTER MOUSE EXIT
    	public void mouseExited(MouseEvent e){
    		for (int i = 0; i < noOfDirections; i++){
    			for (int j = 0; j < 4; j++)
    			{
    				oneDirection[i].labels[j].defaultColor = new Color(oneDirection[i].labels[j].defaultColor.getRed(),
    						oneDirection[i].labels[j].defaultColor.getGreen(),oneDirection[i].labels[j].defaultColor.getBlue(),
    						Math.max((oneDirection[i].labels[j].defaultColor.getAlpha()-40)%255,0));
    				oneDirection[i].labels[j].setParametersL();
    				oneDirection[i].labels[j].repaint();
    			}
    		}
    	}    	
	};
	
	//CHANGING OBSTACLE COORDINATES ON MOUSE DRAG
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{		
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
    //CONSTRUCTOR - MAKE NEW OBSTACLE WHERE THE CLICK ON THE CANVAS OCCURED
	public Aframe(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
		this.xx = x;
		this.yy = y;
		this.heightM = 1.7;
		this.widthM = 0.914;
    	this.setVisible(true);
    	//INITIALISING LABELS
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(2*(Math.sqrt(2.74*2.74-this.heightM*this.heightM)));
    	}
		this.setParameters();
		
		//INITIALISING MENU
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    micro = new MyMenuItem("Micro");
	    menu.add(micro);
	    micro.addActionListener(this);
	    toy = new MyMenuItem("Toy");
	    menu.add(toy);
	    toy.addActionListener(this);
	    midi = new MyMenuItem("Midi");
	    menu.add(midi);
	    midi.addActionListener(this);
	    standard = new MyMenuItem("Standard");
	    menu.add(standard);
	    standard.addActionListener(this);
	    maxi = new MyMenuItem("Maxi");
	    menu.add(maxi);
	    maxi.addActionListener(this);   
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);
	    this.setComponentPopupMenu(menu); 
	    
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
	//PAINT THE OBSTACLE ON A PROVIDED GRAPHICS, WITHOUT ROTATING
	public void paintAframe(Graphics2D g)
    {
		g.setColor(Base.colorMain);
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthM), (int)(Base.unit*this.lengthM));
		g.setColor(Base.colorEnds);
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthM), (int)((1.067/2.74)*Base.unit*this.lengthM/2));
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*(this.realLength - this.obstacleShiftY - (1.067/2.74)*this.lengthM/2)), (int)(Base.unit*this.widthM), (int)((1.067/2.74)*Base.unit*this.lengthM/2));
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.realLength/2)-1, (int)(Base.unit*this.widthM), 2);
    } 
	//ROTATE THE GRAPHICS ONTO WHICH THE OBSTACLE WILL BE PAINTED
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), 
        		(int)(Base.unit*this.realWidth/2), 
        		(int)(Base.unit*this.realLength/2)
        		);
        this.paintAframe(graphics2D);
        graphics2D.dispose();
	}
	//REFESH VALUES (AFTER DRAGGING, ROTATING, ETC.)
	public void setParameters()
	{		
		this.lengthM = 2*(Math.sqrt(2.74*2.74-this.heightM*this.heightM));
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	
		//REAL DIMENSIONS, CONSIDERING THE ROTATION OF THE OBSTACLE
		this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	
    	//MARGINS - DIFFERENCE BETWEEN THE REAL DIMENSIONS AND OBSTACLE DIMENSIONS
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		//CALCULATING THE LOCATION OF EACH LABEL PANEL
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    	this.oneDirection[1].setParameters(this.lengthM);
}
	
	//HANDLING MENU BUTTONS CLICKS
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(0,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
		if (e.getSource() == this.toy ||
				e.getSource() == this.midi ||
				e.getSource() == this.standard ||
				e.getSource() == this.maxi)
		{
            Base.saved = false;
			this.heightM = 1.7;
			this.repaint();
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.micro)
		{
            Base.saved = false;
			this.heightM = 1.6;
			this.repaint();
			Base.canvas.repaint();
		}
	}
	
	//WRITING PARAMETERES TO GIVEN FILE
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Double.toString(this.lengthM) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	//READING PARAMETERS FROM GIVEN FILE
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.aframeObstacles[index] = new Aframe(1,1,index);
		Canvas.aframeObstacles[index].x = x;
		Canvas.aframeObstacles[index].y = y;
		Canvas.aframeObstacles[index].lengthM = Double.parseDouble(parsed[counter]); counter++;
		Canvas.aframeObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.aframeObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.aframeObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.aframeObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.aframeObstacles[index].setVisible(true);
		Canvas.aframeObstacles[index].setParameters();
		Base.canvas.repaint();
	}
} 

//THE CollapsibleTunnel OBSTACLE
class CollapsibleTunnel extends JPanel implements ActionListener
{
	/*
	 * Entrance height: 540mm (1’9”) minimum.
	 * Entrance depth: 457mm (1’6”).
	Material diameter: 609mm (2’) minimum. 762mm (2’6”) maximum.
	Tunnel length: 3.048m (10’) minimum.
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public int[] coordinatesX = new int[4], coordinatesY = new int[4];
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = 3.048, widthEntranceM = 0.540, widthEndM = 0.700, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 1;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		//repaint();
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public CollapsibleTunnel(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(this.lengthM);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintCollapsibleTunnel(Graphics2D g)
    {
		/*
		 * Entrance height: 540mm (1’9”) minimum.
		 * Entrance depth: 457mm (1’6”).
		Material diameter: 609mm (2’) minimum. 762mm (2’6”) maximum.
		Tunnel length: 3.048m (10’) minimum.
		 */
		g.setColor(Base.colorEnds);
		g.fillRect((int)(Base.unit*(this.obstacleShiftX + (this.widthEndM-this.widthEntranceM)/2)), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthEntranceM), (int)(Base.unit*0.457));
		g.setColor(Base.colorMain);
		g.fillPolygon(this.coordinatesX, this.coordinatesY, 4);
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintCollapsibleTunnel(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
    	this.diagonal = Math.sqrt(this.widthEndM*this.widthEndM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthEndM/this.diagonal);
		this.begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
    	this.begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
    	this.end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
    	this.end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthEndM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
		this.coordinatesX[0] = (int)(Base.unit*(this.obstacleShiftX + (this.widthEndM-this.widthEntranceM)/2));
		this.coordinatesX[1] = (int)(Base.unit*(this.obstacleShiftX + (this.widthEndM-this.widthEntranceM)/2 + this.widthEntranceM));
		this.coordinatesX[2] = (int)(Base.unit*(this.obstacleShiftX+this.widthEndM));
		this.coordinatesX[3] = (int)(Base.unit*this.obstacleShiftX);
		this.coordinatesY[0] = (int)(Base.unit*(this.obstacleShiftY+0.457));
		this.coordinatesY[1] = this.coordinatesY[0];
		this.coordinatesY[2] = (int)(Base.unit*(this.realLength - this.obstacleShiftY));
		this.coordinatesY[3] = this.coordinatesY[2];
		
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		labelDistance = Label.size + (int)(Base.unit*this.widthEndM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(1,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	
	
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";"
				+ Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";"
						+ Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";"
						+ Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	
	
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.collapsibleTunnelObstacles[index] = new CollapsibleTunnel(1,1,index);
		Canvas.collapsibleTunnelObstacles[index].x = x;
		Canvas.collapsibleTunnelObstacles[index].y = y;
		Canvas.collapsibleTunnelObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.collapsibleTunnelObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.collapsibleTunnelObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.collapsibleTunnelObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.collapsibleTunnelObstacles[index].setVisible(true);
		Canvas.collapsibleTunnelObstacles[index].setParameters();
		Base.canvas.repaint();
	}
	

}

//THE Dogwalk OBSTACLE
class Dogwalk extends JPanel implements ActionListener
{
	/*Length of plank: 3.66m (12’).
	Width of plank: 12ins 305mm (12”)
	Central plank height: 1.37m (4’6”)
	The last 914mm (3ft) from the bottom of each ramp should be a different colour.
	*/
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM, widthM = 0.305, heightM = 1.37, obstacleAngle, realX, realY, realWidth, realLength, plankLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public Dogwalk(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
		this.plankLength = Math.sqrt(3.66*3.66-this.heightM*this.heightM);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(0);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintDogwalk(Graphics2D g)
    {
		g.setColor(Base.colorMain);
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthM), (int)(Base.unit*this.lengthM));
		g.setColor(Base.colorEnds);
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthM), (int)((0.914/3.66)*Base.unit*this.plankLength));
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*(this.obstacleShiftY+this.lengthM-(0.914/3.66)*this.plankLength)), (int)(Base.unit*this.widthM), (int)((0.914/3.66)*Base.unit*this.plankLength));
		
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*(this.obstacleShiftY + this.plankLength)), (int)(Base.unit*this.widthM), (int)(Base.unit*Base.lineConstant));
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*(this.obstacleShiftY + this.plankLength + 3.66)), (int)(Base.unit*this.widthM), (int)(Base.unit*Base.lineConstant));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintDogwalk(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
    	this.plankLength = Math.sqrt(3.66*3.66-this.heightM*this.heightM);
		this.lengthM = 2*this.plankLength + 3.66;
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    	this.oneDirection[1].setParameters(this.lengthM);
}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(2,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.dogwalkObstacles[index] = new Dogwalk(1,1,index);
		Canvas.dogwalkObstacles[index].x = x;
		Canvas.dogwalkObstacles[index].y = y;
		Canvas.dogwalkObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.dogwalkObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.dogwalkObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.dogwalkObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.dogwalkObstacles[index].setVisible(true);
		Canvas.dogwalkObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE Jump OBSTACLE
class Jump extends JPanel implements ActionListener
{
	/*Width of wings: 483 mm (18”) minimum.
	Length of poles: 1.22m (4’) minimum 1.524m (5’) maximum.
	Plank length: 1.22m (4’) minimum 1.524 (5’) maximum.
	Pole thickness: 43mm (1.75 in) minimum.
	Micro (200mm) jump height may be a minimum of 178mm.
	 * 			Beg Nov Sen Cham
	Toy 		235 260 300 300
	Midi 		265 335 400 400
	Standard 	365 455 550 550
	Maxi 		435 540 650 650
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = 0.6, widthM = 1.22+2*0.483, obstacleAngle, realX, realY, realWidth, realLength, plankLength = 1.22, wingLength = 0.483, wingWidth = 0.08;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, toy, midi, standard, maxi;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public Jump(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(0);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintJump(Graphics2D g)
    {
		g.setColor(Base.colorEnds);
		
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-this.wingWidth/2)*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.widthM-this.wingLength)*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-this.wingWidth/2)*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-0.025)*Base.unit),(int)(this.widthM*Base.unit), (int)(0.05*Base.unit));
		
		g.fillRect((int)((this.obstacleShiftX+this.wingLength-this.wingWidth)*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.wingLength+this.plankLength-this.wingWidth)*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintJump(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
		this.begin.x = (int)(this.x*Base.unit);
    	this.begin.y = (int)(Base.canvas_height-this.y*Base.unit);
    	this.end.x = (int)(this.x*Base.unit);
    	this.end.y = (int)(Base.canvas_height-this.y*Base.unit);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
		
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(0);
    	this.oneDirection[1].setParameters(0);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(3,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.jumpObstacles[index] = new Jump(1,1,index);
		Canvas.jumpObstacles[index].x = x;
		Canvas.jumpObstacles[index].y = y;
		Canvas.jumpObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.jumpObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.jumpObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.jumpObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.jumpObstacles[index].setVisible(true);
		Canvas.jumpObstacles[index].setParameters();
		Base.canvas.repaint();
	}
 }

//THE LongJump OBSTACLE
class LongJump extends JPanel implements ActionListener
{
	/*
	 * To comprise 2 to 5 units. Toy – 2 units, Midi – 3 units, Standard – 4 units, Maxi – maximum 5 units.
	Unit length: 610mm (24”) minimum.
	Maximum length of jump: as per UKA rules and regulations.
	First unit height: 127mm (5ins).
	Fifth unit height: 381mm (15ins).
	The second, third and forth unit heights should be evenly distributed between the first and fifth.
	Marker poles height: 1.219m (4ft) minimum.
	These should be placed at each corner and should not be attached to any part of the obstacle.
	600, 800, 1100, 1300
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0, noOfUnits = 4;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = 1.1, widthM = 0.61, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 1;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, toy, midi, standard, maxi;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		//repaint();
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public LongJump(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(0);
    	}
		this.setParameters();
		
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    toy = new MyMenuItem("Toy");
	    menu.add(toy);
	    toy.addActionListener(this);
	    midi = new MyMenuItem("Midi");
	    menu.add(midi);
	    midi.addActionListener(this);
	    standard = new MyMenuItem("Standard");
	    menu.add(standard);
	    standard.addActionListener(this);
	    maxi = new MyMenuItem("Maxi");
	    menu.add(maxi);
	    maxi.addActionListener(this);   
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
 		Base.canvas.repaint();
    }
    
	public void paintLongJump(Graphics2D g)
    {
		g.setColor(Base.colorArrows);
		g.drawLine((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+this.lengthM)*Base.unit));
		g.drawLine((int)((this.obstacleShiftX+this.widthM)*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+this.lengthM)*Base.unit));
		g.setColor(Base.colorEnds);
        switch (this.noOfUnits){
        	case 2:
        	{
        		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int)Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.4+this.obstacleShiftX)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                break;
        	}
        	case 3:
        	{
        		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.3+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.6+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                break;
        	}
        	case 4:
        	{
        		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit),                  (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.3+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.6+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.9+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                break;
        	}
        	case 5:
        	{
        		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.275+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.55+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((0.825+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                g.fillRect((int)(this.obstacleShiftX*Base.unit), (int) Math.round((1.1+this.obstacleShiftY)*Base.unit), (int)(this.widthM*Base.unit), (int) Math.round(0.2*Base.unit));
                break;
        	}  
        } 
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintLongJump(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(4,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
		if (e.getSource() == this.toy)
		{
            Base.saved = false;
			this.noOfUnits = 2;
	    	this.lengthM = 0.6;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.midi)
		{
            Base.saved = false;
			this.noOfUnits = 3;
	    	this.lengthM = 0.8;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.standard)
		{
            Base.saved = false;
			this.noOfUnits = 4;
	    	this.lengthM = 1.1;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.maxi)
		{
            Base.saved = false;
			this.noOfUnits = 5;
	    	this.lengthM = 1.3;
	    	Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";" + Integer.toString(this.noOfUnits) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.longJumpObstacles[index] = new LongJump(1,1,index);
		Canvas.longJumpObstacles[index].x = x;
		Canvas.longJumpObstacles[index].y = y;
		Canvas.longJumpObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		Canvas.longJumpObstacles[index].noOfUnits = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.longJumpObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.longJumpObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.longJumpObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.longJumpObstacles[index].setVisible(true);
		Canvas.longJumpObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE PipeTunnel OBSTACLE
class PipeTunnel extends JPanel implements ActionListener//, KeyListener
{
	/*Diameter: 600mm (23.5”)minimum.
	Length: 3.048m (10’) minimum.
	pi*r = 3.048 => r = 3.048/pi
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;//, tunnelAngle = 0;
	public double cx, cy, x, y, diagonal, obstacleShiftX, obstacleShiftY, radiusNegative = 0, radius = 3.048/Math.PI, tunnelWidth = 0.6, obstacleAngle = 0, lengthM, widthM, tunnelAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public int[] coordinatesX = new int[20], coordinatesY = new int[20];
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, changeRadius;
	public JSlider radiusSlider = new JSlider();
	//public boolean changeRadiusEnabled = false;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
         		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
    			radiusSlider.setValue((int)(50 + 50*3.048/radius));
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
			if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public PipeTunnel(int x, int y, int index)
    {
		this.index = index;
		this.widthM = 3.048;
		this.lengthM = 3.048;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(3.048);
    	}
		this.setParameters();
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    changeRadius = new MyMenuItem("Change radius");
	    menu.add(changeRadius);
	    changeRadius.addActionListener(this);
	    changeRadius.setPreferredSize(new Dimension(changeRadius.getPreferredSize().width,40));
	    menu.addSeparator();
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);
	    radiusSlider.setVisible(true);
	    radiusSlider = new JSlider(0,100,50);
	    radiusSlider.setMajorTickSpacing(25);
	    radiusSlider.setMinorTickSpacing(10);
	    radiusSlider.setSnapToTicks(true);
	    radiusSlider.setPaintLabels(true);
        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
        for (int i = 0; i < 5;i++){
        	labelTable.put(new Integer(i*25), new JLabel(Integer.toString(-180 + i*90) + "°"));
        }
        radiusSlider.setLabelTable(labelTable);
	    radiusSlider.setSize(changeRadius.getPreferredSize().width,40);
	    radiusSlider.addChangeListener(new ChangeListener(){
	    	public void stateChanged(ChangeEvent e){
	    		if (((JSlider)e.getSource()).getValue() == 50) {
	    			radius = 0;
	    		}else{
	    			radius = 50*3.048/(Math.PI*(((JSlider)e.getSource()).getValue()-50));
	    		}
	    		Base.canvas.repaint();
                Base.saved = false;
	    	}
	    });
	    changeRadius.add(radiusSlider);	    
	    
	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
 		Base.canvas.repaint();
    }
    
	public void paintPipeTunnel(Graphics2D g)
    {
		g.setColor(Base.colorEnds);
        g.fillPolygon(this.coordinatesX, this.coordinatesY, 20);
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
		graphics2D.rotate(Math.toRadians(this.angle+this.radiusNegative*180), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintPipeTunnel(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		if (this.radius == 0) {this.radius = 100;}
	
		if (this.radius < 0) {this.radiusNegative = 1;}else{this.radiusNegative = 0;}
		
		this.tunnelAngle = 3.048/Math.abs(this.radius);
		this.widthM = 2*(Math.abs(this.radius)+this.tunnelWidth/2)*Math.sin(this.tunnelAngle/2);
		this.lengthM = Math.abs(this.radius) + this.tunnelWidth/2 - (Math.abs(this.radius)-this.tunnelWidth/2)*Math.cos(this.tunnelAngle/2);

		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
		this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;

		this.cx = this.widthM/2;
		this.cy = this.lengthM - Math.abs(this.radius) - this.tunnelWidth/2;

		for (int i = 0; i < 10; i++)
		{
			this.coordinatesX[i] = (int)(Base.unit*(this.obstacleShiftX + this.cx + (Math.abs(this.radius)+this.tunnelWidth/2)*Math.sin(-this.tunnelAngle/2 + i*this.tunnelAngle/9)));
			this.coordinatesY[i] = (int)(Base.unit*(this.obstacleShiftY + this.cy + (Math.abs(this.radius) + this.tunnelWidth/2)*Math.cos(-this.tunnelAngle/2 + i*this.tunnelAngle/9)));
		}
		for (int i = 9; i >= 0; i--)
		{
			this.coordinatesX[19-i] = (int)(Base.unit*(this.obstacleShiftX + this.cx + (Math.abs(this.radius)-this.tunnelWidth/2)*Math.sin(-this.tunnelAngle/2 + i*this.tunnelAngle/9)));
			this.coordinatesY[19-i] = (int)(Base.unit*(this.obstacleShiftY + this.cy + (Math.abs(this.radius)-this.tunnelWidth/2)*Math.cos(-this.tunnelAngle/2 + i*this.tunnelAngle/9)));
		}
						
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	
    	this.end.x = (int)(Base.unit*this.x - Math.cos(Math.toRadians(this.angle))*(-this.coordinatesX[0] - this.coordinatesX[19] + Base.unit*this.realWidth)/2.0 + (Math.abs(this.radius)/this.radius)*Math.sin(Math.toRadians(this.angle))*((Base.unit*this.realLength - this.coordinatesY[0] - this.coordinatesY[19])/2.0));
		this.end.y = (int)(Base.canvas_height - Base.unit*this.y - Math.sin(Math.toRadians(this.angle))*(-this.coordinatesX[0] - this.coordinatesX[19] + Base.unit*this.realWidth)/2.0 - (Math.abs(this.radius)/this.radius)*Math.cos(Math.toRadians(this.angle))*((Base.unit*this.realLength - this.coordinatesY[0] - this.coordinatesY[19])/2.0));
		this.begin.x = (int)(Base.unit*this.x + Math.cos(Math.toRadians(this.angle))*(-this.coordinatesX[0] - this.coordinatesX[19] + Base.unit*this.realWidth)/2.0 + (Math.abs(this.radius)/this.radius)*Math.sin(Math.toRadians(this.angle))*((Base.unit*this.realLength - this.coordinatesY[0] - this.coordinatesY[19])/2.0));
		this.begin.y = (int)(Base.canvas_height - Base.unit*this.y + Math.sin(Math.toRadians(this.angle))*(-this.coordinatesX[0] - this.coordinatesX[19] + Base.unit*this.realWidth)/2.0 - (Math.abs(this.radius)/this.radius)*Math.cos(Math.toRadians(this.angle))*((Base.unit*this.realLength - this.coordinatesY[0] - this.coordinatesY[19])/2.0));
			
		this.oneDirection[0].begin.x = this.begin.x;
		this.oneDirection[0].begin.y = this.begin.y;
		this.oneDirection[0].end.x = this.end.x;
		this.oneDirection[0].end.y = this.end.y;

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + (1-2*this.radiusNegative)*Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - (1-2*this.radiusNegative)*Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance + (1-2*this.radiusNegative)*Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance - (1-2*this.radiusNegative)*Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(0);
    	this.oneDirection[1].setParameters(0);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(5,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";" + Double.toString(this.radius) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.pipeTunnelObstacles[index] = new PipeTunnel(1,1,index);
		Canvas.pipeTunnelObstacles[index].x = x;
		Canvas.pipeTunnelObstacles[index].y = y;
		Canvas.pipeTunnelObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		Canvas.pipeTunnelObstacles[index].radius = Double.parseDouble(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.pipeTunnelObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.pipeTunnelObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.pipeTunnelObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.pipeTunnelObstacles[index].setVisible(true);
		Canvas.pipeTunnelObstacles[index].setParameters();
		Base.canvas.repaint();
	}
 
}

// THE SEESAW OBSTACLE
class Seesaw extends JPanel implements ActionListener
{
	/*Length of plank: 3.66m (12”).
	  Width of plank: 0.305 m (12’). 
	  The last 0.914m (3ft) from each end should be a different colour
	*/
	public static int labelDistance = Label.size;
	public static Timer timer;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = Math.sqrt(3.66*3.66-1), widthM = 0.305, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 1;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public Seesaw(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(this.lengthM);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);
	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintSeesaw(Graphics2D g)
    {
		g.setColor(Base.colorMain);
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int)(this.lengthM*Base.unit));
		g.setColor(Base.colorEnds);
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int)(0.914*Base.unit));
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM-0.914)*Base.unit), (int)(this.widthM*Base.unit), (int)(0.914*Base.unit));
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.realLength/2),(int)(Base.unit*this.widthM),(int)(Base.unit*Base.lineConstant));
		g.setColor(Base.colorArrows);
		g.setStroke(new BasicStroke(1));
		g.drawLine((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+1)*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+1.5)*Base.unit));
		g.drawLine((int)((this.obstacleShiftX+this.widthM)*Base.unit), (int)((this.obstacleShiftY+1)*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+1.5)*Base.unit));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
		graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintSeesaw(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
		this.begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
    	this.begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
    	this.end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
    	this.end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    }
	
	public void actionPerformed(ActionEvent e){
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(6,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.seesawObstacles[index] = new Seesaw(1,1,index);
		Canvas.seesawObstacles[index].x = x;
		Canvas.seesawObstacles[index].y = y;
		Canvas.seesawObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.seesawObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.seesawObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.seesawObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.seesawObstacles[index].setVisible(true);
		Canvas.seesawObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE SpreadJump OBSTACLE
class SpreadJump extends JPanel implements ActionListener
{
	/*Width of wings: 483 mm (18”) minimum.
	Length of poles: 1.22m (4’) minimum 1.524m (5’) maximum.
	Plank length: 1.22m (4’) minimum 1.524 (5’) maximum.
	Pole thickness: 43mm (1.75 in) minimum.
	Micro (200mm) jump height may be a minimum of 178mm.
	 * 			Beg Nov Sen Cham
	Toy 		235 260 300 300
	Midi 		265 335 400 400
	Standard 	365 455 550 550
	Maxi 		435 540 650 650
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = 0.550+2*0.043, widthM = 1.22+2*0.483, obstacleAngle, realX, realY, realWidth, realLength, plankLength = 1.22, wingLength = 0.483, wingWidth = 0.08;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 1;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, toy, midi, standard, maxi;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public SpreadJump(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(this.lengthM);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    toy = new MyMenuItem("Toy");
	    menu.add(toy);
	    toy.addActionListener(this);
	    midi = new MyMenuItem("Midi");
	    menu.add(midi);
	    midi.addActionListener(this);
	    standard = new MyMenuItem("Standard");
	    menu.add(standard);
	    standard.addActionListener(this);
	    maxi = new MyMenuItem("Maxi");
	    menu.add(maxi);
	    maxi.addActionListener(this);   
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintSpreadJump(Graphics2D g)
    {
		g.setColor(Base.colorArrows);
		g.drawLine((int)((this.obstacleShiftX+0.8)*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+this.lengthM)*Base.unit));
		g.drawLine((int)((this.obstacleShiftX+this.widthM-0.8)*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)((this.obstacleShiftX+this.widthM/2)*Base.unit), (int)((this.obstacleShiftY+this.lengthM)*Base.unit));
		g.setColor(Base.colorEnds);
		
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.widthM-this.wingLength)*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM-this.wingWidth)*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.widthM-this.wingLength)*Base.unit), (int)((this.obstacleShiftY+this.lengthM-this.wingWidth)*Base.unit),(int)(this.wingLength*Base.unit), (int)(this.wingWidth*Base.unit));
		
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)(this.obstacleShiftY*Base.unit), (int)(this.widthM*Base.unit), (int)(0.05*Base.unit));
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM-0.043)*Base.unit),(int)(this.widthM*Base.unit), (int)(0.05*Base.unit));
	
		g.fillRect((int)((this.obstacleShiftX+this.wingLength-this.wingWidth)*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.wingLength+this.plankLength-this.wingWidth)*Base.unit), (int)(this.obstacleShiftY*Base.unit),(int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintSpreadJump(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(7,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
		if (e.getSource() == this.toy)
		{
            Base.saved = false;
	    	this.lengthM = 2*0.043 + 0.300;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.midi)
		{
            Base.saved = false;
			this.lengthM = 2*0.043 + 0.400;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.standard)
		{
            Base.saved = false;
	    	this.lengthM = 2*0.043 + 0.550;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.maxi)
		{
            Base.saved = false;
	    	this.lengthM = 2*0.043 + 0.650;
	    	Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.spreadJumpObstacles[index] = new SpreadJump(1,1,index);
		Canvas.spreadJumpObstacles[index].x = x;
		Canvas.spreadJumpObstacles[index].y = y;
		Canvas.spreadJumpObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.spreadJumpObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.spreadJumpObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.spreadJumpObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.spreadJumpObstacles[index].setVisible(true);
		Canvas.spreadJumpObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE Table OBSTACLE
class Table extends JPanel implements ActionListener
{
	/*
	 * Table top: 941mm (3’) square minimum.
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, lengthM = 0.941, widthM = 0.941, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 4;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public Table(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(this.lengthM);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintTable(Graphics2D g)
    {
		g.setColor(Color.green);
		////g.fillRect(0,0,(int)(this.realWidth*Base.unit),(int)(this.realLength*Base.unit));
		g.setColor(Base.colorEnds);
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.widthM), (int)(Base.unit*this.lengthM));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintTable(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
		
		this.oneDirection[2].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle+90))*this.lengthM/2.0);
		this.oneDirection[2].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle+90))*this.lengthM/2.0);
		this.oneDirection[2].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle+90))*this.lengthM/2.0);
		this.oneDirection[2].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle+90))*this.lengthM/2.0);
		
		this.oneDirection[3].begin.x = this.oneDirection[2].end.x;  
		this.oneDirection[3].begin.y = this.oneDirection[2].end.y;
		this.oneDirection[3].end.x = this.oneDirection[2].begin.x;
		this.oneDirection[3].end.y = this.oneDirection[2].begin.y;
		
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[2].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle+90))*labelDistance + Math.sin(Math.toRadians(angle+90))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[2].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle+90))*labelDistance - Math.cos(Math.toRadians(angle+90))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[3].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle+90))*labelDistance - Math.sin(Math.toRadians(angle+90))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[3].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle+90))*labelDistance + Math.cos(Math.toRadians(angle+90))*(Base.unit*this.lengthM/2.0)-Label.size);
    	
    	this.oneDirection[0].setParameters(0);
    	this.oneDirection[1].setParameters(0);
    	this.oneDirection[2].setParameters(0);
    	this.oneDirection[3].setParameters(0);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(8,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.tableObstacles[index] = new Table(1,1,index);
		Canvas.tableObstacles[index].x = x;
		Canvas.tableObstacles[index].y = y;
		Canvas.tableObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.tableObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.tableObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.tableObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.tableObstacles[index].setVisible(true);
		Canvas.tableObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE TyreJump OBSTACLE
class TyreJump extends JPanel implements ActionListener
{
	/*Width of wings: 483 mm (18”) minimum.
	Length of poles: 1.22m (4’) minimum 1.524m (5’) maximum.
	Plank length: 1.22m (4’) minimum 1.524 (5’) maximum.
	Pole thickness: 43mm (1.75 in) minimum.
	Aperture diameter: 457mm (1’6”) minimum
	 */
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, radius = 0.5/2.0, lengthM = 0.6, widthM = 1.0, obstacleAngle, realX, realY, realWidth, realLength, wingWidth = 0.08;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete, toy, midi, standard, maxi;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };  
	public TyreJump(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(0);
    	}
		this.setParameters();
    	//Base.canvas.add(this);
    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintTyreJump(Graphics2D g)
    {
		g.setColor(Color.green);
		////g.fillRect(0,0,(int)(this.realWidth*Base.unit),(int)(this.realLength*Base.unit));
		g.setColor(Base.colorEnds);
		
		//WINGS (LEGS)
		g.fillRect((int)(this.obstacleShiftX*Base.unit),(int)(this.obstacleShiftY*Base.unit), (int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.widthM-this.wingWidth)*Base.unit),(int)(this.obstacleShiftY*Base.unit), (int)(this.wingWidth*Base.unit), (int)(this.lengthM*Base.unit));
		
		//PLANK (WHICH HOLDS THE TYRE)
		g.fillRect((int)(this.obstacleShiftX*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-this.wingWidth/2)*Base.unit), (int)((this.widthM/2-this.radius)*Base.unit), (int)(this.wingWidth*Base.unit));
		g.fillRect((int)((this.obstacleShiftX+this.widthM/2+this.radius)*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-this.wingWidth/2)*Base.unit), (int)((this.widthM/2-this.radius)*Base.unit), (int)(this.wingWidth*Base.unit));

		//TYRE
		//http://stackoverflow.com/questions/2839508/java2d-increase-the-line-width
		g.setStroke(new BasicStroke((int)(Base.unit*Base.lineConstant)));
		g.drawOval((int)((this.obstacleShiftX+this.widthM/2-this.radius)*Base.unit), (int)((this.obstacleShiftY+this.lengthM/2-this.radius)*Base.unit), (int)((2*this.radius)*Base.unit), (int)(2*this.radius*Base.unit));
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintTyreJump(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
		this.begin.x = (int)(this.x*Base.unit);
    	this.begin.y = (int)(Base.canvas_height-this.y*Base.unit);
    	this.end.x = (int)(this.x*Base.unit);
    	this.end.y = (int)(Base.canvas_height-this.y*Base.unit);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(0);
    	this.oneDirection[1].setParameters(0);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(9,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.tyreJumpObstacles[index] = new TyreJump(1,1,index);
		Canvas.tyreJumpObstacles[index].x = x;
		Canvas.tyreJumpObstacles[index].y = y;
		Canvas.tyreJumpObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.tyreJumpObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.tyreJumpObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.tyreJumpObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.tyreJumpObstacles[index].setVisible(true);
		Canvas.tyreJumpObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE WALL JUMP OBSTACLE
class WallJump extends JPanel implements ActionListener
{
	/*
	The width of central jumping area is 1220mm (4’), excluding pillars
	Depth of wall is 280mm (11.02”) at base and 135mm (5.31’) at highest point
	Pillar height 1220mm (4’) and 300mm (11.81”) square width
	*/
	public static int labelDistance = Label.size;
	public int index, angle = 0;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, pillarWidth = 0.3, wallLength = 1.22, wallWidth = 0.135, lengthM = pillarWidth, widthM = 2*pillarWidth+wallLength, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public static int noOfDirections = 2;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MyPopupMenu menu;
	public MyMenuItem delete;
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public WallJump(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
		this.setVisible(true);
		for (int i = 0; i < this.noOfDirections; i++)
		{
			this.oneDirection[i] = new OneDirection(this.wallWidth);
		}
		this.setParameters();
		Base.canvas.add(this);
			    	
    	menu = new MyPopupMenu();
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintWallJump(Graphics2D g)
    {
		g.setColor(Base.colorEnds);
		
		//PILLARS
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.pillarWidth), (int)(Base.unit*this.pillarWidth));
		g.fillRect((int)(Base.unit*(this.obstacleShiftX+this.widthM-this.pillarWidth)), (int)(Base.unit*this.obstacleShiftY), (int)(Base.unit*this.pillarWidth), (int)(Base.unit*this.pillarWidth));
		
		//WALL
		g.fillRect((int)(Base.unit*this.obstacleShiftX), (int)(Base.unit*(this.obstacleShiftY+this.lengthM/2-this.wallWidth/2)), (int)(Base.unit*this.widthM), (int)(Base.unit*this.wallWidth));		
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintWallJump(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
    	this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.wallWidth/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.wallWidth/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.wallWidth/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.wallWidth/2.0);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.wallWidth);
    	this.oneDirection[1].setParameters(this.wallWidth);
    }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(10,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.wallJumpObstacles[index] = new WallJump(1,1,index);
		Canvas.wallJumpObstacles[index].x = x;
		Canvas.wallJumpObstacles[index].y = y;
		Canvas.wallJumpObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.wallJumpObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.wallJumpObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.wallJumpObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.wallJumpObstacles[index].setVisible(true);
		Canvas.wallJumpObstacles[index].setParameters();
		Base.canvas.repaint();
	}
}

//THE WeavePoles OBSTACLE
class WeavePoles extends JPanel implements ActionListener
{
	/*
	 * Pole height: 762mm (2’6”)
	Pole diameter: between 30mm (1.18”)to 38mm (1.25”).
	Distance between poles: 600mm.
	The number of poles should be six or twelve.
	The base must have support bars at the bottom of each pole and they must be positioned away from
	the side a dog would normally travel to negotiate each pole.
	 */
	public static int labelDistance = Label.size, noOfDirections = 2;
	public int index, angle = 0, noOfPoles = 6;
	public double x, y, diagonal, obstacleShiftX, obstacleShiftY, poleWidth = 0.05, lengthM = noOfPoles*poleWidth+(noOfPoles-1)*0.6, widthM = 0.6, obstacleAngle, realX, realY, realWidth, realLength;
	public Point begin = new Point(),end = new Point(), labelLocation = new Point();
	public JPanel labelsPanel = new JPanel();
	public Label[] labels = new Label[4];
	public MyPopupMenu menu;
	public MyMenuItem delete, number6, number12;
	public OneDirection[] oneDirection = new OneDirection[noOfDirections];
	public MouseAdapter myAdapter = new MouseAdapter() 
	{
    	public void mouseClicked(MouseEvent e)
    	{
    		if (Label.isAnythingActivated){return;}
    		//http://www.codeprogress.com/java/showSamples.php?key=HandleMouseLeftRightClick&index=31
    		if(e.getButton() == MouseEvent.BUTTON1)
    	    {
        		angle += Base.angleUnit;
        		setParameters(); Base.saved = false;
        		Base.canvas.repaint();
    	    }	    
    		if (e.isPopupTrigger()) 
    		{
                menu.show(e.getComponent(),e.getX(), e.getY());
            }
    	}
	};
	public MouseMotionAdapter myMotionAdapter = new MouseMotionAdapter()
	{
        public void mouseDragged(MouseEvent e) 
        { 
            Base.saved = false;
        	if (Label.isAnythingActivated){return;}
        	x = 1.0*(-Base.canvas.getLocationOnScreen().x + e.getXOnScreen()-Base.canvas_x)/(1.0*Base.unit);
        	y = 1.0*(Base.canvas.getLocationOnScreen().y + Base.canvas_height-e.getYOnScreen())/(1.0*Base.unit);
    		Base.canvas.repaint();
        }
    };
    
	public WeavePoles(int x, int y, int index)
    {
		this.index = index;
		this.x = 1.0*x/(1.0*Base.unit);
		this.y = 1.0*(Base.canvas_height-y)/(1.0*Base.unit);
		
    	this.setVisible(true);
    	for (int i = 0; i < this.noOfDirections; i++)
    	{
    		this.oneDirection[i] = new OneDirection(this.lengthM);
    	}
		this.setParameters();
    	//Base.canvas.add(this);      	  	
    	
    	menu = new MyPopupMenu();
    	menu.setBackground(new Color(200,200,200,50));
	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();
	    menu.addPopupMenuListener(popupMenuListener);
	    number6 = new MyMenuItem("6 poles");
	    menu.add(number6);
	    number6.addActionListener(this);
	    number12 = new MyMenuItem("12 poles");
	    menu.add(number12);
	    number12.addActionListener(this);
	    delete = new MyMenuItem("Delete");
	    menu.add(delete);
	    delete.addActionListener(this);

	    this.setComponentPopupMenu(menu);
    	this.addMouseListener(myAdapter);
    	this.addMouseMotionListener(myMotionAdapter);
		Base.canvas.repaint();
    }
    
	public void paintWeavePoles(Graphics2D g)
    {
		g.setColor(Base.colorEnds);
		g.fillRect((int)((this.obstacleShiftX+this.widthM/2-this.poleWidth/2)*Base.unit),(int)(this.obstacleShiftY*Base.unit), (int)(this.poleWidth*Base.unit), (int)(this.lengthM*Base.unit));
		for (int i = 0; i < this.noOfPoles; i++)
		{
			if (i%2 == 0){
				g.fillRect((int)((this.obstacleShiftX+this.widthM/2)*Base.unit),(int)((this.obstacleShiftY+i*(this.poleWidth+0.6))*Base.unit), (int)(0.5*this.widthM*Base.unit), (int)(this.poleWidth*Base.unit));
			}else{
				g.fillRect((int)(this.obstacleShiftX*Base.unit),(int)((this.obstacleShiftY+i*(this.poleWidth+0.6))*Base.unit), (int)(0.5*this.widthM*Base.unit), (int)(this.poleWidth*Base.unit));
			}
		}
    } 
	
	public void paintComponent(Graphics g) 
	{
		this.setParameters();
		Graphics2D graphics2D = (Graphics2D)g.create();
        graphics2D.rotate(Math.toRadians(this.angle), (int)(Base.unit*this.realWidth/2), (int)(Base.unit*this.realLength/2));
        this.paintWeavePoles(graphics2D);
        graphics2D.dispose();
	}
	
	public void setParameters()
	{		
		this.diagonal = Math.sqrt(this.widthM*this.widthM+this.lengthM*this.lengthM);
		this.obstacleAngle = Math.asin(this.widthM/this.diagonal);
		this.realWidth = Math.max(Math.abs(Math.sin(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.sin(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realLength = Math.max(Math.abs(Math.cos(Math.toRadians(this.angle)-this.obstacleAngle)), Math.abs(Math.cos(Math.toRadians(this.angle)+this.obstacleAngle)))*this.diagonal;
    	this.realX = this.x - this.realWidth/2;
    	this.realY = (1.0*Base.canvas_height)/(1.0*Base.unit) - this.y - this.realLength/2;
    	this.setBounds((int)(Base.unit*this.realX), (int)(Base.unit*this.realY), (int)(Base.unit*this.realWidth), (int)(Base.unit*this.realLength));
    	this.obstacleShiftX = (this.realWidth-this.widthM)/2;
		this.obstacleShiftY = (this.realLength-this.lengthM)/2;
    	
		this.oneDirection[0].begin.x = (int)(this.x*Base.unit + Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].begin.y = (int)(Base.canvas_height-this.y*Base.unit - Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.x = (int)(this.x*Base.unit - Base.unit*Math.sin(Math.toRadians(angle))*this.lengthM/2.0);
		this.oneDirection[0].end.y = (int)(Base.canvas_height-this.y*Base.unit + Base.unit*Math.cos(Math.toRadians(angle))*this.lengthM/2.0);

		this.oneDirection[1].begin.x = this.oneDirection[0].end.x;  
		this.oneDirection[1].begin.y = this.oneDirection[0].end.y;
		this.oneDirection[1].end.x = this.oneDirection[0].begin.x;
		this.oneDirection[1].end.y = this.oneDirection[0].begin.y;
    	
		labelDistance = Label.size + (int)(Base.unit*this.widthM/2);
    	this.oneDirection[0].paintAt.x = (int)(this.x*Base.unit + Math.cos(Math.toRadians(angle))*labelDistance + Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit + Math.sin(Math.toRadians(angle))*labelDistance - Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.x = (int)(this.x*Base.unit - Math.cos(Math.toRadians(angle))*labelDistance - Math.sin(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[1].paintAt.y = (int)(Base.canvas_height-this.y*Base.unit - Math.sin(Math.toRadians(angle))*labelDistance + Math.cos(Math.toRadians(angle))*(Base.unit*this.lengthM/2.0)-Label.size);
    	this.oneDirection[0].setParameters(this.lengthM);
    	this.oneDirection[1].setParameters(this.lengthM);
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == this.delete)
		{
			Canvas.removeObstacle(11,this.index);
			this.setVisible(false);
			this.removeMouseListener(myAdapter);
			Base.canvas.repaint();
		}
		if (e.getSource() == this.number6)
		{
            Base.saved = false;
	    	this.noOfPoles = 6;
	    	this.lengthM = this.noOfPoles*this.poleWidth+(this.noOfPoles-1)*0.6;
	    	Base.canvas.repaint();
		}
		if (e.getSource() == this.number12)
		{
            Base.saved = false;
			this.noOfPoles = 12;
	    	this.lengthM = this.noOfPoles*this.poleWidth+(this.noOfPoles-1)*0.6;
	    	Base.canvas.repaint();
		}
	}
	public void print(BufferedWriter bw) throws IOException
	{
		String str = Integer.toString(this.index) + ";" + Double.toString(this.x) + ";" + Double.toString(this.y) + ";" + Integer.toString(this.angle) + ";" + Integer.toString(this.noOfPoles) + ";";
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				str = str + Integer.toString(this.oneDirection[i].labels[j].number) + ";" + Integer.toString(this.oneDirection[i].labels[j].currentColor) + ";" + Boolean.toString(this.oneDirection[i].labels[j].visible) + ";";
			}
		}
		bw.write(str);
	}
	public static void read(BufferedReader br) throws IOException
	{
		String line = br.readLine();
		String[] parsed = line.split(";"); 
		int counter = 0;
		int index = Integer.parseInt(parsed[counter]); counter++;
		double x = Double.parseDouble(parsed[counter]); counter++;
		double y = Double.parseDouble(parsed[counter]); counter++;
		Canvas.weavePolesObstacles[index] = new WeavePoles(1,1,index);
		Canvas.weavePolesObstacles[index].x = x;
		Canvas.weavePolesObstacles[index].y = y;
		Canvas.weavePolesObstacles[index].angle = Integer.parseInt(parsed[counter]); counter++;
		Canvas.weavePolesObstacles[index].noOfPoles = Integer.parseInt(parsed[counter]); counter++;
		for (int i = 0; i < noOfDirections; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				Canvas.weavePolesObstacles[index].oneDirection[i].labels[j].number = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.weavePolesObstacles[index].oneDirection[i].labels[j].currentColor = Integer.parseInt(parsed[counter]);
				counter++;
				Canvas.weavePolesObstacles[index].oneDirection[i].labels[j].visible = Boolean.parseBoolean(parsed[counter]);
				counter++;
			}
		}
		Canvas.weavePolesObstacles[index].setVisible(true);
		Canvas.weavePolesObstacles[index].setParameters();
		Base.canvas.repaint();
	}
} 
class MyMenuItem extends JMenuItem{
	public static Dimension preferredSize = new Dimension(150,30);
	public MyMenuItem(String name){
		this.setText(name);
		this.setPreferredSize(preferredSize);
		this.setBackground(new Color(250,250,250,150));
		//this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
	}
}
class MyPopupMenu extends JPopupMenu{
	public MyPopupMenu()
	{
		this.setBackground(new Color(250,250,250,150));
	}
}