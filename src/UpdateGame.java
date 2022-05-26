import java.io.Serializable;
import java.util.ArrayList;


public class UpdateGame implements Serializable {
	private ArrayList<PlayerInfo> playersOnline;
	public UpdateGame(ArrayList<PlayerInfo> playerlist){
		playersOnline = playerlist;
	}
	public ArrayList<PlayerInfo> getPlayers() {
		return playersOnline;
	}
	public void removePlayer(String playerName){
		for (PlayerInfo player : playersOnline)
			if (player.getName().equals(playerName)){
				playersOnline.remove(player);
				return;
			}
	}
	public int searchByUsername(String username){
		for (PlayerInfo player : playersOnline){
			if (player.getName().equals(username))
				return playersOnline.indexOf(player);
		}
		return -1;
	}
}