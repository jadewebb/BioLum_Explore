/**
 * @author Jade Webb
 * @version 1.0
 * @date May 2023
 * @email jade.webb@sjsu.edu
 */


import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

public class Frame extends JFrame{
	JButton viewButton;							//button to control the current view
	JButton alignmentButton;					//button to activate alignment mode
	JButton infoButton;							//button to activate info mode
	JButton paramButton;						//button to set PWA and reading frame parameters
	JButton dnaButton;							//button to activate DNA mode
	JButton rnaButton;							//button to activate RNA mode
	JButton proteinButton;						//button to activate protein mode
	viewEnum view;								//tracks whether photo view or species view is active
	modeEnum mode;								//tracks which mode is active
	LinkedHashMap<JLabel, String> selected;		//stores species that are currently selected
	JPanel mainPanel;							//panel to display the grid of species
	
	int match;									//match reward
	int mismatch;								//mismatch penalty
	int indel;									//indel penalty
	int readingFrame;							//reading frame for translation
	String a1Global;							//the global alignment string for sequence 1
	String a2Global;							//the global alignment string for sequence 2
	String a1Local;								//the local alignment string for sequence 1
	String a2Local;								//the local alignment string for sequence 2
	int[][] gridGlobal;							//grid used for Needleman-Wunsch algorithm for global PWA
	int[][] gridLocal;							//grid used for Needleman-Wunsch algorithm for local PWA
	
	enum viewEnum {					//represents text view or photo view
		TEXT,
		PHOTO;
	}
	
	enum modeEnum {					//represents different possible modes
		INFO,
		ALIGN,
		DNA,
		RNA,
		PROTEIN;
	}
	
	/**
	 * Initializes the frame with the following components:
	 	* Row of buttons
	 	* Grid of species, in text or photo view
	 */
	public Frame() {
		this.setSize(1900, 800);									//default window dimensions
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);				//window is full-screen	upon app launch																			
		selected = new LinkedHashMap<JLabel, String>();				//initialize map to store species that are currently selected
		view = viewEnum.TEXT;										//text view is the default view that is shown upon app launch
		match = 1;													//default match is set to 1
		mismatch = -1;												//default mismatch is set to -1
		indel = -2;													//default indel is set to -2
		readingFrame = 1;											//default reading frame is set to 1
		
		//button 1
		viewButton = new JButton("Photo View");						//button triggers the view to change to the view not currently selected
		viewButton.setPreferredSize(new Dimension(150, 80));		
		viewButton.setFont(new Font("DejaVu Sans", 0, 20));
		viewButton.addActionListener(createViewButtonListener());	//listener detects when the button is clicked, and performs actions to change the view
		viewButton.setBackground(new Color (213, 233, 245));
		
		//button 2
		infoButton = new JButton("Species Info");					//button triggers info mode
		infoButton.setPreferredSize(new Dimension(150, 80));
		infoButton.setFont(new Font("DejaVu Sans", 0, 20));
		infoButton.addActionListener(createInfoButtonListener());				//listener detects when the button is clicked, and changes the mode
		infoButton.setToolTipText("Click on a species to view its info");	
		infoButton.setBackground(new Color (213, 233, 245));
		
		//button 3
		dnaButton = new JButton("DNA");								//button triggers DNA mode
		dnaButton.setPreferredSize(new Dimension(90, 80));
		dnaButton.setFont(new Font("DejaVu Sans", 0, 20));
		dnaButton.addActionListener(createDNAButtonListener());		//listener detects when the button is clicked, and changes the mode
		dnaButton.setToolTipText("Get DNA Sequence");
		dnaButton.setBackground(new Color (213, 233, 245));
		
		//button 4
		rnaButton = new JButton("RNA");								//button triggers RNA mode
		rnaButton.setPreferredSize(new Dimension(90, 80));
		rnaButton.setFont(new Font("DejaVu Sans", 0, 20));
		rnaButton.addActionListener(createRNAButtonListener());		//listener detects when the button is clicked, and changes the mode
		rnaButton.setToolTipText("Get RNA Transcription");
		rnaButton.setBackground(new Color (213, 233, 245));
		
		//button 5
		proteinButton = new JButton("Protein");								//button triggers protein mode
		proteinButton.setPreferredSize(new Dimension(110, 80));
		proteinButton.setFont(new Font("DejaVu Sans", 0, 20));
		proteinButton.addActionListener(createProteinButtonListener());		//listener detects when the button is clicked, and changes the mode
		proteinButton.setToolTipText("Get Protein Translation");
		proteinButton.setBackground(new Color (213, 233, 245));
		
		//button 6
		alignmentButton = new JButton("Pairwise Alignment");					//button triggers pairwise alignment mode
		alignmentButton.setPreferredSize(new Dimension(215, 80));
		alignmentButton.setFont(new Font("DejaVu Sans", 0, 20));
		alignmentButton.addActionListener(createAlignmentButtonListener());							//listener detects when the button is clicked, and changes the mode
		alignmentButton.setToolTipText("Click on two species to perform pairwise alignment");
		alignmentButton.setBackground(new Color (213, 233, 245));
				
		//button 7
		paramButton = new JButton("Set Parameters");					//button triggers a popup allowing user to set parameters
		paramButton.setPreferredSize(new Dimension(190, 80));
		paramButton.setFont(new Font("DejaVu Sans", 0, 20));
		paramButton.addActionListener(createParamButtonListener());		//listener detects when the button is clicked, and triggers the creation of a message dialog box to take user input
		paramButton.setToolTipText("Set parameters");
		paramButton.setBackground(new Color (213, 233, 245));		
		
		//panel of buttons
		JPanel buttonsPanel = new JPanel();				
		buttonsPanel.add(viewButton);
		buttonsPanel.add(infoButton);
		buttonsPanel.add(dnaButton);
		buttonsPanel.add(rnaButton);
		buttonsPanel.add(proteinButton);
		buttonsPanel.add(alignmentButton);
		buttonsPanel.add(paramButton);
		
		//add panel of buttons to the top of the frame
		this.setLayout(new BorderLayout());				
		this.add(buttonsPanel, BorderLayout.NORTH);
		
		//Generate default text view that is shown upon app launch
		generateView();
		viewButton.setToolTipText("Change to Photo View");
	}
	
	/**
	 * Creates an ActionListener that changes the view to the view that is not currently selected
	 * @return an ActionListener
	 */
	public ActionListener createViewButtonListener () {
		return event -> {
			    if (viewButton.getText().equals("Text View")) {
			    	viewButton.setText("Photo View");
			    	viewButton.setToolTipText("Change to Photo View");
			    	view = viewEnum.TEXT;									//changes the view to TEXT view
			    	generateView();											//re-generates the grid of species
			    } else {
			    	viewButton.setText("Text View");
			    	viewButton.setToolTipText("Change to Text View");
			    	view = viewEnum.PHOTO;									//changes the view to PHOTO view
			    	generateView();											//re-generates the grid of species
			    }
				};
	}
	
	/**
	 * Creates an ActionListener that changes the mode to INFO mode and clears any currently selected species
	 * @return an ActionListener
	 */
	public ActionListener createInfoButtonListener () {
		return event -> {
			    mode = modeEnum.INFO;										//changes the mode to INFO mode
			    infoButton.setBackground(new Color (75, 171, 227));			//changes the button color to indicate the button is selected
			    dnaButton.setBackground(new Color (213, 233, 245));
			    rnaButton.setBackground(new Color (213, 233, 245));
			    proteinButton.setBackground(new Color (213, 233, 245));
			    alignmentButton.setBackground(new Color (213, 233, 245));
			    selected.clear();											//reset current selection to none
				};
	}
	
	/**
	 * Creates an ActionListener that changes the mode to DNA mode and clears any currently selected species
	 * @return an ActionListener
	 */
	public ActionListener createDNAButtonListener () {
		return event -> {
			    mode = modeEnum.DNA;										//changes the mode to DNA mode
			    infoButton.setBackground(new Color (213, 233, 245));		
			    dnaButton.setBackground(new Color (75, 171, 227));			//changes the button color to indicate the button is selected
			    rnaButton.setBackground(new Color (213, 233, 245));
			    proteinButton.setBackground(new Color (213, 233, 245));
			    alignmentButton.setBackground(new Color (213, 233, 245));
			    selected.clear();											//reset current selection to none
				};
	}
	
	/**
	 * Creates an ActionListener that changes the mode to RNA mode and clears any currently selected species
	 * @return an ActionListener
	 */
	public ActionListener createRNAButtonListener () {
		return event -> {
			    mode = modeEnum.RNA;										//changes the mode to RNA mode
			    infoButton.setBackground(new Color (213, 233, 245));
			    dnaButton.setBackground(new Color (213, 233, 245));
			    rnaButton.setBackground(new Color (75, 171, 227));			//changes the button color to indicate the button is selected
			    proteinButton.setBackground(new Color (213, 233, 245));
			    alignmentButton.setBackground(new Color (213, 233, 245));
			    selected.clear();											//reset current selection to none
				};
	}
	
	/**
	 * Creates an ActionListener that changes the mode to PROTEIN mode and clears any currently selected species
	 * @return an ActionListener
	 */
	public ActionListener createProteinButtonListener () {
		return event -> {
			    mode = modeEnum.PROTEIN;									//changes the mode to PROTEIN mode
			    infoButton.setBackground(new Color (213, 233, 245));
			    dnaButton.setBackground(new Color (213, 233, 245));
			    rnaButton.setBackground(new Color (213, 233, 245));	
			    proteinButton.setBackground(new Color (75, 171, 227));		//changes the button color to indicate the button is selected
			    alignmentButton.setBackground(new Color (213, 233, 245));
			    selected.clear();											//reset current selection to none
				};
	}
	
	/**
	 * Creates an ActionListener that changes the mode to ALIGN mode and clears any currently selected species
	 * @return an ActionListener
	 */
	public ActionListener createAlignmentButtonListener () {
		return event -> {
			    mode = modeEnum.ALIGN;										//changes the mode to ALIGN mode
			    infoButton.setBackground(new Color (213, 233, 245));
			    dnaButton.setBackground(new Color (213, 233, 245));
			    rnaButton.setBackground(new Color (213, 233, 245));
			    proteinButton.setBackground(new Color (213, 233, 245));
			    alignmentButton.setBackground(new Color (75, 171, 227));	//changes the button color to indicate the button is selected
			    selected.clear();											//reset current selection to none
				};
	}
	
	/**
	 * Creates an ActionListener that triggers a popup allowing user to set parameters
	 * @return an ActionListener
	 */
	public ActionListener createParamButtonListener () {
		return event -> {
			    params();					
				};
	}
	
	/**
	 * Displays a popup that prompts the user to set the match, mismatch, indel, and reading frame parameters
	 */
	public void params() {
		
		//inner panel that holds the four parameter fields
		JPanel innerPanel = new JPanel(new GridLayout(4, 1));				
		innerPanel.setBorder(new EmptyBorder(20, 20, 5, 20));
		
		//text field to take user input for match
		JLabel matchLabel = new JLabel("Match reward");
		matchLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		JTextField matchTextField = new JTextField();								
		matchTextField.setText("" + match);
		matchTextField.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		matchTextField.setBorder(BorderFactory.createCompoundBorder(matchTextField.getBorder(), 
				BorderFactory.createEmptyBorder(0, 5, 0, 0)));
		
		//text field to take user input for mismatch
		JLabel mismatchLabel = new JLabel("Mismatch penalty");
		mismatchLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		JTextField mismatchTextField = new JTextField();							
		mismatchTextField.setText("" + mismatch);
		mismatchTextField.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		mismatchTextField.setBorder(BorderFactory.createCompoundBorder(mismatchTextField.getBorder(), 
				BorderFactory.createEmptyBorder(0, 5, 0, 0)));
		
		//text field to take user input for indel
		JLabel indelLabel = new JLabel("Indel penalty");
		indelLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		JTextField indelTextField = new JTextField();								
		indelTextField.setText("" + indel);
		indelTextField.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		indelTextField.setBorder(BorderFactory.createCompoundBorder(indelTextField.getBorder(), 
				BorderFactory.createEmptyBorder(0, 5, 0, 0)));
		
		//combo box to display choices for reading frame
		JLabel frameLabel = new JLabel("Reading frame");
		frameLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		String[] options = {"1", "2", "3", "4", "5", "6"};
		JComboBox frameComboBox = new JComboBox(options);							
		frameComboBox.setSelectedItem(((Integer)readingFrame).toString());
		frameComboBox.setEditable(true);
		frameComboBox.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		
		//panel of parameter labels and fields
		innerPanel.add(matchLabel);
		innerPanel.add(matchTextField);
		innerPanel.add(mismatchLabel);
		innerPanel.add(mismatchTextField);
		innerPanel.add(indelLabel);
		innerPanel.add(indelTextField);
		innerPanel.add(frameLabel);
		innerPanel.add(frameComboBox);
		
		JLabel promptText = new JLabel("Set parameters for pairwise alignment");
		promptText.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
		promptText.setHorizontalAlignment(JLabel.CENTER);
		
		//main panel for popup
		JPanel mainPanel = new JPanel(new BorderLayout());				
		mainPanel.setPreferredSize(new Dimension(400, 180));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(promptText, BorderLayout.NORTH);

		//add inner panel to main panel
		mainPanel.add(innerPanel, BorderLayout.CENTER);
		
		//create popup dialog box
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
		JOptionPane.showMessageDialog(this, mainPanel, "Parameters", JOptionPane.PLAIN_MESSAGE, null);
		
		//get user input and assign value to corresponding variable
		match = Integer.parseInt(matchTextField.getText());
		mismatch = Integer.parseInt(mismatchTextField.getText());
		indel = Integer.parseInt(indelTextField.getText());
		readingFrame = Integer.parseInt((String) frameComboBox.getSelectedItem());
	}
	
	/**
	 * Generate text view or photo view of species, handle actions to be performed when a species is selected
	 */
	public void generateView() {
		
		//re-create the main panel every time the view is changed
		mainPanel = new JPanel(new GridLayout (3,10));								
		mainPanel.setBorder(BorderFactory.createEmptyBorder(80,80,80,80));
		
		//get directory where the species images are stored
		File dir = new File("src/Images");					
		File[] images = dir.listFiles();
		
		//iterate through every image file in the images directory 
		for (File i : images) {								
			
			//each species is contained within a label
			JLabel l;		

			//display TEXT view
			if (view.equals(viewEnum.TEXT)) {
				
				//extract the genus and species from the image name
				String genus = i.getName().substring(0, i.getName().indexOf(' '));
				int period = i.getName().indexOf('.');
				String species = i.getName().substring(i.getName().indexOf(' ') + 1, period);
				
				//display the species text
				l = new JLabel("<html>" + genus + "<br>" + species + "</html>");
				l.setFont(new Font("DejaVu Sans", Font.ITALIC, 25));
				l.setIcon(null);
				
			//display PHOTO view	
			} else {
				
				//get the image icon 
				ImageIcon image = new ImageIcon("src/Images/" + i.getName());
				
				//display the image icon
				l = new JLabel();
				l.setIcon(image);
				l.setText(null);
			}
			
			l.setHorizontalAlignment(SwingConstants.CENTER);
			
			//listener performs actions when a species label is clicked
			l.addMouseListener(new MouseAdapter() {
				
				public void mouseClicked(MouseEvent e) {
					
					//re-generate text view or photo view of species
					if (view.equals(viewEnum.TEXT)) {
						String genus = i.getName().substring(0, i.getName().indexOf(' '));
						int period = i.getName().indexOf('.');
						String species = i.getName().substring(i.getName().indexOf(' ') + 1, period);
						l.setText("<html>" + genus + "<br>" + species + "</html>");
						l.setIcon(null);
					} else {
						ImageIcon image = new ImageIcon("src/Images/" + i.getName());
						l.setIcon(image);
						l.setText(null);
					}
					
					//if the species is already selected, deselect it
					if(selected.containsKey(l)) {
						l.setBackground(new Color(238,238,238));		//reset color to indicate it is not selected
						selected.remove(l);
						
					//if the species is not already selected, select it and perform actions	
					} else {
						
						//if no mode is selected, do nothing
						if (mode == null) {									
							return;
						
						//if a mode is selected, select the species
						} else {
							
							//set color to indicate it is selected
							l.setOpaque(true);
							l.setBackground(new Color (200, 200, 200));
							selected.put(l, i.getName());	
							
							//if the mode is INFO mode, invoke the info method
							if (mode.equals(modeEnum.INFO)) {
								try {
									info();
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							
							//if the mode is ALIGN mode, invoke the alignment method
							} else if (mode.equals(modeEnum.ALIGN)){
								
								//only invoke the alignment method if 2 sequences are selected
								if (selected.size() == 2) {
									try {
										alignment();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							
							//if the mode is DNA mode, invoke the dna method
							} else if (mode.equals(modeEnum.DNA)){
								try {
									dna();
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								
							//if the mode is RNA mode, invoke the rna method
							} else if (mode.equals(modeEnum.RNA)){
								try {
									rna();
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							
							//if the mode is PROTEIN mode, invoke the protein method
							} else if ((mode.equals(modeEnum.PROTEIN))){
								try {
									protein();
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
				
			});
			
			//re-add the species to the grid
			mainPanel.add(l);
		}
		
		//re-add the main panel to the frame
		this.add(mainPanel);
	}
	
	/**
	 * Display info for the currently selected species
	 * @throws IOException 
	 */
	public void info() throws IOException {
		
		//get the currently selected species
		Map.Entry<JLabel, String> species = selected.entrySet().iterator().next();
		
		//get the image for the currently selected species
		ImageIcon image = new ImageIcon(new ImageIcon("src/Images/" + species.getValue()).getImage().getScaledInstance(190,  190, Image.SCALE_SMOOTH));

		//get the species name for the currently selected species
		int period = species.getValue().indexOf('.');
		String speciesName = species.getValue().substring(0, period);
		
		//parse through the info file and extract info for the selected species
		File info = new File("src/Info/Info.txt");
		BufferedReader br = new BufferedReader(new FileReader(info));
		String location = "";
		String discovery = "";
		String size = "";
		String color = "";
		String commonName = "";
		try {
			String currentLine = br.readLine();
			boolean locatedSpecies = false;
			
			//while the species has not been encountered, keep parsing
			while (!locatedSpecies && currentLine != null) {
				
				//if the species is found, get the info fields for that species
				if (currentLine.equals(">" + speciesName)) {
					location = br.readLine();
					discovery = br.readLine();
					size = br.readLine();
					color = br.readLine();
					commonName = br.readLine();
					locatedSpecies = true;
				}
				currentLine = br.readLine();
			}
		} finally {
			br.close();
		}
		
		JLabel text = new JLabel("<html><i>" + speciesName + "</i><br>" + location + "</br><br>" 
				+ discovery + "</br><br>" + size + "</br><br>" + color + "</br><br>" + commonName + "</br></html>");
		text.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
		
		//create popup dialog box to display the species info
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
		JOptionPane.showMessageDialog(this, text, "Species Info", JOptionPane.INFORMATION_MESSAGE, image);
		
		//deselect species
		for(JLabel label : selected.keySet()) {
			label.setBackground(new Color(238,238,238));
		}
		selected.clear();
	}
	
	/**
	 * Save dna sequence for the currently selected species
	 * @throws IOException 
	 * @return dna sequence string
	 */
	public String dna() throws IOException {
		
		//get the currently selected species
		Map.Entry<JLabel, String> species = selected.entrySet().iterator().next();

		//get the species name for the currently selected species
		int period = species.getValue().indexOf('.');
		String speciesName = species.getValue().substring(0, period);
		
		//parse through the sequences file and extract sequence for the selected species
		File info = new File("src/Info/Sequences.txt");
		BufferedReader br = new BufferedReader(new FileReader(info));
		String sequence = "";
		try {
			String currentLine = br.readLine();
			boolean locatedSpecies = false;
			
			//while the species has not been encountered, keep parsing
			while (!locatedSpecies && currentLine != null) {
				
				//if the species is found, get the sequence lines for that species
				if (currentLine.equals(">" + speciesName)) {
					String currentLineInner = br.readLine();
					boolean done = false;
					
					//keep getting sequence lines for the species until the sequence is done
					while (!done && currentLineInner != null) {
						if (!currentLineInner.equals("")) {
							sequence = sequence + currentLineInner + " ";
							currentLineInner = br.readLine();
						} else {
							done = true;
						}
					}
					locatedSpecies = true;
				}
				currentLine = br.readLine();
			}
		} finally {
			br.close();
		}

		//remove any spaces or non-ATGC characters from the sequence
		sequence = sequence.replaceAll("\\s", "");
		sequence = sequence.replaceAll("[^ATGC]", "");
		
		//if the mode is DNA mode, write the dna sequence to a file if it does not already exist
		if (mode.equals(modeEnum.DNA)){
			
			//create a new file for the dna sequence if it does not exist
			String fileNameDNA = "src/Files/" + "DNA " + speciesName + ".txt";
			File dnaFile = new File(fileNameDNA);
			boolean canCreateNewFile = dnaFile.createNewFile();

			JLabel text = null;
			
			//if the file is new, write to the file
			if (canCreateNewFile) {
				
				//write the dna sequence to the file
				FileWriter w = new FileWriter(fileNameDNA);
				int k;
				for(k = 0; k < sequence.length() - 70; k+=70) {
					w.write(sequence.substring(k, k + 70));
					w.write("\n");
				}
				w.write(sequence.substring(k));
				w.close();
				
				text = new JLabel("DNA file written successfully");
			
			//if the file already exists, do not write	
			} else {
				text = new JLabel("DNA file already exists");
			}
			
			text.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
			text.setHorizontalAlignment(SwingConstants.CENTER);
		
			//create popup dialog box to indicate whether the file was written
			JDialog.setDefaultLookAndFeelDecorated(true);
			UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
			JOptionPane.showMessageDialog(this, text, "DNA", JOptionPane.PLAIN_MESSAGE, null);
		
			//deselect species
			for(JLabel label : selected.keySet()) {
				label.setBackground(new Color(238,238,238));
			}
			selected.clear();
			
		//if the mode is not DNA mode, the sequence is being used to get an rna or protein sequence	
		} else {

			//if the reading frame is 4, 5, or 6, reverse the sequence
			if (readingFrame > 3) {
				sequence = new StringBuilder(sequence).reverse().toString();
			}
		}
		
		return sequence;
	}
	
	/**
	 * Save rna sequence for the currently selected species
	 * @throws IOException 
	 * @return rna sequence string
	 */
	public String rna() throws IOException {
		
		//get the currently selected species
		Map.Entry<JLabel, String> species = selected.entrySet().iterator().next();

		//get the species name for the currently selected species
		int period = species.getValue().indexOf('.');
		String speciesName = species.getValue().substring(0, period);
		
		String sequence = "";
		String rna = "";
		
		//get the dna sequence for the species
		sequence = dna();
		
		//replace every T with U in the dna sequence to get the rna sequence
		for(int i = 0; i < sequence.length(); i++) {
			if (sequence.charAt(i) == 'T') {
				rna = rna + 'U';
			} else {
				rna = rna + sequence.charAt(i);
			}
		}
		
		//if the mode is RNA mode, write the rna sequence to a file if it does not already exist
		if (mode.equals(modeEnum.RNA)){
			
			//create a new file for the rna sequence if it does not exist
			String fileNameRNA = "src/Files/" + "RNA " + speciesName + ".txt";
			File rnaFile = new File(fileNameRNA);
			boolean canCreateNewFile = rnaFile.createNewFile();
			
			JLabel text = null;
			
			//if the file is new, write to the file
			if (canCreateNewFile) {
				
				//write the rna sequence to the file
				FileWriter w = new FileWriter(fileNameRNA);
				int k;
				for(k = 0; k < rna.length() - 70; k+=70) {
					w.write(rna.substring(k, k + 70));
					w.write("\n");
				}
				w.write(rna.substring(k));
				w.close();
				
				text = new JLabel("RNA file written successfully");
			
			//if the file already exists, do not write	
			} else {
				text = new JLabel("RNA file already exists");
			}
			
			text.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
			text.setHorizontalAlignment(SwingConstants.CENTER);
		
			//create popup dialog box to indicate whether the file was written
			JDialog.setDefaultLookAndFeelDecorated(true);
			UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
			JOptionPane.showMessageDialog(this, text, "RNA", JOptionPane.PLAIN_MESSAGE, null);
			
			//deselect species
			for(JLabel label : selected.keySet()) {
				label.setBackground(new Color(238,238,238));
			}
			selected.clear();
			
		//if the mode is not RNA mode, the sequence is being used to get a protein sequence		
		} else {
			
			//if the reading frame is 4, 5, or 6, find the complement
			if (readingFrame > 3) {
				rna = "";
				for(int i = 0; i < sequence.length(); i++) {
					if (sequence.charAt(i) == 'A') {
						rna = rna + 'U';
					} else if (sequence.charAt(i) == 'T') {
						rna = rna + 'A';
					} else if (sequence.charAt(i) == 'C') {
						rna = rna + 'G';
					} else {
						rna = rna + 'C';
					}
				}
			}
		}
		
		return rna;
	}
	
	/**
	 * Save protein sequence for the currently selected species
	 * @throws IOException 
	 */
	public void protein() throws IOException {
		
		//get the currently selected species
		Map.Entry<JLabel, String> species = selected.entrySet().iterator().next();

		//get the species name for the currently selected species
		int period = species.getValue().indexOf('.');
		String speciesName = species.getValue().substring(0, period);
		
		String rna = "";
		String protein = "";
		String currentCodon = "";
		
		//get the rna sequence for the species
		rna = rna();

		//set the index based on the reading frame
		int i = -1;
		if (readingFrame < 4) {
			i = readingFrame - 1;
		} else {
			i = readingFrame - 4;
		}
		
		boolean done = false;
		
		//parse through the rna sequence to extract codons and select the corresponding amino acid
		while(!done) {
			
			//get the current codon
			if (i < rna.length() - 3) {
				currentCodon = rna.substring(i, i + 3);
			} else {
				currentCodon = rna.substring(i);
				done = true;
			}
			
			//test to see which protein is encoded by the codon
			if (currentCodon.equals("UUU") || currentCodon.equals("UUC")) {
				protein = protein + 'F';
			} else if (currentCodon.equals("UUA") || currentCodon.equals("UUG")
					 || currentCodon.equals("CUU") || currentCodon.equals("CUC")
					 || currentCodon.equals("CUA") || currentCodon.equals("CUG")) {
				protein = protein + 'L';
			} else if (currentCodon.equals("UCU") || currentCodon.equals("UCC")
					 || currentCodon.equals("UCA") || currentCodon.equals("UCG")
					 || currentCodon.equals("AGU") || currentCodon.equals("AGC")) {
				protein = protein + 'S';
			} else if (currentCodon.equals("UAU") || currentCodon.equals("UAC")) {
				protein = protein + 'Y';
			} else if (currentCodon.equals("UGU") || currentCodon.equals("UGC")) {
				protein = protein + 'C';
			} else if (currentCodon.equals("UGG")) {
				protein = protein + 'W';
			} else if (currentCodon.equals("CCU") || currentCodon.equals("CCC")
					 || currentCodon.equals("CCA") || currentCodon.equals("CCG")
					 || currentCodon.equals("CC")) {
				protein = protein + 'P';
			} else if (currentCodon.equals("CAU") || currentCodon.equals("CAC")) {
				protein = protein + 'H';
			} else if (currentCodon.equals("CAA") || currentCodon.equals("CAG")) {
				protein = protein + 'Q';
			} else if (currentCodon.equals("CGU") || currentCodon.equals("CGC")
					 || currentCodon.equals("CGA") || currentCodon.equals("CGG")
					 || currentCodon.equals("AGA") || currentCodon.equals("AGG")) {
				protein = protein + 'R';
			} else if (currentCodon.equals("AUU") || currentCodon.equals("AUC")
					 || currentCodon.equals("AUA")) {
				protein = protein + 'I';
			} else if (currentCodon.equals("AUG")) {
				protein = protein + 'M';
			} else if (currentCodon.equals("ACU") || currentCodon.equals("ACC")
					 || currentCodon.equals("ACA") || currentCodon.equals("ACG")
					 || currentCodon.equals("AC")) {
				protein = protein + 'T';
			} else if (currentCodon.equals("AAU") || currentCodon.equals("AAC")) {
				protein = protein + 'N';
			} else if (currentCodon.equals("AAA") || currentCodon.equals("AAG")) {
				protein = protein + 'K';
			} else if (currentCodon.equals("GUU") || currentCodon.equals("GUC")
					 || currentCodon.equals("GUA") || currentCodon.equals("GUG")
					 || currentCodon.equals("GU")) {
				protein = protein + 'V';
			} else if (currentCodon.equals("GCU") || currentCodon.equals("GCC")
					 || currentCodon.equals("GCA") || currentCodon.equals("GCG")
					 || currentCodon.equals("GC")) {
				protein = protein + 'A';
			} else if (currentCodon.equals("GAU") || currentCodon.equals("GAC")) {
				protein = protein + 'D';
			} else if (currentCodon.equals("GAA") || currentCodon.equals("GAG")) {
				protein = protein + 'E';
			} else if (currentCodon.equals("GGU") || currentCodon.equals("GGC")
					 || currentCodon.equals("GGA") || currentCodon.equals("GGG")
					 || currentCodon.equals("GG")) {
				protein = protein + 'G';

			//stop codon	
			} else if (currentCodon.equals("UAA") || currentCodon.equals("UAG")
					 || currentCodon.equals("UGA")) {
				protein = protein + '-';
			}
			i+=3;
		}
		
		//create a new file for the protein sequence if it does not exist
		String fileNameProtein = "src/Files/" + "Protein " + readingFrame + " " + speciesName + ".txt";
		File proteinFile = new File(fileNameProtein);
		boolean canCreateNewFile = proteinFile.createNewFile();
		
		JLabel text = null;
		
		//write the protein sequence to a file if it does not already exist
		if (canCreateNewFile) {
			
			//write the protein sequence to the file
			FileWriter w = new FileWriter(fileNameProtein);
			int k;
			for(k = 0; k < protein.length() - 70; k+=70) {
				w.write(protein.substring(k, k + 70));
				w.write("\n");
			}
			w.write(protein.substring(k));
			w.close();
			
			text = new JLabel("Protein file written successfully");
		
		//if the file already exists, do not write	
		} else {
			text = new JLabel("Protein file already exists");
		}
		
		text.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
		text.setHorizontalAlignment(SwingConstants.CENTER);
		
		//create popup dialog box to indicate whether the file was written
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
		JOptionPane.showMessageDialog(this, text, "Protein", JOptionPane.PLAIN_MESSAGE, null);
		
		//deselect species
		for(JLabel label : selected.keySet()) {
			label.setBackground(new Color(238,238,238));
		}
		selected.clear();
	}
	
	/**
	 * Save global and local pairwise alignment for the currently selected species
	 * @throws IOException 
	 */
	public void alignment() throws IOException {
		
		//use an iterator to get both selected sequences
		Iterator<Entry<JLabel, String>> iter = selected.entrySet().iterator();
		
		//get the first currently selected species
		Map.Entry<JLabel, String> species1 = iter.next();
		BufferedImage image1 = ImageIO.read(new File("src/Images/" + species1.getValue()));
		
		//get the second currently selected species
		Map.Entry<JLabel, String> species2 = iter.next();
		BufferedImage image2 = ImageIO.read(new File("src/Images/" + species2.getValue()));
		
		//get the images for the currently selected species
		BufferedImage mergedImage = new BufferedImage(image1.getWidth() + image2.getWidth() + 15, image1.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = mergedImage.createGraphics();
		g.drawImage(image1, 0, 0, null);
		g.drawImage(image2, image1.getWidth() + 15, 0, null);
		g.dispose();
		ImageIcon image = new ImageIcon(mergedImage);
		
		//get the species name for the first selected species
		int period1 = species1.getValue().indexOf('.');
		String speciesName1 = species1.getValue().substring(0, period1);
		
		//get the species name for the second selected species
		int period2 = species2.getValue().indexOf('.');
		String speciesName2 = species2.getValue().substring(0, period2);
		
		//parse through the sequences file and extract the sequences for the selected species
		File info = new File("src/Info/Sequences.txt");
		BufferedReader br = new BufferedReader(new FileReader(info));
		String sequence1 = "";
		String sequence2 = "";
		try {
			String currentLine = br.readLine();
			boolean locatedSpecies1 = false;
			boolean locatedSpecies2 = false;
			
			//while the species have not been encountered, keep parsing
			while ((!locatedSpecies1 || !locatedSpecies2) && currentLine != null) {
				
				//if the first species is found, get the sequence lines for that species
				if (currentLine.equals(">" + speciesName1)) {
					String currentLineInner = br.readLine();
					boolean done = false;
					
					//keep getting sequence lines for the first species until the sequence is done
					while (!done && currentLineInner != null) {
						if (!currentLineInner.equals("")) {
							sequence1 = sequence1 + currentLineInner + " ";
							currentLineInner = br.readLine();
						} else {
							done = true;
						}
					}
					locatedSpecies1 = true;
				}
				
				//if the second species is found, get the sequence lines for that species
				if (currentLine.equals(">" + speciesName2)) {
					String currentLineInner = br.readLine();
					boolean done = false;
					
					//keep getting sequence lines for the second species until the sequence is done
					while (!done && currentLineInner != null) {
						if (!currentLineInner.equals("")) {
							sequence2 = sequence2 + currentLineInner + " ";
							currentLineInner = br.readLine();
						} else {
							done = true;
						}
						
					}
					locatedSpecies2 = true;
				}
				currentLine = br.readLine();
			}
		} finally {
			br.close();
		}
		
		//remove any spaces or non-ATGC characters from the first sequence
		sequence1 = sequence1.replaceAll("\\s", "");
		sequence1 = sequence1.replaceAll("[^ATGC]", "");
		
		//remove any spaces or non-ATGC characters from the first sequence
		sequence2 = sequence2.replaceAll("\\s", "");
		sequence2 = sequence2.replaceAll("[^ATGC]", "");
		
		//create a new file for the global alignment if it does not exist
		String fileNameGlobal = "src/Files/" + "Global Alignment " + match + " " + mismatch + " " + indel + " " + speciesName1 + " and " + speciesName2 + ".txt";
		File globalFile = new File(fileNameGlobal);
		boolean canCreateNewFileG = globalFile.createNewFile();
		
		//create a new file for the local alignment if it does not exist
		String fileNameLocal = "src/Files/" + "Local Alignment " + match + " " + mismatch + " " + indel + " " + speciesName1 + " and " + speciesName2 + ".txt";
		File localFile = new File(fileNameLocal);
		boolean canCreateNewFileL = localFile.createNewFile();
		
		//get global and local alignment results, write alignment to files if they do not already exist
		int[] alignmentResult = pairwise(sequence1, sequence2, fileNameGlobal, fileNameLocal, canCreateNewFileG, canCreateNewFileL);
		
		String alignmentWrittenG = null;
		String alignmentWrittenL = null;
		
		//alter popup text depending on if the alignment files were written
		if (canCreateNewFileG) {
			alignmentWrittenG = "Global alignment file written successfully";
		} else {
			alignmentWrittenG = "Global alignment file already exists";
		}
		
		if (canCreateNewFileL) {
			alignmentWrittenL = "Local alignment file written successfully";
		} else {
			alignmentWrittenL = "Local alignment file already exists";
		}
		
		JLabel text = new JLabel("<html>Pairwise alignment between <i>" + speciesName1 
				+ "</i> (left) and <i>" + speciesName2 + "</i> (right)<br>Algorithm: Needleman-Wunsch</br>"
				+ "<br>Global Alignment Score: " + alignmentResult[0]
				+ "</br><br>Local Alignment Score: " + alignmentResult[1] + "</br><br>" + alignmentWrittenG 
				+ "</br><br>" + alignmentWrittenL + "</br><html>");
		text.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
		
		//create popup dialog box to display alignment results and indicate whether the files were written
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put("InternalFrame.titleFont", new FontUIResource("DejaVu Sans", Font.PLAIN, 20));
		JOptionPane.showMessageDialog(this, text, "Pairwise Alignment", JOptionPane.INFORMATION_MESSAGE, image);
		
		//deselect species
		for(JLabel label : selected.keySet()) {
			label.setBackground(new Color(238,238,238));
		}		
		selected.clear();
	}
	
	/**
	 * Perform global and local pairwise alignment for two species, write alignments to files
	 * @throws IOException 
	 * @return int array containing global and local alignment scores
	 */
	public int[] pairwise(String s1, String s2, String fileNameGlobal, String fileNameLocal, boolean canCreateNewFileG, boolean canCreateNewFileL) throws IOException {
		int[] scores = new int[2];
		
		//get the global and local alignment scores
		scores[0] = globalAlignment(s1, s2);
		scores[1] = localAlignment(s1,s2);
		
		//write the alignments to files if they do not already exist
		if (canCreateNewFileG) {
			saveGlobalAlignment(s1, s2, fileNameGlobal);
		}
		if (canCreateNewFileL) {
			saveLocalAlignment(s1, s2, fileNameLocal);
		}
		return scores;
	}
	
	/**
	 * Perform global pairwise alignment for two species using Needleman-Wunsch algorithm
	 * @throws IOException 
	 * @return global alignment score
	 */
	public int globalAlignment(String s1, String s2) {
		
		//initialize first column
		gridGlobal = new int[s1.length()+1][s2.length()+1];
		int n = 0;
		for (int i = 0; i < gridGlobal[0].length; i++) {
			gridGlobal[0][i] = n;
			n+=indel;
		}

		//initialize first row
		n = 0;
		for (int i = 0; i < gridGlobal.length; i++) {
			gridGlobal[i][0] = n;
			n+=indel;
		}
		
		int north;
		int west;
		int northwest;
		
		//fill out the global alignment grid
		for (int i = 1; i < gridGlobal.length; i++) {
			for (int j = 1; j < gridGlobal[0].length; j++) {
				
				//north cell plus indel penalty
				north = gridGlobal[i][j-1] + indel;
				
				//west cell plus indel penalty
				west = gridGlobal[i-1][j] + indel;
				
				//northwest cell plus match or mismatch
				if (s1.charAt(i-1)==s2.charAt(j-1)) {
					northwest = gridGlobal[i-1][j-1] + match;
				} else {
					northwest = gridGlobal[i-1][j-1] + mismatch;
				}
				
				//assign current cell to maximum between the north, west, and northwest values
				gridGlobal[i][j] = Math.max(Math.max(north, west), northwest);
			}
		}
		
		//get the alignment score (bottom right corner of grid)
		return gridGlobal[gridGlobal.length-1][gridGlobal[0].length-1];
	}
	
	/**
	 * Perform local pairwise alignment for two species using Needleman-Wunsch algorithm
	 * @throws IOException 
	 * @return local alignment score
	 */
	public int localAlignment(String s1, String s2) {
		
		//initialize first column with zeros
		gridLocal = new int[s1.length()+1][s2.length()+1];
		for (int i = 0; i < gridLocal[0].length; i++) {
			gridLocal[0][i] = 0;
		}
		
		//initialize first row with zeros
		for (int i = 0; i < gridLocal.length; i++) {
			gridLocal[i][0] = 0;
		}
		
		int north;
		int west;
		int northwest;
		
		//fill out the local alignment grid
		for (int i = 1; i < gridLocal.length; i++) {
			for (int j = 1; j < gridLocal[0].length; j++) {
				
				//north cell plus indel penalty
				north = gridLocal[i][j-1] + indel;
				
				//west cell plus indel penalty
				west = gridLocal[i-1][j] + indel;
				
				//northwest cell plus match or mismatch
				if (s1.charAt(i-1)==s2.charAt(j-1)) {
					northwest = gridLocal[i-1][j-1] + match;
				} else {
					northwest = gridLocal[i-1][j-1] + mismatch;
				}
				
				//assign current cell to maximum between the north, west, and northwest values
				gridLocal[i][j] = Math.max(Math.max(north, west), northwest);
			}
		}
		
		//get the alignment score (maximum cell value in grid)
		int score = Integer.MIN_VALUE;
		for (int i = 1; i < gridLocal.length; i++) {
			for (int j = 1; j < gridLocal[0].length; j++) {
				score = Math.max(score, gridLocal[i][j]);
			}
		}
		return score;
	}
	
	/**
	 * Trace backwards through the global pairwise alignment grid to build the alignment
	 * @throws IOException 
	 */
	public void saveGlobalAlignment(String s1, String s2, String fileNameGlobal) throws IOException {
		
		//strings to hold the alignment for each sequence
		a1Global = "";
		a2Global = "";
		
		//flags to determine whether the alignment has reached index 0 of the sequences
		boolean doneA1 = false;
		boolean doneA2 = false;
		
		//start at the bottom right cell of the grid
		int i = gridGlobal.length - 1;
		int j = gridGlobal[0].length - 1;
		
		//initialize north, west, and northwest values
		int north = Integer.MIN_VALUE;
		int west = Integer.MIN_VALUE;
		int northwest = Integer.MIN_VALUE;
		
		//while at least one alignment string is still being built, keep building the alignment
		while(!doneA2 && !doneA1) {
			
			//if the second alignment string is still being built, check if the path arrow points to the north 
			if (!doneA2) {
				
				north = gridGlobal[i][j-1] + indel;
				if (gridGlobal[i][j] == north) {
					
					//append an indel to the first alignment string and the sequence character to the second alignment string
					a1Global = "-" + a1Global;
					a2Global = "" + s2.charAt(j-1) + a2Global;
					j = j - 1;
				}
			}
			
			//if the first alignment string is still being built, check if the path arrow points to the west
			if (!doneA1) {
				
				west = gridGlobal[i-1][j] + indel;
				if (gridGlobal[i][j] == west) {
					
					//append the sequence character to the first alignment string and an indel to the second alignment string
					a1Global = "" + s1.charAt(i-1) + a1Global;
					a2Global = "-" + a2Global;
					i = i - 1;
				}
			}
			
			//if both alignment strings are still being built, check if the path arrow points to the northwest
			if (!doneA1 && !doneA2) {
				
				if (s1.charAt(i-1)==s2.charAt(j-1)) {
					northwest = gridGlobal[i-1][j-1] + match;
				} else {
					northwest = gridGlobal[i-1][j-1] + mismatch;
				}
				
				if (gridGlobal[i][j] == northwest) {
					
					//append the sequence character to the first alignment string and the sequence character to the second alignment string
					a1Global = "" + s1.charAt(i-1) + a1Global;
					a2Global = "" + s2.charAt(j-1) + a2Global;
					i = i - 1;
					j = j - 1;
				}
			}
			
			//if the i index is zero, the first sequence is done being parsed
			if (i == 0) {
				doneA1 = true; 
			}
			
			//if the j index is zero, the second sequence is done being parsed
			if (j == 0) {
				doneA2 = true;
			}
		}
		
		//write the alignment sequences to a file if it does not already exist
		FileWriter w = new FileWriter(fileNameGlobal);
		int k;
		for(k = 0; k < a1Global.length() - 70; k+=70) {
			w.write(a1Global.substring(k, k + 70));
			w.write("\n");
			w.write(a2Global.substring(k, k + 70));
			w.write("\n\n");
		}
		w.write(a1Global.substring(k));
		w.write("\n");
		w.write(a2Global.substring(k));
		w.close();
	}
	
	/**
	 * Trace backwards through the local pairwise alignment grid to build the alignment
	 * @throws IOException 
	 */
	public void saveLocalAlignment(String s1, String s2, String fileNameLocal) throws IOException {
		
		//strings to hold the alignment for each sequence
		a1Local = "";
		a2Local = "";
		
		//flag to determine whether the alignment has reached the threshold
		boolean done = false;
		
		//initialize indices, north, west, and northwest values
		int i = Integer.MIN_VALUE;
		int j = Integer.MIN_VALUE;
		int north = Integer.MIN_VALUE;
		int west = Integer.MIN_VALUE;
		int northwest = Integer.MIN_VALUE;
		int score = Integer.MIN_VALUE;
		
		//find the maximum cell value in the grid and find its indices
		for (int k = 1; k < gridLocal.length; k++) {
			for (int m = 1; m < gridLocal[0].length; m++) {
				if (score <= gridLocal[k][m]) {
					score = gridLocal[k][m];
					i = k;
					j = m;
				}
			}
		}
		
		//while the threshold has not been reached, keep building the alignment
		while(!done) {
			
			//check if the path arrow points to the north 
			north = gridLocal[i][j-1] + indel;
			if (gridLocal[i][j] == north) {
				
				//append an indel to the first alignment string and the sequence character to the second alignment string
				a1Local = "-" + a1Local;
				a2Local = "" + s2.charAt(j-1) + a2Local;
				j = j - 1;
			}
			
			//check if the path arrow points to the west
			west = gridLocal[i-1][j] + indel;
			if (gridLocal[i][j] == west) {
				
				//append the sequence character to the first alignment string and an indel to the second alignment string
				a1Local = "" + s1.charAt(i-1) + a1Local;
				a2Local = "-" + a2Local;
				i = i - 1;
			}
			
			//check if the path arrow points to the northwest
			if (s1.charAt(i-1)==s2.charAt(j-1)) {
				northwest = gridLocal[i-1][j-1] + match;
			} else {
				northwest = gridLocal[i-1][j-1] + mismatch;
			}
			if (gridLocal[i][j] == northwest) {
				
				//append the sequence character to the first alignment string and the sequence character to the second alignment string
				a1Local = "" + s1.charAt(i-1) + a1Local;
				a2Local = "" + s2.charAt(j-1) + a2Local;
				i = i - 1;
				j = j - 1;
			}
			
			//if the i index is zero, the j index is 0, or the cell value is negative, the stop threshold is met
			if (i == 0 || j == 0 || (gridLocal[i][j] < 0)) {
				done = true;
			}
		}
		
		//write the alignment sequences to a file if it does not already exist
		FileWriter w = new FileWriter(fileNameLocal);
		int k;
		for(k = 0; k < a1Local.length() - 70; k+=70) {
			w.write(a1Local.substring(k, k + 70));
			w.write("\n");
			w.write(a2Local.substring(k, k + 70));
			w.write("\n\n");
		}
		w.write(a1Local.substring(k));
		w.write("\n");
		w.write(a2Local.substring(k));
		w.close();
	}
	
}