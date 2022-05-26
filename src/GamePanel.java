import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

public class GamePanel extends JPanel implements MessageListener{
	private ServerAPI serverHandler;
	private String username;
	private JPanel playground , scoreUI;
	private JButton SubmitButton;
	private JMS jmsHandler;
	private MessageProducer queueSender;
	private MessageConsumer topicReceiver;
	private JTextField userInputField;
	private PlayerInfo myPlayer;
	private long startTime = 0, endTime = 0;
	private NewGame newGame;
	private int winStatus;
	
	public GamePanel(ServerAPI serverHandler, String userName, JMS jmsHandler, MessageProducer queueSender, MessageConsumer topicReceiver){
		this.serverHandler = serverHandler;
		this.username = userName;
		this.jmsHandler = jmsHandler;
		this.queueSender = queueSender;
		this.topicReceiver = topicReceiver;
		
		playground = new NewGamePanel();
		add(playground, BorderLayout.CENTER);
		setSize(800,550);
		
		try {
			this.topicReceiver.setMessageListener(this);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new GamePlayUpdate().execute();    
	}
	
	private class NewGamePanel extends JPanel {
		
		private JButton newGameButton;
		
		public NewGamePanel(){
			newGameButton = new JButton("New Game");
			newGameButton.setPreferredSize(new Dimension(800, 550));
			newGameButton.addActionListener(new GameButtonListener());
			add(newGameButton, BorderLayout.CENTER);
		}
	}
	
	private class GameButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			remove(playground);
			repaint();
			playground = new WaitingPanel();
			add(playground, BorderLayout.CENTER);
			invalidate();
			validate();
			new LoadingGameUI().execute();
		}
		
		private class LoadingGameUI extends SwingWorker<Void, Void>{

			@Override
			protected Void doInBackground() throws Exception {
				// TODO Auto-generated method stub
				startGame();
				return null;
			}

			@Override
			protected void done() {
				super.done();
			}
			
		}
		
	}
	private class ScoreInterface extends JPanel {
		private ScoreInterface(ArrayList<PlayerInfo> players){
			setPreferredSize(new Dimension(180, 550));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			for (PlayerInfo player : players)
				add(new ScoreChildInterface(player));
		}
	}
	
	private class ScoreChildInterface extends JPanel {
		private ScoreChildInterface(PlayerInfo playerInfo){
			setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setMaximumSize(new Dimension(200,75));
			add(new JLabel(playerInfo.getName()));
			add(new JLabel("Win: "+playerInfo.getNumWin()+"/ Total:"+playerInfo.getGamePlayed()));
		}
	}
	
	private class WaitingPanel extends JPanel {
		private JLabel HintMessage;
		public WaitingPanel(){
			HintMessage = new JLabel("Waiting for Players...", SwingConstants.CENTER);
			setPreferredSize(new Dimension(800, 550));
			add(HintMessage, BorderLayout.CENTER);
		}
	}
	private void startGame() throws JMSException, NamingException{
		if (myPlayer == null)
			myPlayer = new PlayerInfo(username);
		try {
			Message message = null;
			message = jmsHandler.createMessage(myPlayer);
			if(message != null) queueSender.send(message);
			System.out.println("Message Sent");
		} catch (JMSException e1) {
			System.err.println("Failed to send message");
		}
	}
	/*
	private class LoadingGameUI extends SwingWorker<Void, Void>{

		@Override
		protected Void doInBackground() throws Exception {
			startGame();
			return null;
		}

		@Override
		protected void done() {
		}
		
	}*/
	private class GamePlayUpdate extends SwingWorker<Void, Void>{
		@Override
		protected Void doInBackground() throws Exception {
			playGame();
			return null;
		}

		@Override
		protected void done() {
			HomeFrame.setCurrentPlayers(new ArrayList<PlayerInfo>());
		}
		
	}
	private void playGame() {
	    if(serverHandler != null) {
	        try {
	            serverHandler.play(username, HomeFrame.getCurrentPlayers());
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	    }
	}
	private class GameRoomPanel extends JPanel {
		private JPanel gameInterface;
		public GameRoomPanel(String [] cards, ArrayList<PlayerInfo> players){
			gameInterface = new GameInterface(cards);
			setPreferredSize(new Dimension(800,550));
			add(gameInterface, BorderLayout.CENTER);
			scoreUI = new ScoreInterface(players);
			add(scoreUI, BorderLayout.EAST);
		}
	}
	private class GameInterface extends JPanel {
		public GameInterface(String [] cards){
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setPreferredSize(new Dimension(600,550));
			add(new CardsInterface(cards));
			/*
			for(i=0;i<=cards.size();i++) {
				System.err.println("cards");
			}*/
			add(new InputInterface(cards));
		}
	}
	
	private class InputInterface extends JPanel {
		private InputInterface(String [] cards){
			setPreferredSize(new Dimension(600,40));
			userInputField = new JTextField();
			/*
			userInputField.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					userInputField.setEnabled(false);
					//endTime = System.nanoTime();
					new GameOverUpdater().execute();
				}
				
			});*/
			userInputField.setPreferredSize(new Dimension(550,40));
			add(userInputField, BorderLayout.CENTER);
			SubmitButton = new JButton("Submit");
			add(SubmitButton, BorderLayout.EAST);
			SubmitButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					userInputField.setEnabled(false);
					SubmitButton.setEnabled(false);
					new verifyAnswer().execute();
				}
				
			});
		}
	}
	private class verifyAnswer extends SwingWorker<Void, Void>{
		@Override
		protected Void doInBackground() throws Exception {
			checkAns();
			return null;
		}

		@Override
		protected void done() {
			userInputField.setEnabled(true);
			SubmitButton.setEnabled(true);
            if (winStatus == 1){
                System.out.println("Correct answer.");
            } else {
            	System.out.println("Wrong answer.");
            }
			
		}
		
	}
	private class GameOverUI extends JPanel {
		private JButton nextGameButton;
		private GameOverUI(String username, String answer){
			setPreferredSize(new Dimension(800, 550));
			add(new GameResultPanel(username, answer), BorderLayout.CENTER);
			nextGameButton = new JButton("Next Game");
			nextGameButton.setPreferredSize(new Dimension(800, 40));
			nextGameButton.addActionListener(new GameButtonListener());
			add(nextGameButton, BorderLayout.SOUTH);
		}
	}
	private class GameResultPanel extends JPanel {
		private JLabel winnnerName;
		private JLabel formula;
		private GameResultPanel(String username, String answer){
			setPreferredSize(new Dimension(800, 470));
			winnnerName = new JLabel("Winner: "+username, SwingConstants.CENTER);
			formula = new JLabel(answer, SwingConstants.CENTER);
			add(new ResultJLabelPanel());
		}
		private class ResultJLabelPanel extends JPanel {
			public ResultJLabelPanel(){
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				add(winnnerName);
				add(formula);
			}
		}
	}
    private void checkAns() {
        if (serverHandler != null){
            try {
            	System.err.println("Checking answer");
            	winStatus = serverHandler.verifyResult(username, newGame, userInputField.getText());
            } catch (Exception e){
                System.err.println("Verify result error: "+e);
            }
        }else {
        	System.err.println("Serverhandler disappear");
        }
    }
	private class CardsInterface extends JPanel {
		public CardsInterface(String [] cards){
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setPreferredSize(new Dimension(600,400));
			for (int i = 0; i < 4; i++){
				add(new Card(cards[i]+".png"));
				System.err.println(cards[i]);
			}
		}
		private class Card extends JPanel{
			private Card(String file){
				setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
				URL url = null;
				try {
					url = getClass().getResource(file).toURI().toURL();
				} catch (MalformedURLException | URISyntaxException e) {
					e.printStackTrace();
				}
				ImageIcon ii = new ImageIcon(url);
				Image img = ii.getImage();
				Image resized = img.getScaledInstance(100, 145, Image.SCALE_DEFAULT);
				setPreferredSize(new Dimension(65,400));
				add(new JLabel(new ImageIcon(resized)));
			}
		}
	}
	

	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		try {
			Object output = ((ObjectMessage)arg0).getObject();
			if(output instanceof NewGame) {
				NewGame thisGame = (NewGame) output; //convert the jmsmessage back to game object
				if (thisGame.searchByUsername(username) != -1){   //this game include me 
					newGame = (NewGame) output;
					System.out.println("I am player "+ newGame.searchByUsername(username));
					System.out.println("Received message from server, Start the game.");
					HomeFrame.setCurrentPlayers(newGame.getPlayers());
					remove(playground);
					repaint();
					playground = new GameRoomPanel(newGame.getCards(), newGame.getPlayers());
					add(playground, BorderLayout.CENTER);
					//startTime = System.nanoTime();
					invalidate();
					validate();
				}
			}else if (output instanceof EndGame){
				EndGame endGame = (EndGame) output;
				int idx = endGame.searchByUsername(username);
				if (idx != -1){
					System.out.println("Received End game order from server");
					myPlayer = endGame.getPlayers().get(idx);
					HomeFrame.setCurrentPlayers(new ArrayList<PlayerInfo>());
					remove(playground);
					repaint();
					playground = new GameOverUI(endGame.getWinnerName(), endGame.getWinnerFormula());
					add(playground, BorderLayout.CENTER);
					invalidate();
					validate();
				}
			}else if (output instanceof UpdateGame){
				UpdateGame updateGame = (UpdateGame) output;
				System.out.println("Player leave ,hence update UI");
				if (updateGame.searchByUsername(username) != -1){
					HomeFrame.setCurrentPlayers(updateGame.getPlayers());
					String [] playerNames = new String[newGame.getPlayers().size()];
			    	for (int i = 0; i < newGame.getPlayers().size(); i++) {
			    		playerNames[i] = newGame.getPlayers().get(i).getName();
			    	}
					for (int i = 0; i < playerNames.length; i++) {
						if (updateGame.searchByUsername(playerNames[i]) == -1) {
							newGame.removePlayer(playerNames[i]);
						}
					}
					playground.remove(scoreUI);
					repaint();
					scoreUI = new ScoreInterface(updateGame.getPlayers());
					playground.add(scoreUI, BorderLayout.EAST);
					invalidate();
					validate();
				}
			}
		}catch (JMSException e) {
			System.err.println("Failed to receive message");
		}
		
	}
}
