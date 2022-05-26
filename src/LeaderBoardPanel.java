import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class  LeaderBoardPanel extends JPanel {
	private ServerAPI serverHandler;
	private String userName;
	private JTable rankTable;
	private Object [][] data;
	private LeaderBoardPanel thisPanel;
	
	public LeaderBoardPanel(ServerAPI serverHandler, String userName){
		this.serverHandler =  serverHandler;
		this.userName = userName;
		new LeaderBoardUpdater().execute();
		thisPanel = this;
	}
	private class LeaderBoardUpdater extends SwingWorker<Void, Void>{

        @Override
        protected Void doInBackground() throws Exception {
            updateStatus();
            return null;
        }

        @Override
        protected void done(){
        	System.out.println("LeaderBorad done");
    		HomeFrame.setCurrentPlayers(new ArrayList<PlayerInfo>());
        	thisPanel.removeAll();
        	thisPanel.revalidate();
    		String[] columns = {"Player","Games played","Games won"};
    		Object[][] data2 = {
    			    {"Zyron", 1, 1},
    			    {"Thomas", 10, 3},
    			    {"Darren", 3, 2},
    			    {"Angel", 5, 4},
    			    {"Sam", 6, 3},
    			    {"Ben", 3, 1},
    			    {"Tom", 4, 1},
    			    {"Kathy", 6, 1}};
    		rankTable = new JTable(data2,columns);
    		thisPanel.setLayout(new BorderLayout());
    		rankTable.setPreferredScrollableViewportSize(new Dimension(800,550));
    		thisPanel.add(new JScrollPane(rankTable));
        }
    }
    private void updateStatus() {
        if (serverHandler != null){
            try {
            	data = serverHandler.leaderBoard(userName, HomeFrame.getCurrentPlayers());
                System.out.println("strArr = " + data );
            } catch (Exception e){
                System.err.println("User profile: "+e);
            }
        }
    }

}
