package adventure_game;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GameWindow extends JFrame {
    private JButton newGame;
    private JButton loadGame;
    private JPanel buttonPanel;
    private JPanel mainP;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    
    public GameWindow(){
        




       setTitle("Covid, The Zombie Apocalypse");

       contentPanel = new JPanel();
       cardLayout = new CardLayout();
       contentPanel.setLayout(cardLayout);
        
       // sets a new button panel that will allow the buttons
       // to move with the adjusted size of Game window.
       buttonPanel = new JPanel(new GridBagLayout());

       GridBagConstraints constraints = new GridBagConstraints();

       constraints.fill = GridBagConstraints.CENTER;
       constraints.anchor = GridBagConstraints.CENTER;



       newGame = new JButton("New Game");
       newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                cardLayout.show(contentPanel, "gamePanel");
                try {
                    Game.AdventureGameCreator(null);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
       });
       

       loadGame = new JButton("Load Game");

       constraints.gridx = 0;
       constraints.gridy = 0;

       buttonPanel.add(newGame, constraints);

       constraints.gridx = 0;
       constraints.gridy = 1;
       
       buttonPanel.add(loadGame, constraints);

       
       setVisible(true);

       setSize(new Dimension(800,800));


       
    }
    
    public static void main(String[] args) throws FileNotFoundException{
        new GameWindow();
        Game.AdventureGameCreator(null);
    }
}
