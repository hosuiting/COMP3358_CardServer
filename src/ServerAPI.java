import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.singularsys.jep.ParseException;


public interface ServerAPI extends Remote {
	int login(String username, String password) throws RemoteException,SQLException;
	int logOut(String username, ArrayList<PlayerInfo> players) throws RemoteException,SQLException;
	int register(String username, String password) throws RemoteException,SQLException;
	void play(String username, ArrayList<PlayerInfo> players) throws RemoteException;
	int verifyResult(String username, NewGame newGame, String userInputField)throws SQLException, RemoteException;
	String [] [] userProfile(String username, ArrayList<PlayerInfo> players) throws RemoteException, SQLException;
	String [] [] leaderBoard(String username, ArrayList<PlayerInfo> players) throws RemoteException, SQLException;
}
