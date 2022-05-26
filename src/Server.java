
import java.rmi.*;
import java.rmi.server.*;
import java.sql.Connection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;  // Import the File class
import java.io.FileReader;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.PrintWriter;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.jms.Message;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;

public class Server extends UnicastRemoteObject implements ServerAPI {
	
	private JMSServer jmsServer;
	private Connection conn;
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";
	private static String gameStatus = "wait";
	private Thread thread;
	ArrayList<PlayerInfo> Playerlist = new ArrayList<PlayerInfo>();
	
	public static void main(String[] args) {
		try {
			Server cardGameServer = new Server();
			JMSServer jmsServer = new JMSServer();
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("24CardGameServer", cardGameServer);
			System.out.println("Service registered");
			System.out.println("JMS registered");
			cardGameServer.run(jmsServer);
		}catch(Exception e) {
			System.err.println("Exception thrown: "+e);
		}
		
		///Create and Clean OnlineUser
	    try {
		      File onlineUser = new File("OnlineUser.txt");
		      if (onlineUser.createNewFile()) {
		        System.out.println("File created: " + onlineUser.getName());
		      } else {
		        System.out.println("OnlineUser.txt already exists.");
		      }
		      FileWriter myWriter = new FileWriter(onlineUser);
		      PrintWriter pw = new PrintWriter(myWriter, false);
		      pw.flush();
		      pw.close();
		      myWriter.close();
		      System.out.println("Successfully cleaned OnlineUser.txt");
		} catch (IOException e) {
		      System.out.println("Error occur when handling onlineUser.txt.");
		      e.printStackTrace();
		}
	    
	}
	
	private void run(JMSServer jmsServer)throws Exception {
		this.jmsServer = jmsServer;
		new Thread(new WaitPlayer()).start();
	}
	class WaitPlayer implements Runnable {
    	public WaitPlayer() throws JMSException{
    		jmsServer.startService();
    	}
    	public void run() {
			try {
				System.out.println("Opened server, waiting player...");
		    	while(true){
					PlayerInfo playerInfo = jmsServer.receiveMessage(); //block until receive message
					System.out.println("Player come and get message..");
					if (gameStatus.equals("wait")){
						thread = new Thread(new HandlePlayer(playerInfo));
						thread.start();
					} else new Thread(new HandlePlayer(playerInfo)).start();
		    	}
			} catch (JMSException e) {
				System.out.println("Thread error: "+e);
			}
		}
	}
	
	class HandlePlayer implements Runnable {
    	
    	private PlayerInfo playerInfo;
    	
    	public HandlePlayer(PlayerInfo playerInfo){
    		this.playerInfo = playerInfo;
    	}
    	public void run() {
    		try {
    			try {
    				PreparedStatement stmt = conn.prepareStatement("SELECT gamePlayed, winTime from userinfo WHERE userName = ? ");
    				stmt.setString(1, playerInfo.getName());
    		        ResultSet rs = stmt.executeQuery();
    		        while(rs.next()){
    		        	System.out.println(rs.getInt(1));
    		        	System.out.println(rs.getInt(2));
    		        	playerInfo.setGamePlayed(rs.getInt(1));
    		        	playerInfo.setNumWin(rs.getInt(2));
    		       }
    			}catch(Exception e) {
    				e.printStackTrace();
    			}
    			
    			Playerlist.add(playerInfo);
    			if (gameStatus.equals("wait")) {
    				System.out.println("First Player Enter the room"+ playerInfo);
    				gameStatus = "Only1";
    				try { 
    					Thread.sleep(10000); //sleep 10 seconds 
    				} catch (InterruptedException e){ return; }
    				
					if (Playerlist.size() >= 2){
						System.out.println("10 seconds passed. Game start because player >=2");
						gameStatus = "wait"; //reset gamestatus and start new game
						Message m = jmsServer.convertMsg(new NewGame(Playerlist));
						jmsServer.broadcastMessage(m);
						Playerlist.clear();
					} else { 
						gameStatus = "NotEnoughPlayer";
					}
    			}else if (gameStatus.equals("Only1")) { // second player comes in, not timeout
					gameStatus = "2orMore";
					System.out.println("Second player come in: "+playerInfo);
					System.out.println("Wait for more players, no need sleep because not first player");
    			}else if (gameStatus.equals("NotEnoughPlayer")){ // second player comes in after timeout
					System.out.println("Second player come in: "+playerInfo);
					System.out.println("Two players after timeout! Game start...");
					gameStatus = "wait"; //reset gamestatus and start new game
					Message m = jmsServer.convertMsg(new NewGame(Playerlist));
					jmsServer.broadcastMessage(m);
					Playerlist.clear();
				}else if (gameStatus.equals("2orMore")) {
					System.out.println("Third player comein: "+playerInfo);
					gameStatus = "EnoughPlayer";
				}else {
					 // third or fourth player comes in before timeout
					System.out.println("Enough player to start the game...");
					gameStatus = "wait"; 
					Message m = jmsServer.convertMsg(new NewGame(Playerlist));
					thread.interrupt();
					jmsServer.broadcastMessage(m);
					Playerlist.clear();
				}
    		}catch (JMSException e){
				System.out.println("HandlePlayer error: "+e);
				e.printStackTrace();
			}
    	}
	}

	
	public Server() throws RemoteException,SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+
				"/"+DB_NAME+
				"?user="+DB_USER+
				"&password="+DB_PASS);
		System.out.println("Database connection successful.");
		//PreparedStatement createTablestmt = conn.prepareStatement(" USE c3358;CREATE TABLE userinfo1(userName varchar(32) NOT NULL,password varchar(32) NOT NULL, loginStatus int, gamePlayed int,winTime int,PRIMARY KEY ( userName ));");
        try {
        	String sql = "CREATE TABLE userinfo " +
                "(loginStatus INTEGER , " +
                " userName VARCHAR(32) not NULL, " + 
                " password VARCHAR(32) not NULL, " + 
                " gamePlayed INTEGER, " + 
                " winTime INTEGER, " + 
                " PRIMARY KEY ( userName ));"; 
        	Statement createTablestmt = conn.createStatement();
        	createTablestmt.executeUpdate(sql);
        }catch(Exception e) {
        	System.out.println(e);
        }
		PreparedStatement stmt = conn.prepareStatement("UPDATE userinfo SET loginStatus = 0");
		int rows = stmt.executeUpdate();
		if(rows > 0) {
			System.out.println("all LoginStatus updated to 0");
		} else {
			System.out.println("Update loginstatus fail");
		}
	}
	public synchronized String [] [] userProfile(String username, ArrayList<PlayerInfo> players) throws RemoteException, SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT userName , gamePlayed, winTime FROM userinfo WHERE userName = ?");
		stmt.setString(1, username);
		ResultSet rs = stmt.executeQuery();
		String [] [] strArr = new String [1][3];
		while (rs.next()){
			System.out.println("userProfile");
			strArr[0][0] = rs.getString(1);
			System.out.println(strArr[0][0]);
			for (int i = 1; i <= 2; i++) {
				strArr[0][i] = String.valueOf(rs.getInt(i + 1));
				System.out.println(strArr[0][i]);
			}
		}
		updateGame(username, players);
		return strArr;
	}
	
	public synchronized String [] [] leaderBoard(String username, ArrayList<PlayerInfo> players) throws RemoteException, SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT userName , gamePlayed, winTime FROM userinfo");
		ResultSet rs = stmt.executeQuery();
		String [] [] strArr = new String [5][3];
		int count = 0;
		System.out.println("leaderBoard");
		while (rs.next()){
			strArr[count][0] = rs.getString(1);
			strArr[count][1] = String.valueOf(rs.getInt(2));
			strArr[count][2] = String.valueOf(rs.getInt(3));
			System.out.println(strArr[count][0]);
			System.out.println(strArr[count][1]);
			System.out.println(strArr[count][2]);
			count++;
		}
		System.out.println(strArr.toString());
		updateGame(username, players);
		return strArr;
	}
	
	public synchronized int login(String username, String password) throws RemoteException, SQLException {
			PreparedStatement stmt = conn.prepareStatement("SELECT userName,password,loginStatus FROM userinfo");
			ResultSet rs = stmt.executeQuery();
			String currecntUser = "";
			String currecntpw = "";
			int loginStatus;
		    boolean found = false;
			while(rs.next()) {
				currecntUser = rs.getString("userName");
				currecntpw = rs.getString("password");
				//System.out.println(currecntUser);
				//System.out.println(currecntpw);
	            if (currecntUser.equals(username) && currecntpw.equals(password)){
	            	found = true;
	            	loginStatus = rs.getInt("loginStatus");
	            	//System.out.println(loginStatus);
	            	if (loginStatus == 1) {
	            		return 0;
	            	}
	            }
			}
			if(found) {
        		System.out.println("Enter update process");
        		System.out.println(username);
        		PreparedStatement stmt2 = conn.prepareStatement("UPDATE userinfo SET loginStatus = 1 WHERE userName = ?");
        		stmt2.setString(1, username);
        		int rows = stmt2.executeUpdate();
        		System.out.println(rows);
    			if(rows > 0) {
    				System.out.println("LoginStatus updated");
    				return 1;
    			} else {
    				System.out.println("Update loginstatus fail");
    				return 0;
    			}
			}
			return 0;
		
		/*
		/*
			int unMatchedPW = 0;
			try {
				//Check account info correct or not
				String currentLine2;
				String data2[];
				FileReader fr2 = new FileReader("UserInfo.txt");
				BufferedReader br2 = new BufferedReader(fr2);
				while ((currentLine2 = br2.readLine())!= null) {
					data2 = currentLine2.split(",");
					if (data2[0].equals(username) && data2[1].equals(password) ) {
						//pw and username is not matched
						unMatchedPW = 1;
					}
				}
				br2.close();
				fr2.close();
				if(unMatchedPW == 0) {
					return 0;
				}
				//Check Online already or not
				String currentLine;
				String data[];
				FileReader fr = new FileReader("OnlineUser.txt");
				BufferedReader br = new BufferedReader(fr);
				while ((currentLine = br.readLine())!= null) {
					data = currentLine.split(",");
					if (data[0].equals(username) ) {
						//same user online detected
						br.close();
						fr.close();
						return 0;
					}
				}
				//no repeat online
				FileWriter fw = new FileWriter("OnlineUser.txt",true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw); 
				pw.println(username + "," + password);
				//pw.println(username + "," + password + "\n");
				pw.flush();
				pw.close();
				bw.close();
				fw.close();
				br.close();
				fr.close();	
			}catch (IOException e) {
			      System.out.println("Error occur when handling onlineUser.txt.");
			      e.printStackTrace();
			      return 0;
			}
			return 1;
			*/
			
	}
	
	public synchronized int logOut(String username, ArrayList<PlayerInfo> players) throws RemoteException, SQLException {
		PreparedStatement stmt = conn.prepareStatement("UPDATE userinfo SET loginStatus = 0 WHERE userName = ?");
		stmt.setString(1, username);
		int rows = stmt.executeUpdate();
		System.out.println(rows);
		if(rows > 0) {
			System.out.println("LoginStatus updated");
			updateGame(username, players);
			return 0;
		} else {
			System.out.println("Update loginstatus fail");
			return 1;
		}
		
			//Check account info correct or not
			/*
			String currentLine;
			String data[];
			
			System.out.println("Logouting");
			
			File oldFile = new File("OnlineUser.txt");
			File newFile = new File("Temp.txt");
			
			try {
				FileWriter fw = new FileWriter("Temp.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw); 
				
				FileReader fr = new FileReader("OnlineUser.txt");
				BufferedReader br = new BufferedReader(fr);
				while ((currentLine = br.readLine())!= null) {
					data = currentLine.split(",");
					if (!(data[0].equals(username))) {
						pw.println(currentLine);
					}
				}	
				pw.flush();
				pw.close();
				bw.close();
				fw.close();
				br.close();
				fr.close();	
				oldFile.delete();
				File copyToNewOnlineUser = new File("OnlineUser.txt");
				newFile.renameTo(copyToNewOnlineUser);	
			}catch (IOException e) {
			      System.out.println("Error occur when handling onlineUser.txt.");
			      e.printStackTrace();
			      return 1;
			}
			return 0;
			*/
	}
	
	public synchronized int register(String username, String password) throws RemoteException, SQLException{
		//searching for the user 
		System.out.println(username);
		PreparedStatement stmt = conn.prepareStatement("SELECT userName FROM userinfo WHERE userName = ?");
		stmt.setString(1, username);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			System.out.println("User register before");
			return 0;
		}
		System.out.println("Registering");
        PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO userinfo (userName, password, gamePlayed, winTime, loginStatus) VALUES (?, ?, ?, ?, ?)");
        stmt2.setString(1, username);
        stmt2.setString(2, password);
        stmt2.setInt(3, 0);
        stmt2.setInt(4, 0);
        stmt2.setInt(5, 1);
        stmt2.executeUpdate();
        return 1;
		/*
		String currentLine;
		String data[];
		System.out.println("Registering.");
		try {
			FileWriter fw = new FileWriter("UserInfo.txt",true);
			FileReader fr = new FileReader("UserInfo.txt");
			BufferedReader br = new BufferedReader(fr);
			while ((currentLine = br.readLine())!= null) {
				data = currentLine.split(",");
				if (data[0].equals(username)) {
					//same username detected
					br.close();
					fr.close();
					return 0;
				}
			}
			//no same username detected 
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			pw.println(username + "," + password);
			//pw.println(username + "," + password + "\n");
			pw.flush();
			pw.close();
			bw.close();
			fw.close();
			br.close();
			fr.close();	
			// add new user to online user
			File onlineUser = new File("OnlineUser.txt");
			FileWriter myWriter = new FileWriter(onlineUser,true);
			PrintWriter pw2 = new PrintWriter(myWriter);
			pw2.println(username + "," + password);
		    pw2.close();
		    myWriter.close();
			return 1;
		}catch (IOException e) {
		      System.out.println("Error occur when handling UserInfo.txt.");
		      e.printStackTrace();
		      return 0;
		}
		*/
	}
	public synchronized void play(String username, ArrayList<PlayerInfo> players) throws RemoteException {
		updateGame(username, players);
	}
	public int verifyResult(String username, NewGame newGame, String userInputField)throws SQLException, RemoteException{
		System.out.println("Verifying");
		Jep jep = new Jep();
		Object res = null;
		try {
			System.out.println(userInputField);
			jep.parse(userInputField);
			res = jep.evaluate();
			Double ca = (Double) res;
			if (ca == 24){
				System.out.println("Correct answer.");
				int idex = newGame.searchByUsername(username);
				System.out.println(idex + "Win");
				ArrayList<PlayerInfo> players = new ArrayList<PlayerInfo>();
				if (idex != -1){
					players = newGame.getPlayers();
					for (int i = 0; i < players.size(); i++){
						PlayerInfo player = players.get(i);
						player.setGamePlayed(player.getGamePlayed()+1);  //everyone add 1 game reocrd
						if (i == idex) {
							player.setNumWin(player.getNumWin()+1);
							System.out.println("update win 1 time");
						}
						players.set(i, player);
					}
				}
				PreparedStatement stmt = conn.prepareStatement("UPDATE userinfo SET gamePlayed = ?, winTime = ? WHERE userName = ?");
	    		System.out.println("players size is " + players.size());
				for (PlayerInfo player : players){
	    			stmt.setInt(1, player.getGamePlayed());
	    			stmt.setInt(2, player.getNumWin());
	    			stmt.setString(3, player.getName());
	    			System.out.println(player.getGamePlayed());
	    			System.out.println(player.getNumWin());
	    			System.out.println(player.getName());
		    		stmt.executeUpdate();
	    		}
				EndGame endGame = new EndGame(players, username,userInputField);
				try {
					Message m = jmsServer.convertMsg(endGame);
					jmsServer.broadcastMessage(m);
				} catch (JMSException e){
					e.printStackTrace();
				}
				return 1;
			}
			System.out.println("Wrong Answer");
			return 0;	
		} catch (EvaluationException e) {
			System.out.println("EvaluationException"+ e);
			return 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	private void updateGame(String username, ArrayList<PlayerInfo> players){
		if (players.size() > 0){
			for (int i = 0; i < players.size(); i++)
				if (players.get(i).getName().equals(username))
					players.remove(i);
			System.out.println("Only "+players.toString()+" are online");
			UpdateGame updateGame = new UpdateGame(players);
			try {
				Message m = jmsServer.convertMsg(updateGame);
				jmsServer.broadcastMessage(m);
			} catch (JMSException e){
				e.printStackTrace();
			}
		}
	}

}
