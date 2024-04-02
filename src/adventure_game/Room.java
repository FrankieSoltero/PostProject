package adventure_game;



public class Room {
    private int roomNumber;
    private String roomName;
    private String roomBio;
    private int hasNPC = -1;
    private int hasCure = -1;
    private int hasWeapon = -1;
    private int hasItem = -1;
    private Room east;
    private Room south;
    private Room north;
    private Room west;




    /**
     * This is the Room Creator method
     * This is what allows for the game to create rooms from the map
     * 
     * @param roomNumber sets Room number to roomNumber
     * @param roomName sets Room Name to roomName
     * @param roomBio sets Room Bio to roomBio
     * 
     */ 
    public Room(int roomNumber, String roomName, String roomBio){
            this.roomNumber = roomNumber;
            this.roomName = roomName;
            this.roomBio = roomBio;
    }
    /**
     * This method allows for the room name and bio to be printed out
     * 
     * @return the room in String format
     */
    public String ToString() {
        return "\n" + this.roomName + "\n" + this.roomBio + "\n";
    }
    /**
     * This returns the room number
     * 
     * @return the room Number as an Int
     */
    public int getRoomNumber() {
        return this.roomNumber;
    }
    /**
     * this returns the room name
     * @return String room name
     */
    public String getRoomName() {
        return this.roomName;
    }
    /**
     * returns the room bio
     * @return String room bio
     */
    public String getRoomBio() {
        return this.roomBio;
    }
    /**
     * Sets the west room
     * @param westRoom of type Room
     */
    public void setWestRoom(Room westRoom){
        this.west = westRoom;
    }
     /**
     * Sets the north room
     * @param northRoom of type Room
     */
    public void setNorthRoom(Room northRoom){
        this.north = northRoom;
    }
     /**
     * Sets the east room
     * @param eastRoom of type Room
     */
    public void setEastRoom(Room eastRoom) {
        this.east = eastRoom;
    }
     /**
     * Sets the south room
     * @param southRoom of type Room
     */
    public void setSouthRoom(Room southRoom) {
        this.south = southRoom;
    }
    /**
     * returns the east room
     * 
     * @return east room
     */
    public Room getEastRoom(){
        return this.east;
    }
     /**
     * returns the south room
     * 
     * @return south room
     */
    public Room getSouthRoom() {
        return this.south;
    }
     /**
     * returns the south room
     * 
     * @return south room
     */
    public Room getNorthRoom(){
        return this.north;
    }
     /**
     * returns the south room
     * 
     * @return south room
     */
    public Room getWestRoom() {
        return this.west;
    }
    /**
     * sets the east room to null
     */
    public void setRoomEastNull() {
        this.east = null;
    }
    /**
     * sets the north room to null
     */
    public void setRoomNorthNull(){
        this.north = null;
    }
    public void setRoomSouthNull() {
        this.south = null;
    }
    /**
     * sets the west room to null
     */
    public void setRoomWestNull(){
        this.west = null;
    }
    /**
     * @return true if the east room is null
     */
    public Boolean isRoomEastNull(){
        return this.east == null;
    }
    /**
     * @return true if the south room is null
     */
    public Boolean isRoomSouthNull(){
        return this.south == null;
    }
    /**
     * @return true if the west room is null
     */
    public Boolean isRoomWestNull(){
        return this.west == null;
    }
    /**
     * @return true if the north room is null
     */
    public Boolean isRoomNorthNull(){
        return this.north == null;
    }
    /**
     * removes an NPC by setting Has NPC TO -1
     */
    public void removeNPC(){
        this.hasNPC = -1;
    }
    public void removeItem(){
        this.hasItem = -1;
    }
    public void removeWeapon(){
        this.hasWeapon = -1;
    }
    /**
     * Sets NPC by setting Has NPC to 1
     */
    public void setNPC(){
        this.hasNPC = 1;
    }
    /**
     * Sets weapon by setting hasWepon to 1
     */
    public void setWeapon(){
        this.hasWeapon = 1;
    }
    /**
     * Sets item by setting hasItem
     */
    public void setItem(){
        this.hasItem = 1;
    }
    /**
     * Sets a boss by setting HasNPC to 4
     */
    public void setBossNPC(){
        this.hasNPC = 4;
    }
    /**
     * Sets cure by setting hasCure to 1
     */
    public void setRoomCure(){
        this.hasCure = 1;
    }
    /**
     * @return True if the hasCure returns 1
     */
    public Boolean hasCure(){
        return this.hasCure == 1;
    }
    /**
     * Returns 1 if hasNPC = 1
     * Returns 4 if hasNPC = 4
     * @return The type of NPC to call
     */
    public int hasNPC(){
       if (this.hasNPC == 1){
        return 1;
       }
       else if (this.hasNPC == 4){
        return 4;
       }
       else {
        return -1;
       }
    }
    /**
     * @return 1 if it has an item else -1
     */
    public int hasItem(){
        if (this.hasItem == 1){
         return 1;
        }
        else {
            return -1;
        }
    }
    /**
     * @return 1 if it has a weapon else -1
     */
    public int hasWeapon(){
        if (this.hasWeapon == 1){
            return 1;
        }
        else {
            return -1;
        }
    }

    
}
