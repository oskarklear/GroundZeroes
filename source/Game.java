import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.time.*;
import java.util.concurrent.TimeUnit;
/**
 *  This class is the main class of the "Ground Zeroes" application. 
 *  "Ground Zeroes" is a tactical text-based espionage game where the user explores a meticulously designed map, filled with memorable locations and secrets hidden all around.
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 * 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 *  @author Oskar Klear, Michael Kölling and David J. Barnes
 *  @version 20.11.2017
 */

public class Game 
{
    private Parser parser;
    private Room currentRoom;
    private Room previousRoom;
    private ArrayList<String> itemList;
    private ArrayList<String> usedItemList;
    private boolean lose;
    private Room supplyRoom;
    private Room cells;
    private Room extraction;
    private Room fence;
    private Room road;
    private Room tents;
    private Room tower; 
    private Room cliff;
    private Room helipad;
    private Room outsideCells;
    private Room compound;
    private Room warehouse;
    private Room truck;
    private Room fence2;
    private Room adminRoom;
    private Room stairs;
    private Room fork;
    private Room teleporter;
    private Timer timer;
    private int gameTime;
    private boolean win;
    private Thread thread;
    private ArrayList<Room> existingRooms;
    private int z;
    /**
     * Creates the game and initializes the map, timer(used for steps), and the parser.
     */
    public Game() 
    {
        createRooms();
        parser = new Parser();
        timer = new Timer(0, 1);
        win = false;
        lose = false;
    }
    /**
     * Creates all the rooms and link their exits together, also gives each room its item and guard.
     * Pre: There can only be one item per room. If you try to add multiple items into a room, it will only add the last one you create.
     */
    private void createRooms()
    {
      
        // create the rooms
        fence = new Room("along the southern fence of the camp"); 
        fence.setItem("magazine");
        road = new Room("inside the camp on a road circling the perimeter of the site");
        tents = new Room("in an area with several green tents, most likely for the soldiers");
        tower = new Room("outside a guard tower looking over the camp");
        tower.setItem("magazine");
        cliff = new Room("looking over a sheer cliff");
        helipad = new Room("near an empty helipad");
        outsideCells = new Room("in the holding cells for prisoners. Our target isn't here.."); 
        outsideCells.setItem("key");
        compound = new Room("in front the central compound for the camp. There are several trucks parked around a central building");
        warehouse = new Room("outside a large warehouse, most likely for holding cargo. Our agent won't be in there..");
        fence2 = new Room("standing by a fence");
        cells = new Room("in a prisoner holding area underneath the main compound. Our agent is in here!"); cells.setItem("prisoner");
        adminRoom = new Room("in the admin room of the compound");
        stairs = new Room("peering down a staircase that descends into a dark area. Be careful, Boss");
        supplyRoom = new Room("inside a small metal building. Perhaps for storing supplies?"); 
        supplyRoom.setItem("silencer");
        truck = new Room("near a large barrack truck, probably used for transporting soldiers");
        truck.setItem("box");
        fork = new Room("in the middle the central compound, right in front of the admin building.");
        extraction = new Room("outside the camp, looks like a good place to call for extraction.");
        teleporter = new Room("in a room with a wormhole? Looks like it'll transport you somewhere!");
        
        existingRooms = new ArrayList<Room>();
        existingRooms.add(fence);
        existingRooms.add(road);
        existingRooms.add(tents);
        existingRooms.add(tower);
        existingRooms.add(cliff);
        existingRooms.add(helipad);
        existingRooms.add(outsideCells);
        existingRooms.add(compound);
        existingRooms.add(warehouse);
        existingRooms.add(fence2);
        existingRooms.add(cells);
        existingRooms.add(adminRoom);
        existingRooms.add(stairs);
        existingRooms.add(supplyRoom);
        existingRooms.add(truck);
        existingRooms.add(fork);
        existingRooms.add(extraction);
       
        setGuards();
        // initialise room exits
        fence.setExit("north", road);
        
        road.setExit("south", fence);
        road.setExit("east", tower);
        road.setExit("north", tents);
        road.setExit("west", cliff);
        
        cliff.setExit("east", road);
        
        tower.setExit("west", road);
        
        tents.setExit("north", helipad);
        tents.setExit("south", road);
        
        helipad.setExit("west", compound);
        helipad.setExit("south", tents);
        helipad.setExit("east", warehouse);
            
        warehouse.setExit("north", fence2);
        warehouse.setExit("south", supplyRoom);
        warehouse.setExit("west", helipad);
        warehouse.setExit("east", outsideCells);
        
        fence2.setExit("south", warehouse);
        fence2.setExit("east", extraction);
        
        outsideCells.setExit("west", warehouse);
        
        supplyRoom.setExit("north", warehouse);
        
        compound.setExit("east", helipad);
        compound.setExit("north", stairs);
        compound.setExit("west", fork);
        
        fork.setExit("north", adminRoom);
        fork.setExit("west", truck);
        fork.setExit("east", compound);
        
        truck.setExit("east", fork);
        truck.setExit("north", teleporter);
        
        adminRoom.setExit("south", fork);
        
        stairs.setExit("south", compound);
        stairs.setExit("west", cells);
        
        cells.setExit("east", stairs);
        
        extraction.setExit("west", fence2);
        
        currentRoom = fence;  // start game outside
        
        previousRoom = previousRoom;
        
        itemList = new ArrayList();
        itemList.add("pistol");
        itemList.add("knife");
        itemList.add("magazine");
        
        usedItemList = new ArrayList();
    }

    /**
     *  Main play routine. Loops until end of play.
     *  When "throws interrupted Exception" is in the method header, "Thread.sleep()" will be inside the method.\
     *  This simply makes the program halt execution and make the user wait after doing an action, adding more suspense into the game.
     */
    public void play() throws InterruptedException
    {            
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.      
        boolean finished = false;
        while ((! win) && (! lose)) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        if (lose){
            System.out.println("Mission failed!");
        }
        if (win){
            win();
        }
        
    }

    /**
     * Prints out the opening message for the player.
     */
    private void printWelcome()
    {
        System.out.println("   ____                        __  _                ______                           __   _____           ");
        System.out.println("  / __ \\____  ___  _________ _/ /_(_)___  ____     / ____/________  __  ______  ____/ /  /__  /  ___  _________  ___  _____");
        System.out.println(" / / / / __ \\/ _ \\/ ___/ __ `/ __/ / __ \\/ __ \\   / / __/ ___/ __ \\/ / / / __ \\/ __  /     / /  / _ \\/ ___/ __ \\/ _ \\/ ___/");
        System.out.println("/ /_/ / /_/ /  __/ /  / /_/ / /_/ / /_/ / / / /  / /_/ / /  / /_/ / /_/ / / / / /_/ /     / /__/  __/ /  / /_/ /  __(__  )  ");
        System.out.println("\\____/ .___/\\___/_/   \\__,_/\\__/_/\\____/_/ /_/   \\____/_/   \\____/\\__,_/_/ /_/\\__,_/     /____/\\___/_/   \\____/\\___/____/  ");
        System.out.println("    /_/                                                                                                                    ");

        System.out.println();
        System.out.println("                                          **OPERATION GROUND ZEROES**");
        System.out.println("                                                -MARCH 4, 1975-");
        System.out.println("                                              -----CLASSIFIED-----");
        System.out.println("                    Boss, your mission is to locate and extract our agent who was captured.");
        System.out.println("                The agent is being held at Camp Omega, a U.S military black site located in Cuba.");
        System.out.println("        This is an infiltration mission, so avoid detection at all costs or else you'll fail the mission.");
        System.out.println("        Rescue the agent and get him on the helicopter for extraction to successfully complete the mission.");
        System.out.println("                            We can only send one extraction chopper, so make it count.");
        System.out.println("                        Our agent doesn't have much time left, so time is of the essence.");
        System.out.println();
        System.out.println("                                                Good luck, Boss");
        System.out.println();
        System.out.println("Your command words are:");
        parser.showCommands();
        System.out.println();
        System.out.println(currentRoom.getLongDescription());
        if (currentRoom.hasGuard()){
            System.out.println("Boss watch out! An enemy guard is in the vicinity.");
        }
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true if the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) throws InterruptedException 
    {
        boolean wantToQuit = false;
        boolean updateTimer = true;
        if(command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        if (commandWord.equals("help")) {
            printHelp(command);
        }
        else if (commandWord.equals("go")) {
            goRoom(command);
        }
        else if (commandWord.equals("quit")) {
            wantToQuit = quit(command);
        }
        else if (commandWord.equals("use")) {
            useItem(command);
        }
        else if (commandWord.equals("items")) {
            viewItems(command);
        }
        else if (commandWord.equals("search")){
            searchRoom(command);
        }
        else if (commandWord.equals("interrogate")) {
            interrogatePerson(command);
        }
        else if (commandWord.equals("take"))
        {
            takeItem(command);
        }
        else if (commandWord.equals("extract")){
            extract(command);
        }
        else if(commandWord.equals("return")){
            goBack(command);
        }
        return true;
    }

    // implementations of user commands:
    /**
     * Print out some help information.
     * Prints the mission information and information on commands.
     */
    private void printHelp(Command command) 
    {
        if (!command.hasSecondWord()){
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------------------------");
        System.out.println("Boss, your mission is to locate and extract our agent who was captured.");
        System.out.println("The agent is being held at Camp Omega, a U.S military black site located in Cuba.");
        System.out.println("This is an infiltration mission, so avoid detection at all costs or else you'll fail the mission.");
        System.out.println("Rescue the agent and get him on the helicopter for extraction to successfully complete the mission.");
        System.out.println("We can only send one extraction chopper, so make it count.");
        System.out.println("The target doesn't have much time left, so time is of the essence.");
        System.out.println("Good luck, Boss");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.println("Your command words are:");
        parser.showCommands();
        System.out.println("You can say \"help\" + a command to get information on that command.");
        System.out.println();
       }
       if (command.hasSecondWord())
       {
           if (command.getSecondWord().equals("go"))
           {
               System.out.println("Use \"go\" + a direction to navigate the areas.");
           }
           else if (command.getSecondWord().equals("quit"))
           {
               System.out.println("Use \"quit\" to abandon the mission.");
           }
           else if (command.getSecondWord().equals("use"))
           {
               System.out.println("Use \"use\" + an item from your inventory to use that item.");
           }
           else if (command.getSecondWord().equals("take"))
           {
               System.out.println("If a room has an item in it, use \"take\" + the item you want to grab to pick up that item.");
           }
           else if (command.getSecondWord().equals("search"))
           {
               System.out.println("Use \"search\" to search a room for anything out of the ordinary, like maybe an item somewhere or a guard in the room.");
           }
           else if (command.getSecondWord().equals("interrogate"))
           {
               System.out.println("Use \"interrogate\" to interrogate a guard for intel. Be careful, the guard might fight back! If you get a successful interrogation, you kill the guard.");
           }
           else if (command.getSecondWord().equals("items"))
           {
               System.out.println("Use \"items\" to display your current items in your inventory.");
           }
           else if (command.getSecondWord().equals("extract"))
           {
               System.out.println("Use \"extract\" to call Pequod, our helicopter pilot, for extraction. You must get outside the camp before you can call for extraction.");
            }
           else if (command.getSecondWord().equals("help"))
           {
               System.out.println("Use \"help\" to radio us for intel.");
           }
           else if (command.getSecondWord().equals("return"))
           {
               System.out.println("Use \"return\" to go back to the previous room");
           }
       }
    }

    /** 
     * Used when the user inputs "go".
     * The user can traverse the map with "go" + a valid direction.
     * If the room has a guard, there is a small chance the user will be detected when trying to go onto the next room.
     * This also makes the guards "move" by removing all guards from the map and respawning them in random rooms.
     */
    private void goRoom(Command command) throws InterruptedException
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }
        String direction = command.getSecondWord();
        
        // Try to leave current room.
        Room nextRoom = currentRoom.getExit(direction);
        
        if (currentRoom.hasGuard())
        {
            int x = (int) (Math.random() * 4);
            if (!usedItemList.contains("box"))
            {   
                if (x == 1)
                {
                    System.out.println("*You crawl behind the guard*");
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("\"Enemy sighted! Opening fire!\"");
                    lose = true;
                    return;
                }
                else
                {
                    System.out.println("*You crawl behind the guard*");
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("You snuck by unnoticed!");
                }
            }
            else
            {
                System.out.println("You sneak by the guard unnoticed! You're undetectable in that cardboard box!");
                System.out.println("");
            }
        }
        if (nextRoom == null) 
        {
            System.out.println("You can't go that way!");
            return;
        }
        if (nextRoom == teleporter)
        {
            System.out.println(currentRoom.getLongDescription());
            Random x = new Random();
            currentRoom = existingRooms.get(x.nextInt(existingRooms.size()));
            System.out.println("You have been teleported!");
            System.out.println(currentRoom.getLongDescription());
            timer.updateTimer();
            return;
        }
        if (nextRoom != teleporter)
        {
            removeGuards();
            setGuards();
            previousRoom = currentRoom;
            currentRoom = nextRoom;
            System.out.println(currentRoom.getLongDescription());
            timer.updateTimer();            
        }
        if (currentRoom.hasGuard()){
            System.out.println("Boss watch out! An enemy guard is in the vicinity.");
        }
    }
    /**
     * Creates guards in random rooms. 
     */
    private void setGuards()
    {
        int ranNum;
        int numguards = 0;
        for ( z = 0; z < existingRooms.size(); z++)
        {
            ranNum = (int)(Math.random() * (5 - 1 + 1) + 1);
            if (ranNum == 2)
            {
                numguards++;
                existingRooms.get(z).setHasGuard(true);
                existingRooms.get(z).setCanInterrogate(true);
            }
        }
    }
    /**
     *  Removes the guards in all rooms.
     */
    private void removeGuards()
    {
       for ( z = 0; z < existingRooms.size(); z++)
       {
        existingRooms.get(z).setHasGuard(false);
        existingRooms.get(z).setCanInterrogate(true);  
       }
    }
    /**
     * Allows the user to go back to the previous room. 
     */
    private void goBack(Command command)
    {
        if (previousRoom == null)
        {
            System.out.println("No going back!");
        }
        else
        {
            removeGuards();
            setGuards();
            Room nextRoom = previousRoom;
            previousRoom = currentRoom;           
            currentRoom = nextRoom;
            System.out.println(currentRoom.getLongDescription());
        }
    }
    /**
     * Allows the user to search the current room, and will print out any items in the room.
     * Also includes descriptions for each item.
     */
    private void searchRoom(Command command) throws InterruptedException
    {
        System.out.println("*You search the area*");
        Thread.sleep(1000);
        System.out.println(".");
        Thread.sleep(1000);
        System.out.println("..");
        Thread.sleep(1000);
        if (currentRoom.hasItem())
        {
            String item = currentRoom.getItem();
            System.out.println("There is a " + item + " in here! ");
            if (item.equals("silencer"))
            {
                System.out.println("Boss, you can equip that silencer to your pistol to silence your shots.");
            }
            else if (item.equals("magazine"))
            {
                System.out.println("An empty ammunition magazine. Can be thrown to create a noise distraction.");
            }
            else if (item.equals("key"))
            {
                System.out.println("Maybe it goes to the cell where our agent is in?");
            }
            else if (item.equals("prisoner"))
            {
                System.out.println("Our agent is in here! Free him from the cell and extract him!");
            }
            else if (item.equals("box"))
            {
                System.out.println("There is an empty cardboard box near a parked truck. What could you possibly use that for?");
                Thread.sleep(1000);
                System.out.println("It's not like you could use that to hide from the guards or anything, right?");
            }
        }
        else
        {
            System.out.println("Nothing out of the ordinary in here...");
        }
    }
    /**
     *  Allows the user to "take" an item. 
     *  User types "take" + the item in the room to add it to their inventory. 
     *  Removes the item from the room.
     */
    private void takeItem(Command command) throws InterruptedException
    {
        if (command.hasSecondWord())
        {
            if ((currentRoom.hasItem()) && (command.getSecondWord().equals(currentRoom.getItem())))
            {
                String item = currentRoom.getItem();
                currentRoom.setHasItem(false);
                if (item.equals("prisoner")){
                    if (itemList.contains("key")){
                        System.out.println("*You use the key to open the cell*");
                        Thread.sleep(1000);
                        System.out.println("Agent secured. Bring him home, Boss.");
                        itemList.add(item);
                    }
                    else{
                        System.out.println("You need a key to unlock the cell! Try interrogating guards to locate one.");
                    }
                }
                if (!item.equals("prisoner")){
                    itemList.add(item);
                    System.out.println("Picked up a " + item + ".");
                }
            }
            else
            {
                System.out.println("There is no " + command.getSecondWord());
            }
            // else command not recognised.
            return ;
            }
    }
    /**
     *  Allows the user to check what items they have.
     */
    private void viewItems(Command command)
    {
        if(command.hasSecondWord()) {
            System.out.println("I don't understand...");
            return;
        }
        String printedItemList = itemList.toString();
        printedItemList = printedItemList.replace("[", "");
        printedItemList = printedItemList.replace("]", "");
        System.out.println("You have " + printedItemList);
    }
    /**
     *  Allows the user to "interrogate" a guard inside a room. 
     *  A random line is said by your character, and the guard will either give you a piece of information, give you nothing, or kill you.
     */
    private void interrogatePerson(Command command) throws InterruptedException
    {
        if(command.hasSecondWord()){
            System.out.println("Interrogate what?");
        }
        else if (!command.hasSecondWord()){
            if (currentRoom.getCanInterrogate()){
                Random gen = new Random();
                int x;
                int y;
                x = gen.nextInt(5);
                y = gen.nextInt(4);
                if (y == 0){
                    System.out.println("'Talk.'");
                }
                if (y == 1){
                    System.out.println("'Spit it out!'");
                }
                if (y == 2){
                    System.out.println("'Speak.'");
                }
                if (y == 3){
                    System.out.println("'Better start talking...'");
                }
                if (x == 0){
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("...");
                    Thread.sleep(1000);
                    System.out.println("Eat this!");
                    Thread.sleep(900);
                    System.out.println("*Guard pulls a knife on you and kills you*");
                    lose = true;
                }
                if (x == 1){
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("...");
                    Thread.sleep(1000);
                    System.out.println("\"The POW is northwest of here, in the main compound! He's locked in a cell north of the entrance! That's all I know I swear!\"");
                    Thread.sleep(900);
                    System.out.println("");
                    System.out.println("Boss, we know where our agent is. Go rescue him.");
                    currentRoom.setHasGuard(false);
                    currentRoom.setCanInterrogate(false);
                }
                if (x == 2)
                {
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("...");
                    Thread.sleep(1000);
                    System.out.println("\"There's a silencer in the supply room, east of the helipad and south of the warehouse! That's all I'm telling you!\"");
                    currentRoom.setHasGuard(false);
                    currentRoom.setCanInterrogate(false);
                }
                if (x == 3)
                {
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("...");
                    Thread.sleep(1000);
                    System.out.println("\"You ain't getting nothin' outta me!\"");
                    currentRoom.setHasGuard(false);
                    currentRoom.setCanInterrogate(false);                        
                }
                if (x == 4)
                {
                    Thread.sleep(1000);
                    System.out.println(".");
                    Thread.sleep(1000);
                    System.out.println("..");
                    Thread.sleep(1000);
                    System.out.println("...");
                    Thread.sleep(1000);
                    System.out.println("I've got nothing for ya! Bet you feel dumb now huh!");
                    System.out.println("");
                    Thread.sleep(1000);
                    itemList.add("key");
                    System.out.println("*The guard drops a key on the ground*");
                    System.out.println("");
                    Thread.sleep(1000);
                    System.out.println("W-wait! Time-out! Give that back!");
                    Thread.sleep(1000);
                    System.out.println("");
                    System.out.println("Boss, that key might go to the cell they're keeping our agent in.");
                    outsideCells.setHasItem(false);
                }
            }
            else{
                System.out.println("Nobody to interrogate");
            }
        }
        
        
    }
    /**
     *  The win command, so to speak. When the user has the prisoner in their inventory, and is in one of the specified areas, "extract" will trigger the win method.
     */
    private boolean extract(Command command)
    {
        if (!itemList.contains("prisoner"))
        {
            System.out.println("Boss, you need to grab the prisoner before you can extract.");
        }
        if (itemList.contains("prisoner") && (currentRoom.getShortDescription().equals("along the southern fence of the camp") || currentRoom.getShortDescription().equals("outside the camp, looks like a good place to call for extraction.")))
        {
            System.out.println("This is Pequod, arriving shortly at LZ.");
            win = true;
            return true;
        }
        else
        {
            System.out.println("Boss, you can't do that here. You'll put our chopper in danger.");
        }
        return true;
    }
    /**
     *  Invoked when the user completes the objective and wins the game. 
     *  The number of moves is read and an appropriate rank is given based off the number of moves.
     *  The repeated System.out.println("-") and the Thread.sleep is to create an animation of sorts
     *  at the win screen.
     */        
    private boolean win() throws InterruptedException
    {
        win = true;
        Thread.sleep(2000);
        System.out.println("You extracted the target! Mission complete, Boss!");
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        System.out.print("-");
        Thread.sleep(10);
        do
        {
            int playtime = timer.getTime(); //playtime is the number of moves.
            if (playtime <= 16)
            {
                System.out.println("");
                System.out.println("RANK: ");
                Thread.sleep(2000);
                System.out.print("S");
            }
            else if(playtime <= 26)
            {
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.print("A");
            }
            else if(playtime <= 36)
            {
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.print("B");
            }
            else if(playtime <= 46)
            {
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.print("C");
            }
            else if(playtime <= 56)
            {
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.print("D");
            }
            else if(playtime <= 66)
            {
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.print("E");
            }
            else if(playtime >= 67)
            {
                System.out.print("-");
                Thread.sleep(10);
                System.out.println("");
                System.out.print("RANK: ");
                Thread.sleep(2000);
                System.out.println("F");
            }
            return true;
        }
        while (!lose);
    }
    /**
     *  Allows the user to use an item in their inventory specified by the parameter.
     *  Each item is used differently, some items are thrown so they are removed from inventory after use, and some are not so they remain after each use.
     */
    private boolean useItem(Command command) throws InterruptedException
    {
       if (!command.hasSecondWord())
       {
           System.out.println("Specify an item to use.");
           return false;
       }
       String itemUsed = command.getSecondWord();
        if (itemList.contains(itemUsed))
        {
            if (itemUsed.equals("pistol"))
            {
                if (!usedItemList.contains("silencer"))
                {
                    System.out.println("Boss, you've alerted the guards of your presence! Mission Failed!");
                    lose = true;
                    return true;
                }
                if (currentRoom.hasGuard())
                {
                    System.out.println("Enemy down. Nice shot, Boss.");
                    currentRoom.setHasGuard(false);
                    currentRoom.setCanInterrogate(false);
                }
                else
                {
                    System.out.println("You shoot at nothing.");
                }
            }
            if (itemUsed.equals("knife"))
            {
                if (!currentRoom.hasGuard())
                {
                    System.out.println("You slash at the air.");
                }
                else //There is a chance you will lose the knife battle.
                {
                    Random gen = new Random();
                    int x = gen.nextInt(3);
                    if (x >= 1)
                    {
                        currentRoom.setHasGuard(false);
                        currentRoom.setCanInterrogate(false);
                        System.out.println("You've taken out a guard, keep going Boss.");
                    }
                    else
                    {
                        System.out.println("*The guard shoots you and kills you*");
                        System.out.println("Boss, talk to me! Don't you die on me!");
                        lose = true;
                    }
                }
            }
            if (itemUsed.equals("silencer"))
            {
                if (usedItemList.contains("silencer"))
                {
                    System.out.println("You have attached the silencer to your pistol.");
                }
                else
                {
                    System.out.println("You've equipped a silencer onto your pistol. The guards won't be able to hear your shots now.");
                    usedItemList.add("silencer");
                    itemList.remove("silencer");
                }
            }
            if (itemUsed.equals("magazine"))
            {
                currentRoom.setHasGuard(false);
                currentRoom.setCanInterrogate(false);
                System.out.println("*Chucked a magazine into a different room*");
                System.out.println("The coast is clear");
                
                Room dummyroom = new Room("");
                Random x = new Random();
                dummyroom = existingRooms.get(x.nextInt(existingRooms.size()));
                dummyroom.setItem("magazine");
                
                usedItemList.add("magazine");
                itemList.remove("magazine");
            }
            if (itemUsed.equals("box"))
            {
                System.out.println("*You put the cardboard box on top of you.*");
                System.out.println("Boss what are you doing? You think the guards won't notice a moving cardboard box?");
                itemList.remove("box");
                usedItemList.add("box");
            }
       }
       else
       {
           System.out.println("You don't have a " + itemUsed);
       }
       return false;
    }
    /** 
     * If the user inputs "quit", the game will lose, and print out a losing screen. No ranks are awarded.
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command) 
    {
        if(command.hasSecondWord()) 
        {
            System.out.println("Quit what?");
            return false;
        }
        else 
        {
            System.out.println("You've abandoned the mission, Boss.");
            lose = true;
            return true;  // signal that we want to quit
        }
    }
}

