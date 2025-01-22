package com.cn2.communication;


import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.*;
import java.awt.event.*;
import java.lang.Thread;

@SuppressWarnings("unused")
public class App extends Frame implements WindowListener, ActionListener {

	/*
	 * Definition of the app's fields
	 */
	static TextField inputTextField;		
	static JTextArea textArea;				 
	static JFrame frame;					
	static JButton sendButton;				
	static JTextField meesageTextField;		  
	public static Color gray;				
	final static String newline="\n";		
	static JButton callButton;				
	
	//  initialize your variables here...
	String receiverAddress = "192.168.1.28";      // Place the IP Address of the other peer here
	final Integer MESSAGE_PORT = 12345;
	static Peer peer;
	
	
	/**
	 * Construct the app's frame and initialize important parameters
	 */
	public App() {                                                          
		           
		/*
		 * 1. Defining the components of the GUI
		 */
		// Setting up the characteristics of the frame
		super("CN2 - AUTH");	
		gray = new Color(254, 254, 254);		
		setBackground(gray);
		setLayout(new FlowLayout());			
		addWindowListener(this);	
		
		// Setting up the TextField and the TextArea
		inputTextField = new TextField();
		inputTextField.setColumns(20);
		
		// Setting up the TextArea.
		textArea = new JTextArea(10,40);			
		textArea.setLineWrap(true);				
		textArea.setEditable(false);			
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Setting up the buttons
		sendButton = new JButton("Send");			
		callButton = new JButton("Call");			
						
		/*
		 * 2. Adding the components to the GUI
		 */
		add(scrollPane);								
		add(inputTextField);
		add(sendButton);
		add(callButton);
		
		/*
		 * 3. Linking the buttons to the ActionListener
		 */
		sendButton.addActionListener(this);			
		callButton.addActionListener(this);


		//Peer will continuously listen for messages and start a client to send messages 
		peer = new Peer();                                                                     
		peer.start(this);                                                               
	}

	/**
	 * The main method of the application. It continuously listens for
	 * new messages.
	 */
	public static void main(String[] args){
		/*
		 * 1. Create the app's window
		 */
		App app = new App(); 																	  
		app.setSize(500,250);				  
		app.setVisible(true);				  
	}
	
	/**
	 * The method that corresponds to the Action Listener. Whenever an action is performed
	 * (i.e., one of the buttons is clicked) this method is executed. 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == sendButton){
			if (inputTextField.getText().trim().isEmpty()) {
				return;
			}
			peer.startClient(receiverAddress,MESSAGE_PORT);

			try {
				String message=peer.getAddress().getHostAddress()+": "+ inputTextField.getText();
				peer.getClient().sendMessageFromInputField(message);
				textArea.setText(textArea.getText()+"\n"+message);

			} catch (Error er) {
				System.out.println(er);
			}
			inputTextField.setText("");
					
			
		}else if(e.getSource() == callButton){
						
			if(callButton.getText()=="Call"){
				peer.voiceCall();
				callButton.setText("Stop call");
			}
			else if(callButton.getText()=="Stop call"){
				peer.stopVoiceCall();
				callButton.setText("Call");
			}
			
		}
			

	}
    public void handleMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(newline + message));
    }

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		dispose();
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}


}
