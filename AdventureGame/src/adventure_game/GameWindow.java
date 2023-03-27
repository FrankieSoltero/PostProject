package adventure_game;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.FileNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GameWindow extends JFrame {
    public GameWindow(){
       setTitle("Covid, The Zombie Apocalypse");
       // set background image
       JPanel contentP = new JPanel(new BorderLayout()) {

            BufferedImage image;
            {
            try {
                image = ImageIO.read(getClass().getResource("AdventureGame/data/levels/Hospital Map/HE.png"));
            }
             catch (IOException e) {
            e.printStackTrace();
            }
            }
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.drawImage(image, 0,0,getWidth(),getHeight(),this);
            }
        };

        
       // sets a new button panel that will allow the buttons
       // to move with the adjusted size of Game window.
       JPanel buttonPanel = new JPanel(new GridBagLayout());

       GridBagConstraints constraints = new GridBagConstraints();

       constraints.fill = GridBagConstraints.CENTER;
       constraints.anchor = GridBagConstraints.CENTER;



       JButton newGame = new JButton("New Game");
       JButton loadGame = new JButton("Load Game");

       constraints.gridx = 0;
       constraints.gridy = 0;

       buttonPanel.add(newGame, constraints);

       constraints.gridx = 0;
       constraints.gridy = 1;
       
       buttonPanel.add(loadGame, constraints);

       contentP.add(buttonPanel, BorderLayout.CENTER);

       setContentPane(contentP);
       
       setVisible(true);

       setSize(new Dimension(400,400));


       
    }
    
    public static void main(String[] args) throws FileNotFoundException{
        new GameWindow();
    }
}
