package adventure_game;



/*
 * Project-01: Adventure Game
 * Name: Francisco Soltero Section 2
 */

import java.util.Scanner;

import adventure_game.items.antiBiotics;
import adventure_game.items.bandage;
import adventure_game.items.AssaultRifle;
import adventure_game.items.Consumable;
import adventure_game.items.Knife;
import adventure_game.items.Pistols;
import adventure_game.items.Weapons;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
/**
 * This is the class that creates the Adventure Game
 */
public class Game {
    static Scanner in = new Scanner(System.in);
    public static Random rand = new Random();
    private static Player player;
    static ArrayList<Room> roomMap = new ArrayList<>();
    public static ArrayList<NPC> NPCS = new ArrayList<>();
    public static ArrayList<Consumable> Items = new ArrayList<>();
    public static ArrayList<Weapons> weapons = new ArrayList<>();
    
    public static void main(String[] args) throws FileNotFoundException{
        Game game = new Game();
        game.createNPCS();
        game.createItems();
        game.chooseMap();

        game.createPlayer();
        
        System.out.println(Game.player.toString());

        int choice = 0;
        Room currentRoom = roomMap.get(0);

        System.out.printf("It is a cold day in 2040. My name is %S. Its been 20 years since the zombie apocalypse, it all began when covid had it's boom the \ndoctor got the symptoms very wrong. It was originally reported as a mild to severe cold, but then came its mutation. Suddenly people began to get agressive, then hungry, finally came the zombies. First it was so far everything seemed fine, 2 years later the world was nothing \nbut a wasteland where the survivors are greatly outnumbered. 20 years later, here I am where the last reported \ngovernment found a cure but was sadly overrun with zombies. I am the worlds only hope.\n",Game.player.getName());
        System.out.printf("You are at %s\n%s",currentRoom.getRoomName(),currentRoom.getRoomBio());
        while (true){
            System.out.println("You have 4 choices\n1:North\n2:South\n3:East\n4:West\n5:Use Item\n6:Create Bandage");
            choice = Game.in.nextInt();
            switch(choice) {
                case 1:
                    if (currentRoom.isRoomNorthNull()){
                        System.out.println("You cannot go this Way");
                    }
                    else{
                        currentRoom = currentRoom.getNorthRoom();
                        System.out.println(currentRoom.ToString());
                    }
                    break;
                case 2:
                    if (currentRoom.isRoomSouthNull()){
                        System.out.println("You cannot go this way");
                    }
                    else{
                        currentRoom = currentRoom.getSouthRoom();
                        System.out.println(currentRoom.ToString());
                    }
                    break;
                case 3:
                    if (currentRoom.isRoomEastNull()){
                        System.out.println("You cannot go this way");
                    }
                    else {
                        currentRoom = currentRoom.getEastRoom();
                        System.out.println(currentRoom.ToString());
                    }
                    break;
                case 4:
                    if (currentRoom.isRoomWestNull()){
                        System.out.println("You Cannot go this way");
                    }
                    else {
                        currentRoom = currentRoom.getWestRoom();
                        System.out.println(currentRoom.ToString());
                    }
                    break;
                case 5:
                    if (Game.player.hasItems()){
                        Game.player.useItem(player, player);
                        System.out.println(currentRoom.ToString());
                    }
                    else {
                        System.out.println("You have no items.");
                    }
                    break;
                case 6:
                    Game.player.obtain(new bandage());
                    System.out.println(currentRoom.ToString());
                    break;
                default:
                    System.out.println("Invalid Input please enter 1-6");
                    System.out.println(currentRoom.ToString());
                    break;
                
                }
             if (currentRoom.hasNPC() == 1){
                int NpcChoice = rand.nextInt(12);
                System.out.println(NPCS.get(NpcChoice).toString());
                game.enterCombat(NPCS.get(NpcChoice));
                if (Game.player.isAlive()){
                    NPCS.get(NpcChoice).modifyHealth(1000);
                    int NPCdespawn = Game.rand.nextInt(2)+1;
                    if (NPCdespawn == 1){
                        System.out.printf("There are no more zombies in here for now.\n");
                        currentRoom.removeNPC();
                    }
                    if (NPCdespawn == 2){
                        System.out.printf("Theres another zombie in here. Fuck.\n");
                        currentRoom.hasNPC();
                    }
                    if (currentRoom.hasItem() == 1) {
                        System.out.printf("You have picked up an item. Look in your inventory during your next fight to see what it is.\n");
                        int randItem = Game.rand.nextInt(4);
                        Game.player.obtain(Items.get(randItem));
                        currentRoom.removeItem();
                    }
                    if (currentRoom.hasWeapon() == 1){
                        System.out.printf("You have found a weapon.\n");
                        int randWeapon = Game.rand.nextInt(5);
                        weapons.get(randWeapon).pickUpItem(player);
                        currentRoom.removeWeapon();
                    }
                }
                else {
                    if (NPCS.get(NpcChoice).getName() == "Walker"){
                        System.out.println("A walker has killed you. You were never destined to make if far. Get better.");
                    }
                    else if(NPCS.get(NpcChoice).getName() == "Creeper") {
                        System.out.print("A creeper has killed you. This is not minecraft do better");
                    }
                    else {
                        System.out.print("You died to a Sprinter. Should have ran track in highschool.");
                    }
                    break;
                }
                System.out.println(currentRoom.ToString());
                }
            if (currentRoom.hasNPC() == 4 && !currentRoom.hasCure()){
                if (currentRoom.getRoomNumber() == 10){
                    System.out.println(NPCS.get(12).toString());
                    game.enterCombat(NPCS.get(12));
                    if (Game.player.isAlive()){
                        System.out.printf("You have killed a boss. Good job you level up.\n");
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        System.out.println(currentRoom.ToString());
                    }
                    else {
                        System.out.printf("You have lost to %s. Take an L and move on.\n",NPCS.get(12).getName());
                        break;
                    }
                }
                else if (currentRoom.getRoomNumber() == 19){
                    System.out.println(NPCS.get(13).toString());
                    game.enterCombat(NPCS.get(13));
                    if (Game.player.isAlive()){
                        System.out.printf("You have killed a boss. Good job you level up.\n");
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        System.out.println(currentRoom.ToString());
                    }
                    else {
                        System.out.printf("You have lost to %s. Take an L and move on.\n",NPCS.get(13).getName());
                        break;
                    }
                }
                else {
                    System.out.println(NPCS.get(14).toString());
                    game.enterCombat(NPCS.get(14));
                    if (Game.player.isAlive()){
                        System.out.printf("You have killed a boss. Good job you level up.\n");
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        Game.player.levelModifier();
                        System.out.println(currentRoom.ToString());
                    }
                    else {
                        System.out.printf("You have lost to %s. Take an L and move on.\n",NPCS.get(14).getName());
                        break;
                    }
                }
                
            }
            if (currentRoom.hasNPC() == 4 && currentRoom.hasCure()) {
                System.out.println(NPCS.get(15).toString());
                game.enterCombat(NPCS.get(15));
                if (Game.player.isAlive()){
                    System.out.println("You have found the cure you win");
                    break;
                }
                else{
                    System.out.println("You lost");
                    break;         
                }
            }
        }
    }

    public Game() {
        
    }
    /**
     * This allows for character creation by the user
     */
    public void createPlayer(){
        /* TO-DO */
        /* Modify this method to allow the user to create their own player */
        /* The user will specify the player's name and description, and spend */
        /* 20 points on health, mana, and baseDamage as they see fit. */
        String name;
        int health;
        int damage;
        int totalPoints = 20;
        int pointsUsed;
        

        while (totalPoints > 0) {
            System.out.println("What is your name survivor? ");
            name = Game.in.next();
            System.out.println(name + ", you have 20 points to give yourself, 1 point is 10 health points, 5 damage points, and you start at level 0");
            pointsUsed = 0;
            System.out.println("How many points would you like to put in health?");
            health = Game.in.nextInt();
            pointsUsed = health;
            if (pointsUsed == 20) {
                totalPoints -= pointsUsed;
                health = 10 * health;
                player = new Player(name, health,0,0);
                continue;
            }
            else if(pointsUsed == 420){
                health = 10 * health;
                System.out.printf("You have found the legendary Joint while creating your character your health is now %s.",health);
                System.out.printf("%S has %S health. You have %S points left. \n",name,health,totalPoints);
                System.out.printf("You have %S points left. How many points would you like to put into damage? ",totalPoints);
                damage = Game.in.nextInt();
                pointsUsed = damage;
                totalPoints -= pointsUsed;
                if (totalPoints == 0) {
                    damage = 5 * damage;
                    player = new Player(name, health, 0,damage);
                    continue;
                }
                else {
                System.out.println("You have either spent too little or too many of your points. Please Try again");
                    continue;
                }
            }
            else {
                totalPoints = totalPoints - pointsUsed;
                health = 10 * health;
                System.out.printf("%S has %S health. You have %S points left. \n",name,health,totalPoints);
                System.out.printf("You have %S points left. How many points would you like to put into damage? ",totalPoints);
                damage = Game.in.nextInt();
                pointsUsed += damage;
                if (pointsUsed == 20) {
                    damage = 5 * damage;
                    player = new Player(name, health, 0,damage);
                    totalPoints -= pointsUsed;
                    continue;
                }
                else {
                    System.out.println("You have either spent too little or too many of your points. Please Try again");
                    totalPoints = 20;
                    continue;
                }
            }
            
            
            
        }
        

    }
    /**
     * This creates a combat loop between an NPC and a Character
     * The loops ends when either the NPC or Character's health hits
     * 0.
     * @param opponent of type NPC
     */
    public void enterCombat(NPC opponent){
        System.out.printf("%s has engaged in combat with %s.\n", Game.player.getName(), opponent.getName());
        while(true){
            Game.player.takeTurn(opponent);
            if(!opponent.isAlive()){
                System.out.printf("%S has been killed.\n",opponent.getName());
                Game.player.levelModifier();
                break;
            }

            opponent.takeTurn(Game.player);
            if(!Game.player.isAlive()){
                System.out.printf("%s has been killed\n",Game.player.getName());
                break;
            }
        }
    }
    /**
     * The Read Map fucntion inputs a file name and parses the file
     * looking for : to split into fileInfo to then put into rooms. Once
     * the rooms are created from the read map then the player can move on
     * to playing the game.
     * @param file of type String file
     */
    public void readMap(String file) throws FileNotFoundException {
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
                roomMap.add(room);
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
    /**
     * Allows for the player to choose a map
     * throws a fileNotFoundException
     */
    public void chooseMap() throws FileNotFoundException {
        System.out.print("You have a choice on which map you would like to play.\nPlayers are encouraged to play on Hospital.\nAnyone who dares can play Zombie Hive.\n1: The Hospital\n2: Zombie Hive(not done don't choose).\n");
        int choice = in.nextInt();
        switch(choice) {
            case 1:
                this.readMap("AdventureGame/data/levels/The-Hospital.txt");
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
                break;
            case 2:
                this.readMap("/Users/frankiesoltero/Desktop/GitHub/project-01-adventure-game-FrankieSoltero/data/levels/the-stilts.txt");
                break;
        }
        
    }
    /**
     * Creates the NPCs for the map
     */
    public void createNPCS(){
        int level = Game.rand.nextInt(15);
        NPC Walker = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(15);
        NPC Walker2 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(15);
        NPC Walker3 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(15);
        NPC Walker4 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(15);
        NPC Walker5 = new NPC("Walker",100,level,15);
        level = Game.rand.nextInt(15);
        NPC Creeper = new NPC("Creeper",250,level,20);
        level = Game.rand.nextInt(15);
        NPC Creeper2 = new NPC("Creeper",250,level,20);
        level = Game.rand.nextInt(15);
        NPC Creeper3 = new NPC("Creeper",250,level,20);
        level = Game.rand.nextInt(15);
        NPC Creeper4 = new NPC("Creeper",250,level,20);
        level = Game.rand.nextInt(15);
        NPC Sprinter = new NPC("Sprinter", 500, level, 10);
        level = Game.rand.nextInt(15);
        NPC Sprinter2 = new NPC("Sprinter", 500, level, 10);
        level = Game.rand.nextInt(15);
        NPC Sprinter3 = new NPC("Sprinter", 500, level, 10);
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
    /**
     * Creates the weapons and Items for the map
     */
    public void createItems() {
        Knife knife = new Knife();
        Knife knife2 = new Knife();
        bandage bandage = new bandage();
        Pistols pistol = new Pistols();
        Pistols pistol2 = new Pistols();
        antiBiotics antis = new antiBiotics();
        antiBiotics antis2 = new antiBiotics();
        antiBiotics antis3 = new antiBiotics();
        AssaultRifle AR = new AssaultRifle();
        Items.add(bandage);
        Items.add(antis);
        Items.add(antis2);
        Items.add(antis3);
        weapons.add(knife);
        weapons.add(knife2);
        weapons.add(pistol);
        weapons.add(AR);
        weapons.add(pistol2);
    }
}
