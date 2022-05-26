import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.rmi.*;
import java.rmi.registry.*;
import java.sql.SQLException;


public class Client implements Runnable {
	private ServerAPI serverHandler;
	private int loginStatus;
	private int registerStatus;
	private String userName;
	private String passWord;
	private JFrame loginFrame;
	private JFrame mainFrame;
	private JPanel mainPanel;
	private JLabel userNameTag;
	private JPanel leaderBroadPanel;
	private String host;
	
	public Client(String host) {
	    try {
	    	this.host = host;
	        Registry registry = LocateRegistry.getRegistry(host);
	        serverHandler = (ServerAPI)registry.lookup("24CardGameServer");
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
	}
	/*
	public Client() {
	    try {
	    	serverHandler = (ServerAPI)Naming.lookup("24CardGameServer");
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
	}
	*/

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Client(args[0]));;
		//SwingUtilities.invokeLater(new Client());
	}
	
	
	public void run() {
		loginFrameCreation();
		mainFrameCreation();
	}
	
	private void mainFrameCreation() {
		
		mainFrame = new JFrame("JPoker-24Game");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(1500,900);
		
		leaderBroadPanel = new JPanel();
		leaderBroadPanel.setLayout(null);
		//mainFrame.add(leaderBroadPanel);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		//mainFrame.add(mainPanel);
		
		
		JLabel rankingTag = new JLabel("Rank");
		rankingTag.setBounds(100,50,150,25);
		leaderBroadPanel.add(rankingTag);
		JLabel playerTag = new JLabel("Player");
		playerTag.setBounds(200,50,150,25);
		leaderBroadPanel.add(playerTag);
		JLabel gameWonTag = new JLabel("Game Won");
		gameWonTag.setBounds(300,50,150,25);
		leaderBroadPanel.add(gameWonTag);
		JLabel gamePlayTag = new JLabel("Game Play");
		gamePlayTag.setBounds(400,50,150,25);
		leaderBroadPanel.add(gamePlayTag);
		JLabel averageWinningTimeTag = new JLabel("Average Winning Time");
		averageWinningTimeTag.setBounds(500,50,150,25);
		leaderBroadPanel.add(averageWinningTimeTag);
		leaderBroadPanel.setVisible(true);
		
		JButton profileButton = new JButton("User Profile");
		JButton gameButton = new JButton("Play Game");
		JButton leaderBroadButton = new JButton("Leader Broad");
		JButton logOutButton = new JButton("Logout");
		profileButton.setBounds(10,10,150,25);
		gameButton.setBounds(210,10,150,25);
		leaderBroadButton.setBounds(410,10,150,25);
		logOutButton.setBounds(610,10,150,25);
				
		mainFrame.add(profileButton);
		mainFrame.add(gameButton);
		mainFrame.add(leaderBroadButton);
		mainFrame.add(logOutButton);
		
		JPanel cards =new JPanel(new CardLayout()); 
		cards.add(mainPanel, "mainPanel");
		cards.add(leaderBroadPanel, "leaderBroadPanel");
		CardLayout cl=(CardLayout)(cards.getLayout());
		cl.show(cards,"mainPanel");
		mainFrame.add(cards);
		
		leaderBroadButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                    	cl.show(cards,"leaderBroadPanel");
                    }
                });			
			} 
		});
		profileButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                    	cl.show(cards,"mainPanel");
                    }
                });			
			} 
		});
		
		logOutButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				new logOutUpdater().execute();
			} 
		});
		
		userNameTag = new JLabel();
		userNameTag.setFont(new Font("Calibri", Font.BOLD, 20));
		userNameTag.setBounds(20,50,165,25);
		mainPanel.add(userNameTag);
		JLabel numOfWinTag = new JLabel("Number of Wins: 10");
		numOfWinTag.setBounds(20,80,165,25);
		mainPanel.add(numOfWinTag);
		JLabel numOfGameTag = new JLabel("Number of Games: 20");
		numOfGameTag.setBounds(20,110,165,25);
		mainPanel.add(numOfGameTag);
		JLabel averageOfTimeTag = new JLabel("Average time to win: 12.5s");
		averageOfTimeTag.setBounds(20,140,165,25);
		mainPanel.add(averageOfTimeTag);
		JLabel rankTag = new JLabel("RANK: #10 ");
		rankTag.setFont(new Font("Calibri", Font.BOLD, 30));
		rankTag.setBounds(20,180,165,30);
		mainPanel.add(rankTag);
		mainFrame.setVisible(false);
		
	}

	private void loginFrameCreation() {
		//login frame
		loginFrame = new JFrame("Login");
		JPanel panel = new JPanel();
		loginFrame.setSize(500,300);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.add(panel);	
		panel.setLayout(null);	
		JLabel loginLabel = new JLabel("Login Name");
		loginLabel.setBounds(10,20,80,25);//x,y,w,h
		panel.add(loginLabel);
		JTextField loginName = new JTextField(20);
		loginName.setBounds(100,20,165,25);
		panel.add(loginName);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setBounds(10,50,80,25);
		panel.add(passwordLabel);
		JTextField passwordText = new JTextField(20);
		passwordText.setBounds(100,50,165,25);
		panel.add(passwordText);	
		JButton loginButton = new JButton("Login");
		loginButton.setBounds(10,80,100,25);
		loginButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if (loginName.getText().isEmpty() || passwordText.getText().isEmpty() ) {
					JOptionPane.showMessageDialog(loginFrame, "Username or Password should not be empty.");
				}
				else {
					userName = loginName.getText();
					passWord = passwordText.getText();
					new loginUpdater().execute();
				}
			} 
		});
		panel.add(loginButton);		
		JButton registerButton = new JButton("Register");
		registerButton.setBounds(150,80,100,25);
		JLabel confirmPasswordLabel = new JLabel("Confirm PW");
		confirmPasswordLabel.setBounds(10,80,80,25);
		panel.add(confirmPasswordLabel);
		JTextField confirmPasswordText = new JTextField(20);
		confirmPasswordText.setBounds(100,80,165,25);
		panel.add(confirmPasswordText);
		loginButton.setVisible(false);
		registerButton.setVisible(false);
		JButton newUserButton = new JButton("Register");
		newUserButton.setBounds(10,110,100,25);
		newUserButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if (loginName.getText().isEmpty() || passwordText.getText().isEmpty() ) {
					JOptionPane.showMessageDialog(loginFrame, "Username or Password should not be empty.");
				}
				else {
					if (passwordText.getText().equals(confirmPasswordText.getText()) ) {
						userName = loginName.getText();
						passWord = passwordText.getText();
						new registerUpdater().execute();
					}
				}
				if (passwordText.getText().equals(confirmPasswordText.getText()) == false){
					JOptionPane.showMessageDialog(loginFrame, "Password should remain the same.");
				}
			} 
		});
		panel.add(newUserButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(150,110,100,25);
		cancelButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				confirmPasswordLabel.setVisible(false);
				confirmPasswordText.setVisible(false);
				newUserButton.setVisible(false);
				cancelButton.setVisible(false);
				loginButton.setVisible(true);
				registerButton.setVisible(true);
			} 
		});
		panel.add(cancelButton);
		registerButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				confirmPasswordLabel.setVisible(true);
				confirmPasswordText.setVisible(true);
				newUserButton.setVisible(true);
				cancelButton.setVisible(true);
				loginButton.setVisible(false);
				registerButton.setVisible(false);
			} 
		});
		panel.add(registerButton);
		loginFrame.setVisible(true);
		confirmPasswordLabel.setVisible(false);
		confirmPasswordText.setVisible(false);
		newUserButton.setVisible(false);
		cancelButton.setVisible(false);
		loginButton.setVisible(true);
		registerButton.setVisible(true);
	}

	public void login() throws SQLException {
		System.out.println("USERNAME = " + userName);
		System.out.println("PASSWORD = " + passWord);
		// TODO: update GUI according to result of login
	    if(serverHandler != null) {
	        try {
	            loginStatus = serverHandler.login(userName,passWord);
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	    }
	}
	
	public void logOut() throws SQLException {
		System.out.println("USERNAME = " + userName);
		System.out.println("PASSWORD = " + passWord);
		// TODO: update GUI according to result of login
	    if(serverHandler != null) {
	    	/*
	        try {
	            loginStatus = serverHandler.logOut(userName,passWord);
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	        */
	    }
	}
	
	public void register() throws SQLException {
		System.out.println("USERNAME = " + userName);
		System.out.println("PASSWORD = " + passWord);
		// TODO: update GUI according to result of login
	    if(serverHandler != null) {
	        try {
	            registerStatus = serverHandler.register(userName,passWord);
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	    }
	}
	private class loginUpdater extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws SQLException {
			login();
			return null;
		}
		protected void done() {
			if (loginStatus == 1) {
				System.out.println("You have login successfully!!");
				//userNameTag.setText(userName);
				loginFrame.setVisible(false);
				new HomeFrame(userName, serverHandler, host,loginFrame);
				//mainFrame.setVisible(true);
			}else {
				JOptionPane.showMessageDialog(loginFrame, "Failed to login!!");
				System.out.println("Failed to login!!");
			}
			//wordCountLabel.setText(""+wordCount);
			//wordCountLabel.invalidate();
		}
	}
	
	private class logOutUpdater extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws SQLException {
			logOut();
			return null;
		}
		protected void done() {
			if (loginStatus == 0) {
				System.out.println("You have logout successfully!!");
				mainFrame.setVisible(false);
				loginFrame.setVisible(true);
			}else {
				JOptionPane.showMessageDialog(loginFrame, "Failed to logout!!");
				System.out.println("Failed to logout!!");
			}
			//wordCountLabel.setText(""+wordCount);
			//wordCountLabel.invalidate();
		}
	}
	
	private class registerUpdater extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws SQLException {
			register();
			return null;
		}
		protected void done() {
			if (registerStatus == 1) {
				System.out.println("You have registered successfully!!");
				//userNameTag.setText(userName);
				loginFrame.setVisible(false);
				new HomeFrame(userName, serverHandler, host,loginFrame);
			}else {
				JOptionPane.showMessageDialog(loginFrame, "You have registered before.");
				System.out.println("Failed to register!!");
			}
			//wordCountLabel.setText(""+wordCount);
			//wordCountLabel.invalidate();
		}
	}
}

