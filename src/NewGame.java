import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class NewGame extends UpdateGame implements Serializable {
	private String [] cards = new String[4];
	
	public NewGame(ArrayList<PlayerInfo> playerlist){
		super(playerlist);
		Random r = new Random();
		char [] pattern = {'a','b','c','d'}, shuf = new char[4];
		int [] num = new int[4];
		for (int i = 0; i < 4; i++){
			shuf[i] = pattern[r.nextInt(4)];
			num[i] = r.nextInt(13)+1;
			cards[i] = shuf[i]+Integer.toString(num[i]);
		}
	}

	public String[] getCards() {
		return cards;
	}
}
