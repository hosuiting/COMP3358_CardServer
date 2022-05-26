import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.naming.NamingException;

public class HomeFrame extends JFrame {
    private String userName;
    private ServerAPI serverHandler;
    private JMS jmsHandler;
	private MessageProducer queueSender;
	private MessageConsumer topicReceiver;
	private JPanel mainPanel;
	private JPanel topPanel;
	private JPanel userProfilePanel;
	private JButton userProfileButton;
    private JButton playGameButton;
    private JButton leaderBoardButton;
    private JButton logoutButton;
    private JFrame loginFrame;
    private JPanel ContentPanel;
    private static ArrayList<PlayerInfo> currentPlayers = new ArrayList<PlayerInfo>();
    private int loginStatus = 1;
	
	public HomeFrame (String Name, ServerAPI Handler,String host, JFrame loginFrame) {
		this.userName = Name;
		this.serverHandler = Handler;
		this.loginFrame = loginFrame;
		
		try {
			jmsHandler = new JMS(host);
			queueSender = jmsHandler.createQueueSender();
			topicReceiver = jmsHandler.createTopicReceiver();
			System.out.println("JMS registered");
		}catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
		
        setTitle("JPoker 24-Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500,900);
        //setPreferredSize(new Dimension(800,600));
        //setResizable(false);
        setVisible(true);
        mainPanel = new JPanel();
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0,4));
		topPanel.setPreferredSize(new Dimension(800,40));
   		userProfileButton = new JButton("User Profile");
		topPanel.add(userProfileButton);
		playGameButton = new JButton("Play Game");
		topPanel.add(playGameButton);
		leaderBoardButton = new JButton("Leader Board");
		topPanel.add(leaderBoardButton);
		logoutButton = new JButton("Logout");
		topPanel.add(logoutButton);
        mainPanel.add(topPanel,BorderLayout.PAGE_START);
        //userProfilePanel = new UserProfilePanel(serverHandler, username);
        userProfilePanel = new UserProfilePanel(serverHandler, userName);
        /*
        userProfilePanel.setSize(800,500);
        
        //////////////////////////////
        JLabel userNameTag = new JLabel();
        userNameTag.setFont(new Font("Calibri", Font.BOLD, 20));
        userNameTag.setBounds(20,50,165,25);
        userProfilePanel.add(userNameTag);
		JLabel numOfWinTag = new JLabel("Number of Wins: 10");
		numOfWinTag.setBounds(20,80,165,25);
		userProfilePanel.add(numOfWinTag);
		JLabel numOfGameTag = new JLabel("Number of Games: 20");
		numOfGameTag.setBounds(20,110,165,25);
		userProfilePanel.add(numOfGameTag);
		JLabel averageOfTimeTag = new JLabel("Average time to win: 12.5s");
		averageOfTimeTag.setBounds(20,140,165,25);
		userProfilePanel.add(averageOfTimeTag);
		JLabel rankTag = new JLabel("RANK: #10 ");
		rankTag.setFont(new Font("Calibri", Font.BOLD, 30));
		rankTag.setBounds(20,180,165,30);
		userProfilePanel.add(rankTag);
		*/
		////////////
		
		mainPanel.add(userProfilePanel, BorderLayout.CENTER);
		setContentPane(mainPanel);
		
		userProfileButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				remove(userProfilePanel);
				repaint();
				userProfilePanel = new UserProfilePanel(serverHandler, userName);
				add(userProfilePanel, BorderLayout.CENTER);
				invalidate();
				validate();
			} 
		});
		playGameButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				remove(userProfilePanel);
				repaint();
				userProfilePanel = new GamePanel(serverHandler, userName, jmsHandler, queueSender, topicReceiver);
				add(userProfilePanel, BorderLayout.CENTER);
				invalidate();
				validate();
			} 
		});
		leaderBoardButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				remove(userProfilePanel);
				repaint();
				userProfilePanel = new LeaderBoardPanel(serverHandler, userName);
				add(userProfilePanel, BorderLayout.CENTER);
				invalidate();
				validate();
			}
		});
		logoutButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				new logOutUpdater().execute();
			} 
		});   
	}
	
	private class logOutUpdater extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws SQLException {
			logOut();
			return null;
		}
		protected void done() {
			if (loginStatus == 0) {
				System.out.println("You have logout successfully!!");
				setVisible(false);
				loginFrame.setVisible(true);
			}else {
				System.out.println("Failed to logout!!");
			}
			//wordCountLabel.setText(""+wordCount);
			//wordCountLabel.invalidate();
		}
	}
	
	public void logOut() throws SQLException {
		// TODO: update GUI according to result of login
	    if(serverHandler != null) {
	        try {
	            loginStatus = serverHandler.logOut(userName,currentPlayers);
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	    }
	}
	public static ArrayList<PlayerInfo> getCurrentPlayers() {
		return currentPlayers;
	}
	public static void setCurrentPlayers(ArrayList<PlayerInfo> currentPlayers) {
		HomeFrame.currentPlayers = currentPlayers;
	}

}
