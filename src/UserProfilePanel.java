import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class UserProfilePanel extends JPanel {
	private JLabel[] ja = new JLabel[3];
	private ServerAPI serverHandler;
	private String userName;
	private String [][] strArr;
	JPanel thisPanel;
	public UserProfilePanel(ServerAPI serverHandler, String userName){
		this.serverHandler =  serverHandler;
		this.userName = userName;
		new UserProfileUpdater().execute();
		thisPanel = this;
	}
	private class UserProfileUpdater extends SwingWorker<Void, Void>{

        @Override
        protected Void doInBackground() throws Exception {
            updateStatus();
            return null;
        }

        @Override
        protected void done(){
        	System.out.println("userProfile done");
        	// If previous player record is not cleared, clear up first
    		HomeFrame.setCurrentPlayers(new ArrayList<PlayerInfo>());
        	Font playerNameFont = new Font("Arial", Font.BOLD, 25);
    		Font paragraphFont = new Font("Arial", Font.PLAIN, 15);
    		ja[0] = new JLabel(strArr[0][0]);
    		ja[1] = new JLabel("Number of games: "+strArr[0][1]);
    		ja[2] = new JLabel("Number of wins: "+strArr[0][2]);
    		int counter = 0;
    		thisPanel.invalidate();
    		setBorder(new EmptyBorder(10,10,10,10));
    		setLayout(new BoxLayout(thisPanel, BoxLayout.Y_AXIS));
    		setPreferredSize(new Dimension(800, 550));
    		for (JLabel jt : ja){
    			jt.setAlignmentX(LEFT_ALIGNMENT);
    			jt.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    			if (counter == 0) {
    				jt.setFont(playerNameFont);
    			}
    			else {
    				jt.setFont(paragraphFont);
    			}
    			counter++;
    			add(jt);
    		}
        }
    }
    private void updateStatus() {
        if (serverHandler != null){
            try {
                strArr = serverHandler.userProfile(userName, HomeFrame.getCurrentPlayers());
                System.out.println("strArr = " + strArr );
            } catch (Exception e){
                System.err.println("User profile: "+e);
            }
        }
    }

}
