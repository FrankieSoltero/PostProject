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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

public class GameWindow extends JFrame {
    private JFrame gameWindow;
    private JLabel title;
    private JTextArea gameTextArea;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    private JButton newGame;
    private JButton loadGame;
    
    
    public GameWindow(){
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Lines 47-62 Create the Image background then apply it to the starter GameWindow
        JPanel panel = new JPanel() {
            private Image backgroundImage;
            
            // Use a method to load the image to ensure it is loaded before paintComponent is called
            private void loadImage() {
                try {
                    backgroundImage = ImageIO.read(new File("AdventureGame/data/levels/Hospital Map/HE.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // Override paintComponent to draw the image as the background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage == null) {
                    loadImage();
                }
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        
        newGame = new JButton("New Game");
        newGame.addActionListener(e -> {
            newGameWindow();
        });
        //loadGame = new JButton("Load Game");
        
        setLayout(new GridBagLayout());

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 0;
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;

        panel.add(newGame, buttonConstraints);
        //buttonConstraints.gridy = 1;
        //add(loadGame,buttonConstraints);
        setContentPane(panel);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
       
    }
    private void newGameWindow() {
        // Creates the Frame and sets the default closer
        // operator to the red exit button.
        gameWindow = new JFrame("Covid, The Zombie Apocolypse");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Sets the text area for the game
        gameTextArea = new JTextArea(20,60);
        gameTextArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(gameTextArea);

        // creation of the buttons
        button1 = new JButton("North");
        button2 = new JButton("South");
        button3 = new JButton("West");
        button4 = new JButton("East");
        button5 = new JButton("Open Inventory");
        button6 = new JButton("Create a Bandage");
        // Add components to the gameWindow
        Container gamePane = gameWindow.getContentPane();
        gamePane.setLayout(new BorderLayout());
        gamePane.add(scroll, BorderLayout.CENTER);

        JPanel buttonPannel = new JPanel(new GridLayout(2,3));
        buttonPannel.add(button1);
        buttonPannel.add(button2);
        buttonPannel.add(button3);
        buttonPannel.add(button4);
        buttonPannel.add(button5);
        buttonPannel.add(button6);

        gamePane.add(buttonPannel, BorderLayout.SOUTH);

        gameWindow.pack();
        gameWindow.setVisible(true);
    }
    
    public static void main(String[] args) throws FileNotFoundException{
       new GameWindow();
        
    }
}
