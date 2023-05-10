package adventure_game;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    private Room currentRoom;
    private Room room;
    private Game game = new Game();
    private ArrayList<Room> roomMap = new ArrayList<>();
    static StringInput strInput;
    
    
    
    public GameWindow(){
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        strInput = new StringInput("null");

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
            dispose();
            try {
                newGameWindow();
                game.createPlayer(gameTextArea);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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
    private void newGameWindow() throws FileNotFoundException {
        // Creates the Frame and sets the default closer
        // operator to the red exit button.
        strInput.setStringInput("null");
        String[] options = {"The Hospital","Zombie Hive"};
        int choice = JOptionPane.showOptionDialog(this, "You have a choice on which map you would like to play.\nPlayers are encouraged to play on Hospital.\nAnyone who dares can play Zombie Hive (Not Done).", "Choose Map:", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        System.out.println(choice);
        switch(choice){
            case 0:
                game.readMap("AdventureGame/data/levels/Hospital Map/The-Hospital.txt", roomMap);
            case 1:
                game.readMap("AdventureGame/data/levels/Hospital Map/The-Hospital.txt", roomMap);
        }
        gameWindow = new JFrame("Covid, The Zombie Apocolypse");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Sets the text area for the game
        gameTextArea = new JTextArea(20,60);
        gameTextArea.setEditable(false);
        gameTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String inputText = gameTextArea.getText();
    
                    String[] lines = inputText.split("\\n");
    
                    String input = lines[lines.length - 1];
                    input.trim();
                    
                    System.out.println(input);
                    GameWindow.strInput.setStringInput(input);
                }
            }
            
        });

        JScrollPane scroll = new JScrollPane(gameTextArea);

        // creation of the buttons
        button1 = new JButton("North");
        button1.addActionListener(e -> {

        });
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
        gameTextArea.append(currentRoom.toString());
        

        
    }
    
    public static void main(String[] args) throws FileNotFoundException{
       new GameWindow();
        
    }
}
