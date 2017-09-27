import javax.swing.JFrame;
import javax.swing.JPanel;


public class GameViewer3d extends JFrame{

	/**
	 * @param args
	 */
	

		
	public static void main(String[] args) 
	{	
		GameViewer3d window = new GameViewer3d();
		JPanel p = new JPanel();
        p.add(new GamePanel());  //  add a class that extends JPanel
        window.setTitle("NOW IN THREE DIMENSIONS!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        window.setContentPane(p);
        
       
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
	}

}