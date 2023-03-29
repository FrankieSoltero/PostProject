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
    public GameWindow(){
        JButton newGame;
        JButton loadGame;
        JPanel buttonPanel;
        JPanel contentP;



       setTitle("Covid, The Zombie Apocalypse");
       // set background image
       contentP = new JPanel() {

            BufferedImage image;
            {
            try {
                image = ImageIO.read(new File("AdventureGame/data/levels/Hospital Map/DownStairsHall.png"));
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
        setContentPane(contentP);

        
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
                JFrame gameFrame = new JFrame("Covid Zombie Apocalypse");
                JTextArea outputTextArea = new JTextArea(20,40);
                outputTextArea.setEditable(false);
                JScrollPane scroll = new JScrollPane(outputTextArea);
                gameFrame.add(scroll);
                gameFrame.pack();
                gameFrame.setVisible(false);
                try {
                    Game.AdventureGameCreator(outputTextArea);
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

       contentP.add(buttonPanel, BorderLayout.CENTER);

       
       setVisible(true);

       setSize(new Dimension(800,800));


       
    }
    
    public static void main(String[] args) throws FileNotFoundException{
        new GameWindow();
    }
}
