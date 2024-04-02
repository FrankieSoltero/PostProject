package adventure_game;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import adventure_game.items.AssaultRifle;
import adventure_game.items.Knife;
import adventure_game.items.Pistols;
import adventure_game.items.Weapons;
import adventure_game.items.bandage;

public class GameWindow extends JFrame {
    private JFrame gameWindow;
    private JFrame battleWindow;
    private JTextArea gameTextArea;
    private JTextArea textArea;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    private JButton button7;
    private JButton loadGame;
    private JButton attack;
    private JButton defend;
    private JButton useItem;
    private JButton createItem;
    private JButton newGame;
    public Room currentRoom;
    private ArrayList<Room> roomMap = new ArrayList<>();
    private ArrayList<NPC> NPCS = new ArrayList<>();
    private HashMap<String,NPC> Bosses = new HashMap<>();
    private ArrayList<Weapons> weapons = new ArrayList<>();
    public static int choice = 0;
    public int NpcChoice;
    public static Player savedPlayer;
    public static Room savedRoom;
    
    public static Player player = new Player("Mick",150, 0, 75);
    public static Random rand = new Random();
    private ImageIcon room0 = new ImageIcon("PostProject/AdventureGame/data/levels/Hospital Map/Room0LVL1.png");
    private JLabel room0Label = new JLabel();
    private JPanel imagePanel = new JPanel();
    


    
    
    
    public GameWindow(){
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Lines 47-62 Create the Image background then apply it to the starter GameWindow
        JPanel panel = new JPanel() {
            private Image backgroundImage;
            
            // Use a method to load the image to ensure it is loaded before paintComponent is called
            private void loadImage() {
                try {
                    backgroundImage = ImageIO.read(new File("PostProject/AdventureGame/data/levels/Hospital Map/HE.png"));
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
        loadGame = new JButton("Load Game");
        loadGame.addActionListener(e -> {
            dispose();
            try {
                nextLevel();
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                File save = new File("AdventureGame/src/adventure_game/SaveFile.txt");
                Scanner E = new Scanner(save);
                if (save.length() > 0){
                    if(E.hasNextLine()){
                        String line = E.nextLine();
                        line.trim();
                        String[] savedFileInfo = line.split(":");
                        int playerLevel = Integer.parseInt(savedFileInfo[0]) * 3;
                        int itemCount = Integer.parseInt(savedFileInfo[2]);
                        for (int i = 0; i <= playerLevel; i++){
                                GameWindow.player.levelModifier(null);
                         }
                        int savedRoom = Integer.parseInt(savedFileInfo[1]);
                        for (int i = 0; i <= itemCount; ++i ){
                             GameWindow.player.obtain(new bandage(),null);
                        }
                        currentRoom = roomMap.get(savedRoom);
                    }
                    else {
                        currentRoom = roomMap.get(0);
                    }
                }
                E.close();
                mainGameWindow();
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        newGame.addActionListener(e -> {
            dispose();
            try {
                nextLevel();
                mainGameWindow();
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        
        setLayout(new GridBagLayout());
        setSize(400, 400);
        //This sets the buttons to the middle top of the window no matter what
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 0;
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;

        panel.add(newGame, buttonConstraints);
        panel.add(loadGame, buttonConstraints);
        //buttonConstraints.gridy = 1;
        //add(loadGame,buttonConstraints);
        setContentPane(panel);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
       
    }
    private void scaleImage(){
        int imageWidth = room0Label.getWidth();
        int imageHeight = room0Label.getHeight();

        if (imageWidth > 0 && imageHeight > 0){
            Image image = room0.getImage();
            Image scaledImage = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
            ImageIcon room0Scaled = new ImageIcon(scaledImage);
            room0Label.setIcon(room0Scaled);
        }
    }
    public void mainGameWindow() throws FileNotFoundException {
        // Creates the Frame and sets the default closer
        // operator to the red exit button.
        gameWindow = new JFrame("Covid, The Zombie Apocolypse");

        /*room0Label.setIcon(room0);
        imagePanel.setLayout(new BorderLayout());

        imagePanel.add(room0Label, BorderLayout.NORTH);

        room0Label.setHorizontalAlignment(JLabel.CENTER);
        */


        
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Sets the text area for the game
        gameTextArea = new JTextArea(20,60);
        gameTextArea.setLineWrap(true);
        gameTextArea.setWrapStyleWord(true);
        gameTextArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(gameTextArea);
        gameWindow.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                scaleImage();
            }
        });

        // creation of the buttons
        button1 = new JButton("North");
        button1.addActionListener(e -> {
            if (currentRoom.isRoomNorthNull()){
                gameTextArea.append("You cannot go that way!\n");
            }
            else {
                currentRoom = currentRoom.getNorthRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet(gameTextArea);
        });
        button2 = new JButton("South");
        button2.addActionListener(e -> {
            if (currentRoom.isRoomSouthNull()){
                gameTextArea.append("You cannot go that way!\n");
            }
            else{
                currentRoom = currentRoom.getSouthRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet(gameTextArea);
        });
        button3 = new JButton("West");
        button3.addActionListener(e -> {
            if (currentRoom.isRoomWestNull()){
                gameTextArea.append("You cannot go that way!\n");
            }
            else {
                currentRoom = currentRoom.getWestRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet(gameTextArea);
        });
        button4 = new JButton("East");
        button4.addActionListener(e -> {
            if (currentRoom.isRoomEastNull()){
                gameTextArea.append("You cannot go that way!\n");
            }
            else{
                currentRoom = currentRoom.getEastRoom();
                gameTextArea.append(currentRoom.ToString());
            }
            NPCSBattleSet(gameTextArea);
        });
        button5 = new JButton("Use Bandage");
        button5.addActionListener(e -> {
            if (player.hasItems()){
                player.items.get(0).consume(player, gameTextArea);
                player.items.remove(0);
            }
            else{
                gameTextArea.append("You do not have any items");
            }
            NPCSBattleSet(gameTextArea);
        });
        button6 = new JButton("Create a Bandage");
        button6.addActionListener(e -> {
            player.obtain(new bandage(),gameTextArea);
            NPCSBattleSet(gameTextArea);
        });
        button7 = new JButton("Save Game");
        button7.addActionListener(e -> {
            GameWindow.saveGame(currentRoom, player);
            gameTextArea.append("Your Game has been Saved\n");
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
        buttonPannel.add(button7);

        gamePane.add(buttonPannel, BorderLayout.SOUTH);
        gamePane.add(imagePanel, BorderLayout.NORTH);

        if (choice == 0){
            gameTextArea.append("General Should Knows\n1. Creating a bandage and using an item take a turn\n2. You can only carry 10 items at a time\n3. The game will tell you if theres another zombie and what you pick up\n4. Weapons are automatically picked up\n5. NPCS level randomly generate for each one\n6. Defending only stops damage for that turn. The NPC is then vulnerable for the next turn but can still hit you\n7. A message will pop up telling you if theres another zombie in the room.\n8. At level 10 bandages begin to heal up to 200 points\nHappy Fighting survivor.\n\n");
        }
        gameWindow.pack();
        gameWindow.setVisible(true);
        gameTextArea.append(currentRoom.ToString());


        

        
    }
    public void battleWindow(NPC op){
        battleWindow = new JFrame(player.getName() + " vs " + op.getName() + "\n");
        battleWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea(20,60);
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);

        attack = new JButton("Attack");
        attack.addActionListener(e -> {
            player.attack(op, textArea);
            op.takeTurn(player, textArea);
            if (!player.isAlive()) {
                battleWindow.dispose();
            }
            if (op.getHealth() <= 1){
                battleWindow.dispose();
            }
            textArea.append("Your health is " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            textArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        defend = new JButton("Defend");
        defend.addActionListener(e -> {
            op.takeTurn(player, textArea);
            player.defend(op, textArea);
            if (!player.isAlive()) {
                battleWindow.dispose();
            }
            if (op.getHealth() <= 1){
                battleWindow.dispose();
            }
            textArea.append("Your health is " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            textArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        useItem = new JButton("Use Bandage");
        useItem.addActionListener(e -> {
            if (player.hasItems()){
                player.items.get(0).consume(player, textArea);
                player.items.remove(0);
                op.takeTurn(player, textArea);
            }
            else{
                textArea.append("You do not have any items. Choose another option");
            }
            if (!player.isAlive()) {
                battleWindow.dispose();
            }
            if (op.getHealth() <= 1){
                battleWindow.dispose();
            }
            textArea.append("You have " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            textArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });
        createItem = new JButton("Create Bandage");
        createItem.addActionListener(e -> {
            player.obtain(new bandage(), textArea);
            op.takeTurn(player, textArea);
            if (!player.isAlive()) {
                battleWindow.dispose();
            }
            if (op.getHealth() <= 1){
                battleWindow.dispose();
            }
            textArea.append("You have " + player.getHealth() + "/" + player.getMaxHealth() + "\n");
            textArea.append(op.getName() + " has " + op.getHealth() + " left\n");
        });

        battleWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (currentRoom.hasNPC() == 1){
                    if (player.isAlive()){
                        player.levelModifier(gameTextArea);
                        NPCS.get(NpcChoice).modifyHealth(1000);
                        gameTextArea.append(NPCS.get(NpcChoice).getName() + " has died\n");
                        int NPCdespawn = rand.nextInt(10)+1;
                        if (NPCdespawn <= 5){
                            gameTextArea.append("There are no more zombies in here for now.\n");
                            currentRoom.removeNPC();
                        }
                        if (NPCdespawn >= 6){
                            gameTextArea.append("Theres another zombie in here. Fuck.\n");
                            currentRoom.hasNPC();
                        }
                        if (currentRoom.hasItem() == 1) {
                            gameTextArea.append("You have picked up a bandage.\n");
                            player.obtain(new bandage(), gameTextArea);
                            currentRoom.removeItem();
                        }
                        if (currentRoom.hasWeapon() == 1){
                            gameTextArea.append("You have found a weapon.\n");
                            int randWeapon = rand.nextInt(5);
                            weapons.get(randWeapon).pickUpItem(player, gameTextArea);
                            currentRoom.removeWeapon();
                        }
                    }
                    else{
                        if (NPCS.get(NpcChoice).getName() == "Walker"){
                            gameTextArea.append("A walker has killed you. You were never destined to make if far. Get better.");
                        }
                        else if(NPCS.get(NpcChoice).getName() == "Creeper") {
                            gameTextArea.append("A creeper has killed you. This is not minecraft do better");
                        }
                        else {
                            gameTextArea.append("You died to a Sprinter. Should have ran track in highschool.");
                        }
                        gameWindow.dispose();
                        player.modifyHealth(999999999);
                        new GameWindow();
                        }
                            
                    }
                if (currentRoom.hasNPC() == 4 && !currentRoom.hasCure()){
                    if (choice == 0){
                        if (player.isAlive()){
                            currentRoom.removeNPC();
                            textArea.append("You have killed Hivemind and as he falls you notice theres no cure to be found, but there is a large manhole. You look around and find a message on a computer nearby and realize the doctor with the cure attempted to escape into the manhole. Pissed off you enter the manhole and go to the sewers.\n");
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            try {
                                Thread.sleep(15000);
                            } catch (InterruptedException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            try {
                                gameWindow.dispose();
                                nextLevel();
                                mainGameWindow();
                            } catch (FileNotFoundException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                        else {
                            gameTextArea.append("You have lost to HiveMind. Get better.\n");
                            player.modifyHealth(999999999);
                            gameWindow.dispose();
                            new GameWindow();
                            
                        }
                    }
                    if (choice == 1){
                        if (player.isAlive()){
                            currentRoom.removeNPC();
                            gameTextArea.append("You have killed a boss. Good job you level up.\n");
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            gameTextArea.append(currentRoom.ToString());
                        }
                        else {
                            gameTextArea.append("You have lost to Zombie Nest. Take an L and move on.\n");
                            gameWindow.dispose();
                            player.modifyHealth(999999999);
                            new GameWindow();
                            
                        }
                    }
                    if(choice == 2) {
                        gameTextArea.append(NPCS.get(14).toString());
                        enterCombat(NPCS.get(14), gameTextArea);
                        if (player.isAlive()){
                            currentRoom.removeNPC();
                            gameTextArea.append("You have killed a boss. Good job you level up.\n");
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            player.levelModifier(gameTextArea);
                            gameTextArea.append(currentRoom.ToString());
                        }
                        else {
                            gameTextArea.append("You have lost to Rat King. Take an L and move on.\n");
                            gameWindow.dispose();
                            player.modifyHealth(999999999);
                            new GameWindow();
                            
                        }
                    }
                    
                }
                if (currentRoom.hasNPC() == 4 && currentRoom.hasCure()) {
                    if (player.isAlive()){
                        currentRoom.removeNPC();
                        gameTextArea.append("You have found the cure you win");
                        gameWindow.dispose();

                        new GameWindow();
                        
                    }
                    else{
                        gameTextArea.append("You lost");
                        new GameWindow();
                                 
                    }
                }
            }
        });
        textArea.append(player.toString());
        textArea.append(op.toString());
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
        currentRoom = roomMap.get(0);
        
    }
    public void enterCombat(NPC opponent, JTextArea output){
        output.append(player.getName() + "has squared up with " + opponent.getName());
        output.append(player.toString() + " vs " + opponent.toString());
        while(true){
            player.takeTurn(opponent, output);
            if(!opponent.isAlive()){
                output.append(opponent.getName() + " has gotten fucking merked");
                if (opponent.getLevel() > player.getLevel() + 2) {
                    player.levelModifier(output);
                    player.levelModifier(output);
                }
                player.levelModifier(output);
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
    }
    public void level2(){
        int i;
        for(i = 1; i < 14; ++i){
            roomMap.get(i).setNPC();
        }
        roomMap.get(5).setWeapon();
        roomMap.get(6).setWeapon();
        roomMap.get(6).setItem();
        roomMap.get(10).setWeapon();
        roomMap.get(11).setWeapon();
        roomMap.get(13).setWeapon();
        roomMap.get(14).setBossNPC();
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
    public void createNPCS(){
        int level = rand.nextInt(16);
        NPC Walker = new NPC("Walker",100,level,15);
        level = rand.nextInt(16);
        NPC Walker2 = new NPC("Walker",100,level,15);
        level = rand.nextInt(16);
        NPC Walker3 = new NPC("Walker",100,level,15);
        level = rand.nextInt(16);
        NPC Walker4 = new NPC("Walker",100,level,15);
        level = rand.nextInt(16);
        NPC Walker5 = new NPC("Walker",100,level,15);
        level = rand.nextInt(11);
        NPC Creeper = new NPC("Creeper",250,level,18);
        level = rand.nextInt(11);
        NPC Creeper2 = new NPC("Creeper",250,level,18);
        level = rand.nextInt(11);
        NPC Creeper3 = new NPC("Creeper",250,level,18);
        level = rand.nextInt(11);
        NPC Creeper4 = new NPC("Creeper",250,level,18);
        level = rand.nextInt(6);
        NPC Sprinter = new NPC("Sprinter", 500, level, 25);
        level = rand.nextInt(6);
        NPC Sprinter2 = new NPC("Sprinter", 500, level, 25);
        level = rand.nextInt(6);
        NPC Sprinter3 = new NPC("Sprinter", 500, level, 25);
        NPC HiveMind = new NPC("HiveMind",1000,5,30);
        NPC Faucci = new NPC("Faucci",3000,20,50);
        NPC ZombieNest = new NPC("Zombie Nest",2000,15,50);
        NPC RatKing = new NPC("RatKing",1500,10,50);
        //We add All the NPCS Here
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

        //We Add the bosses to a hash map to be able to grab them each by name and level them
        //up individually here as well
        Bosses.put("Hivemind", HiveMind);
        HiveMind.levelingUp(null);
        Bosses.put("Faucci",Faucci);
        Faucci.levelingUp(null);
        Bosses.put("ZombieNest", ZombieNest);
        ZombieNest.levelingUp(null);
        Bosses.put("RatKing", RatKing);
        RatKing.levelingUp(null);
        int i;
        for (i = 0; i < NPCS.size(); ++i){
            NPCS.get(i).levelingUp(null);
        }
    }
    /*
     * This function pulls an NPCS from NPCS if the room number is 1 and calls the battleWindow 
     * If the number is 4 it is a boss room and a boss NPC is pulled based on the room number
     */
    public void NPCSBattleSet(JTextArea gameTextArea){
        //If the current room has an NPC we grab a random NPC
        if (currentRoom.hasNPC() == 1){
            NpcChoice = rand.nextInt(12);
            battleWindow(NPCS.get(NpcChoice));
            }
        if (currentRoom.hasNPC() == 4 && !currentRoom.hasCure()){
            if (choice == 0){
                battleWindow(Bosses.get("Hivemind"));
            }
            if (choice == 1){
                battleWindow(Bosses.get("Zombienest"));
            }
            if (choice == 2) {
                battleWindow(Bosses.get("RatKing"));
            }
            
        }
        if (currentRoom.hasNPC() == 4 && currentRoom.hasCure()) {
            battleWindow(Bosses.get("Faucci"));
        }
    }
    public static void saveGame(Room room, Player player) {
            String saveFileName = "AdventureGame/src/adventure_game/SaveFile.txt";
            int savedRoom = room.getRoomNumber();
            int savedPlayerLevel = player.getLevel();
            try {
                // Creates File Writer
                FileWriter writer = new FileWriter(saveFileName);
                writer.write(Integer.toString(savedPlayerLevel));
                writer.write(":");
                writer.write(Integer.toString(savedRoom));
                writer.write(":");
                writer.write(Integer.toString(player.items.size()));
                writer.write(":");
                writer.write(Integer.toString(choice));
                writer.write("\n");
                writer.close();
                System.out.print("Room Saved");

            } catch (IOException e) {
                e.printStackTrace();
            }  
            
    }
    public void nextLevel() throws FileNotFoundException{
        choice += 1;
        switch(choice){
            case 0:
                readMap("AdventureGame/data/levels/Hospital Map/The-Hospital.txt", roomMap);
                level1();
                createNPCS();
                createItems();
            case 1:
                readMap("PostProject/AdventureGame/data/levels/Hospital Map/The-Sewers.txt",roomMap);
                createItems();
                createNPCS();
                level2();
           /*  case 2:
                //Level 3 goes here   
            // readMap("")
            case 3:
                //Level 4 goes here so on and so forth
            */    
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException{
        new GameWindow();

        
    }
}
