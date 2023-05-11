package adventure_game;
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
import java.util.Random;
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

import adventure_game.items.AssaultRifle;
import adventure_game.items.Consumable;
import adventure_game.items.Knife;
import adventure_game.items.Pistols;
import adventure_game.items.Weapons;
import adventure_game.items.antiBiotics;
import adventure_game.items.bandage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

public class GameWindow extends JFrame {
    private JFrame gameWindow;
    private JFrame battleWindow;
    private JLabel title;
    private JTextArea gameTextArea;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    private JButton attack;
    private JButton defend;
    private JButton useItem;
    private JButton createItem;
    private JButton newGame;
    private JButton loadGame;
    private Room currentRoom;
    private ArrayList<Room> roomMap = new ArrayList<>();
    private ArrayList<Consumable> Items = new ArrayList<>();
    private ArrayList<NPC> NPCS = new ArrayList<>();
    private ArrayList<Weapons> weapons = new ArrayList<>();
    public int choice = 0;
    
    private Player player = new Player("Baron",150, 0, 75);
    public static Random rand = new Random();
    


    
    
    
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
            dispose();
            try {
                newGameWindow();
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
        switch(choice){
            case 0:
                readMap("PostProject/AdventureGame/data/levels/Hospital Map/The-Hospital.txt", roomMap);
                level1();
                createNPCS1();
            case 1:
                readMap("PostProject/AdventureGame/data/levels/Hospital Map/The-Hospital.txt", roomMap);
        }
        gameWindow = new JFrame("Covid, The Zombie Apocolypse");
        currentRoom = roomMap.get(0);

        
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Sets the text area for the game
        gameTextArea = new JTextArea(20,60);
        gameTextArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(gameTextArea);

        // creation of the buttons
        button1 = new JButton("North");
        button1.addActionListener(e -> {
            if (currentRoom.isRoomNorthNull()){
                gameTextArea.append("You cannot go that way!");
            }
            else {
                currentRoom = currentRoom.getNorthRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet();
        });
        button2 = new JButton("South");
        button2.addActionListener(e -> {
            if (currentRoom.isRoomSouthNull()){
                gameTextArea.append("You cannot go that way!");
            }
            else{
                currentRoom = currentRoom.getSouthRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet();
        });
        button3 = new JButton("West");
        button3.addActionListener(e -> {
            if (currentRoom.isRoomWestNull()){
                gameTextArea.append("You cannot go that way!");
            }
            else {
                currentRoom = currentRoom.getWestRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet();
        });
        button4 = new JButton("East");
        button4.addActionListener(e -> {
            if (currentRoom.isRoomEastNull()){
                gameTextArea.append("You cannot go that way!");
            }
            else{
                currentRoom = currentRoom.getEastRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet();
        });
        button5 = new JButton("Use Bandage");
        button5.addActionListener(e -> {
            if (player.hasItems()){
                Items.get(0).consume(player, gameTextArea);
                Items.remove(0);
                gameTextArea.append(currentRoom.ToString());
            }
            else{
                gameTextArea.append("You do not have any items");
            }
        });
        button6 = new JButton("Create a Bandage");
        button6.addActionListener(e -> {
            player.obtain(new bandage());
            gameTextArea.append(currentRoom.ToString());
        });
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
        gameTextArea.append("General Should Knows\n1. Creating a bandage and using an item take a turn\n2. You can only carry 10 items at a time\n3. The game will tell you if theres another zombie and what you pick up\n4. Weapons are automatically picked up\nHave fun\n");
        gameTextArea.append(player.toString());
        gameTextArea.append(currentRoom.ToString());

        

        
    }
    public void battleWindow(NPC op){
        battleWindow = new JFrame(player.getName() + " vs " + op.getName() + "\n");
        
        gameTextArea = new JTextArea(20,60);
        gameTextArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(gameTextArea);

        attack = new JButton("Attack");
        attack.addActionListener(e -> {
            op.takeTurn(op, gameTextArea);
            player.attack(op, gameTextArea);
            if (!op.isAlive()){
                battleWindow.dispose();
            }
            gameTextArea.append("You have attacked. Your health is " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            gameTextArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        defend = new JButton("Defend");
        defend.addActionListener(e -> {
            op.takeTurn(op, gameTextArea);
            player.defend(op, gameTextArea);
            
        });
        useItem = new JButton("Use Bandage");
        useItem.addActionListener(e -> {
            if (player.hasItems()){
                Items.get(0).consume(player, gameTextArea);
                Items.remove(0);
                gameTextArea.append(currentRoom.ToString());
            }
            else{
                gameTextArea.append("You do not have any items");
            }
            op.takeTurn(op, gameTextArea);
            gameTextArea.append("You have " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            gameTextArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        createItem = new JButton("Create Bandage");
        createItem.addActionListener(e -> {
            player.obtain(new bandage());
            op.takeTurn(op, gameTextArea);
            gameTextArea.append("You have " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            gameTextArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        gameTextArea.append("You have " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
        gameTextArea.append(op.getName() + " has " + op.getHealth() + " left\n");

        Container gamePane = battleWindow.getContentPane();
        gamePane.setLayout(new BorderLayout());
        gamePane.add(scroll, BorderLayout.CENTER);

        JPanel buttonPannel = new JPanel(new GridLayout(2,3));
        buttonPannel.add(attack);
        buttonPannel.add(defend);
        buttonPannel.add(useItem);
        buttonPannel.add(createItem);
        gamePane.add(buttonPannel, BorderLayout.SOUTH);
        battleWindow.pack();
        battleWindow.setVisible(true);






    }
    public void readMap(String file, ArrayList<Room> rooms) throws FileNotFoundException {
        File f = new File(file);
        Scanner scnr = new Scanner(f);
        int count = 0;
        int numberLinesToRead = 0;
        while (scnr.hasNextLine()) {
            String line = scnr.nextLine(); 
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                ++count;
                continue;
            }
            if (count == 2) {
                numberLinesToRead = Integer.parseInt(line);
                ++count;
                continue;
            }
            if (count > 2 && count <= 2 + numberLinesToRead) {
                line = line.trim();
                String[] fileInfo = line.split(":");
                int roomNumber = Integer.parseInt(fileInfo[0]);
                String roomName = fileInfo[1];
                String roomBio = fileInfo[2];
                Room room = new Room(roomNumber, roomName, roomBio);
                rooms.add(room);
            } else if (count > numberLinesToRead + 3) {
                line.trim();
                String[] exits = line.split(":");
                int roomTag = Integer.parseInt(exits[0].trim());
                int roomEast = Integer.parseInt(exits[1].trim());
                int roomNorth = Integer.parseInt(exits[2].trim());
                int roomWest = Integer.parseInt(exits[3].trim());
                int roomSouth = Integer.parseInt(exits[4].trim());
                Room currentRoom = roomMap.get(roomTag);
                if (roomEast == -1) {
                    currentRoom.setRoomEastNull();
                } else {
                    Room eastRoom = roomMap.get(roomEast);
                    currentRoom.setEastRoom(eastRoom);
                }
                if (roomNorth == -1) {
                    currentRoom.setRoomNorthNull();
                } else {
                    Room northRoom = roomMap.get(roomNorth);
                    currentRoom.setNorthRoom(northRoom);
                }
                if (roomSouth == -1) {
                    currentRoom.setRoomSouthNull();
                } else {
                    Room southRoom = roomMap.get(roomSouth);
                    currentRoom.setSouthRoom(southRoom);
                }
                if (roomWest == -1) {
                    currentRoom.setRoomWestNull();
                } else {
                    Room westRoom = roomMap.get(roomWest);
                    currentRoom.setWestRoom(westRoom);
                }
            }
            count++;
        }
        scnr.close();
        
    }
    public void enterCombat(NPC opponent, JTextArea output){
        output.append(player.getName() + "has squared up with " + opponent.getName());
        output.append(player.toString() + " vs " + opponent.toString());
        while(true){
            player.takeTurn(opponent, output);
            if(!opponent.isAlive()){
                output.append(opponent.getName() + " has gotten fucking merked");
                
                player.levelModifier();
                break;
            }


            if(!player.isAlive()){
                output.append(player.getName() + " has gotten fucking merked");
                break;
            }
        }
    }
    public void level1(){
        int i;
        for (i = 1; i < 10; ++i){
            roomMap.get(i).setNPC();
        }
        for (i = 11; i < 19; ++i){
            roomMap.get(i).setNPC();
        }
        roomMap.get(19).setBossNPC();
        roomMap.get(22).setBossNPC();
        roomMap.get(24).setBossNPC();
        roomMap.get(24).setRoomCure();
        roomMap.get(1).setItem();
        roomMap.get(1).setWeapon();
        roomMap.get(3).setItem();
        roomMap.get(3).setWeapon();
        roomMap.get(4).setItem();
        roomMap.get(6).setWeapon();
        roomMap.get(7).setItem();
        roomMap.get(7).setWeapon();
        roomMap.get(8).setWeapon();
        roomMap.get(9).setWeapon();
        roomMap.get(10).setBossNPC();
        roomMap.get(11).setWeapon();
        roomMap.get(11).setItem();
        roomMap.get(16).setWeapon();
        roomMap.get(16).setWeapon();
        roomMap.get(15).setWeapon();
        roomMap.get(17).setWeapon();
        roomMap.get(17).setItem();
        roomMap.get(20).setNPC();
        roomMap.get(21).setNPC();

        roomMap.get(20).setWeapon();
        roomMap.get(20).setItem();
        roomMap.get(21).setWeapon();
        roomMap.get(21).setItem();
    }
    public void createItems() {
        Knife knife = new Knife();
        Knife knife2 = new Knife();
        Pistols pistol = new Pistols();
        Pistols pistol2 = new Pistols();
        AssaultRifle AR = new AssaultRifle();
        weapons.add(knife);
        weapons.add(knife2);
        weapons.add(pistol);
        weapons.add(pistol2);
        weapons.add(AR);
    }
    public void createNPCS1(){
        int level = Game.rand.nextInt(15);
        NPC Walker = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(10)+1;
        NPC Walker2 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(10)+1;
        NPC Walker3 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(10)+1;
        NPC Walker4 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(10)+1;
        NPC Walker5 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(10)+1;
        NPC Creeper = new NPC("Creeper",250,level,18);
        level = Game.rand.nextInt(10)+1;
        NPC Creeper2 = new NPC("Creeper",250,level,18);
        level = Game.rand.nextInt(10)+1;
        NPC Creeper3 = new NPC("Creeper",250,level,18);
        level = Game.rand.nextInt(10)+1;
        NPC Creeper4 = new NPC("Creeper",250,level,18);
        level = Game.rand.nextInt(5)+1;
        NPC Sprinter = new NPC("Sprinter", 500, level, 25);
        level = Game.rand.nextInt(5)+1;
        NPC Sprinter2 = new NPC("Sprinter", 500, level, 25);
        level = Game.rand.nextInt(5)+1;
        NPC Sprinter3 = new NPC("Sprinter", 500, level, 25);
        NPC HiveMind = new NPC("HiveMind",1000,6,70);
        NPC Faucci = new NPC("Faucci",20000,20,150);
        NPC ZombieNest = new NPC("Zombie Nest",5000,10,80);
        NPC RatKing = new NPC("RatKing",7500,15,100);
        NPCS.add(Walker);
        NPCS.add(Walker2);
        NPCS.add(Walker3);
        NPCS.add(Walker4);
        NPCS.add(Walker5);
        NPCS.add(Creeper);
        NPCS.add(Creeper2);
        NPCS.add(Creeper3);
        NPCS.add(Creeper4);
        NPCS.add(Sprinter);
        NPCS.add(Sprinter2);
        NPCS.add(Sprinter3);
        NPCS.add(HiveMind);
        NPCS.add(ZombieNest);
        NPCS.add(RatKing);
        NPCS.add(Faucci);
        int i;
        for (i = 0; i < NPCS.size(); ++i){
            NPCS.get(i).levelingUp();
        }
    }
    public void NPCSBattleSet(){
        if (currentRoom.hasNPC() == 1){
            int NpcChoice = rand.nextInt(12);
            gameTextArea.append(NPCS.get(NpcChoice).toString());
            enterCombat(NPCS.get(NpcChoice), gameTextArea);
            if (player.isAlive()){
                NPCS.get(NpcChoice).modifyHealth(1000);
                gameTextArea.append(NPCS.get(NpcChoice).getName() + " has died\n");
                int NPCdespawn = rand.nextInt(2)+1;
                if (NPCdespawn == 1){
                    gameTextArea.append("There are no more zombies in here for now.\n");
                    currentRoom.removeNPC();
                }
                if (NPCdespawn == 2){
                    gameTextArea.append("Theres another zombie in here. Fuck.\n");
                    currentRoom.hasNPC();
                }
                if (currentRoom.hasItem() == 1) {
                    gameTextArea.append("You have picked up an item. Look in your inventory during your next fight to see what it is.\n");
                    int randItem = rand.nextInt(4);
                    player.obtain(Items.get(randItem));
                    currentRoom.removeItem();
                }
                if (currentRoom.hasWeapon() == 1){
                    gameTextArea.append("You have found a weapon.\n");
                    int randWeapon = rand.nextInt(5);
                    weapons.get(randWeapon).pickUpItem(player);
                    currentRoom.removeWeapon();
                }
            }
            else {
                if (NPCS.get(NpcChoice).getName() == "Walker"){
                    gameTextArea.append("A walker has killed you. You were never destined to make if far. Get better.");
                }
                else if(NPCS.get(NpcChoice).getName() == "Creeper") {
                    gameTextArea.append("A creeper has killed you. This is not minecraft do better");
                }
                else {
                    gameTextArea.append("You died to a Sprinter. Should have ran track in highschool.");
                }
                dispose();
                try {
                    newGameWindow();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            System.out.println(currentRoom.ToString());
            }
        if (currentRoom.hasNPC() == 4 && !currentRoom.hasCure()){
            if (currentRoom.getRoomNumber() == 10){
                gameTextArea.append(NPCS.get(12).toString());
                enterCombat(NPCS.get(12), gameTextArea);
                if (player.isAlive()){
                    gameTextArea.append("You have killed a boss. Good job you level up.\n");
                    player.levelModifier();
                    player.levelModifier();
                    player.levelModifier();
                    gameTextArea.append(currentRoom.ToString());
                }
                else {
                    gameTextArea.append("You have lost to " + NPCS.get(12).getName() + " Get better.\n");
                    
                }
            }
            else if (currentRoom.getRoomNumber() == 19){
                gameTextArea.append(NPCS.get(13).toString());
                enterCombat(NPCS.get(13), gameTextArea);
                if (player.isAlive()){
                    gameTextArea.append("You have killed a boss. Good job you level up.\n");
                    player.levelModifier();
                    player.levelModifier();
                    player.levelModifier();
                    gameTextArea.append(currentRoom.ToString());
                }
                else {
                    gameTextArea.append("You have lost to " + NPCS.get(13).getName() + " Take an L and move on.\n");
                    
                }
            }
            else {
                gameTextArea.append(NPCS.get(14).toString());
                enterCombat(NPCS.get(14), gameTextArea);
                if (player.isAlive()){
                    gameTextArea.append("You have killed a boss. Good job you level up.\n");
                    player.levelModifier();
                    player.levelModifier();
                    player.levelModifier();
                    gameTextArea.append(currentRoom.ToString());
                }
                else {
                    gameTextArea.append("You have lost to " + NPCS.get(14).getName() + " Take an L and move on.\n");
                    
                }
            }
            
        }
        if (currentRoom.hasNPC() == 4 && currentRoom.hasCure()) {
            System.out.println(NPCS.get(15).toString());
            enterCombat(NPCS.get(15), gameTextArea);
            if (player.isAlive()){
                gameTextArea.append("You have found the cure you win");
                choice += 1;
                
            }
            else{
                gameTextArea.append("You lost");
                         
            }
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException{
       GameWindow game = new GameWindow();
       NPC npc = new NPC("Jared", 1000, 0, 15);
       game.battleWindow(npc);

        
    }
}
