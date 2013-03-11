import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import javax.swing.ImageIcon;

public class RPG extends Applet implements Runnable, MouseListener, KeyListener {
    
    private static final long serialVersionUID = 1L;
    
    long time;
    int fps;
    int appWidth, appHeight;
    Thread t;
    Player player;
    double interactionDistance;
    Image offscreenImage;
    int tileSize;
    Graphics offscr;
    //static ArrayList<Solid> allSolids = new ArrayList<Solid>();
    //ArrayList<Overlay> allOverlays = new ArrayList<Overlay>();
    Image imgDialogBG, imgInventoryBG, imgItemSelected, imgLoading;
    MessageOverlay currentMessage;
    Font font;
    boolean paused;
    boolean loaded;
    String[] gameData;
    String[][] solidsData, levelsData;
    Hashtable<String, Solid> solidDefs = new Hashtable<String, Solid>();
    Set<String> solidKeys;
    boolean inventoryOpen;
    int inventoryTimer;
    int viewX, viewY;
    int viewDistFromPlayerX, viewDistFromPlayerY;
    int numLevels;
    static Level currentLevel;
    ArrayList<Level> levels;
    
    public void init() {
        t = new Thread(this);
        t.start();
        
        //open the files
        gameData = readFile("data/game/game.txt");
        //gameData = gameText.split("\\r?\\n");
        
        //begin parsing the game data
        int line = 0;
        numLevels = getIntFromString(gameData[line++], "numLevels = ");
        fps = getIntFromString(gameData[line++], "fps = ");
        appWidth = getIntFromString(gameData[line++], "appWidth = ");
        appHeight = getIntFromString(gameData[line++], "appHeight = ");
        interactionDistance = getDoubleFromString(gameData[line++], "interactionDistance = ");
        tileSize = getIntFromString(gameData[line++], "tileSize = ");
        imgDialogBG = getImageFromString(gameData[line++], "imgDialogBG = ");
        imgInventoryBG = getImageFromString(gameData[line++], "imgInventoryBG = ");
        imgItemSelected = getImageFromString(gameData[line++], "imgItemSelected = ");
        imgLoading = getImageFromString(gameData[line++], "imgLoading = ");
        viewDistFromPlayerX = getIntFromString(gameData[line++], "viewDistFromPlayerX = ");
        viewDistFromPlayerY = getIntFromString(gameData[line++], "viewDistFromPlayerY = ");
        font = getFontFromString(gameData[line++], "font = ");
        //done parsing the game data
        
        solidsData = new String[numLevels][];
        levelsData = new String[numLevels][];
        
        for(int i = 0; i < numLevels; i++) {
            //levelTexts.add(i, readFile("data/game/level" + i + ".txt"));
            //solidsTexts.add(i, readFile("data/game/solids" + i + ".txt"));
            //solidsData[i] = solidsTexts.get(i).split("\\r?\\n");
            //levelsData[i] = levelTexts.get(i).split("\\r?\\n");
            levelsData[i] = readFile("data/game/level" + i + ".txt");
            solidsData[i] = readFile("data/game/solids" + i + ".txt");
        }
        
        //do some game initialization stuff
        time = 0;
        paused = false;
        inventoryOpen = false;
        inventoryTimer = -1;
        viewX = viewY = 0;
        setSize(appWidth, appHeight);
        
        //Button titleButton = new Button(titleImage, appWidth / 2 - titleImage.getWidth(null) / 2, 100);
        //Button startButton = new Button(startButtonImage, appWidth / 2 - titleImage.getWidth(null) / 2, 500);
        //Button[] titleBList = {titleButton, startButton};
        //titleScreen = new Level(titleBList);
        //done with the game initialization stuff
        
        
        levels = new ArrayList<Level>();
        for(int i = 0; i < numLevels; i++) {
            
            Level level = new Level(levelsData[i], this);
            //make the objects
            //all objects made at 0, 0, moved during next step
            level.learnSolids(solidsData[i], this);
            //do the same for overlays
            
            level.createObjects(this);
            //do the same for overlays
            
            //Image imgFog = getImage(getCodeBase(), "images/fog.png");
            //Overlay fog = new Overlay(imgFog, 0, 0);
            //allOverlays.add(fog);
            
            levels.add(level);
        }
        currentLevel = levels.get(0);
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        
        addMouseListener(this);
        addKeyListener(this);
    }
    
    public void space() {
        if(player == null) {
            return;
        }
        if(paused) {
            if(inventoryOpen) {
                paused = false;
                inventoryOpen = false;
            }
            else if(!currentMessage.equals(null)) {
                if(currentMessage.isLast()) {
                    paused = false;
                }
                currentMessage = currentMessage.nextMessage;
            }
        }
        else {
            interact();
        }
    }
    
    public void nextLevel() {
        int nextLevel = levels.indexOf(currentLevel) + 1;
        if(nextLevel >= levels.size()) {
            System.out.println("Cannot move to room after last room");
            System.exit(1);
        }
        currentLevel.bgMusic.stop();
        currentLevel = levels.get(nextLevel);
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        currentLevel.bgMusic.loop();
    }
    
    public MessageOverlay createMessageOverlay(Image portrait, String message, MessageOverlay nextMessage){
        //uses some default stuff to make things easier
        return new MessageOverlay(imgDialogBG, portrait, 0, appHeight - new ImageIcon(imgDialogBG).getIconHeight(), message, nextMessage);
    }
    
    public void interact() {
        //check if there is a SceneryInteractable or NPC near the player
        //if there is, set currentMessage to its messageOverlay and pause the game, so that dialogue can occur
        Solid s;
        SceneryInteractable interactable;
        for(int i = 0; i < currentLevel.allSolids.size(); i++) {
            s = currentLevel.allSolids.get(i);
            if(s instanceof SceneryInteractable) {
                interactable = (SceneryInteractable)s;
                if(player.distanceTo(interactable) < interactionDistance) {
                    paused = true;
                    currentMessage = interactable.overlay;
                    return;
                }
            }
        }
        
        //use the player's current item (only usable items are weapons right now)
        if(player.inventoryItem != null && player.inventoryItem.isWeapon && time - player.lastShot > ((Weapon)player.inventoryItem).rate) {
            ((Weapon)player.inventoryItem).fire(player);
            currentLevel.allSolids.add(player.attack);
            player.lastShot = time;
        }
    }
    
    public static String[] readFile(String fileName) {
        Scanner reader;
        try {
            reader = new Scanner(new File(fileName));
        }
        catch(FileNotFoundException fnfe) {
            System.out.println("Error: Could not read file " + fileName);
            return new String[0];
        }
        
        reader.useDelimiter("\n");
        String data = "";
        while(reader.hasNext()) {
            data += reader.next() + "\n";
        }
        
        return data.split("\\r?\\n");
    }
    
    public Image getImageFromString(String str, String start) {
        if(str.startsWith(start)) {
            return getImage(getCodeBase(), str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to null");
        return null;
    }
    
    public static int getIntFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Integer.parseInt(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to 0");
        return 0;
    }
    
    public static double getDoubleFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Double.parseDouble(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to 0.0");
        return 0.0;
    }
    
    public AudioClip getAudioClipFromString(String str, String start) {
        if(str.startsWith(start)) {
            return getAudioClip(getCodeBase(), str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to null");
        return null;
    }
    
    public Font getFontFromString(String str, String start) {
        if(str.startsWith(start)) {
            return new Font(str.substring(start.length()), Font.PLAIN, 18);
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to null");
        return null;
    }
    
    public static String getSubstringFromString(String str, String start) {
        if(str.startsWith(start)) {
            return str.substring(start.length());
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to blank string");
        return "";
    }
    
    public static Image splitImage(Image image, int rows, int columns, int imgWidth, int imgHeight, int imgIndex) {
        ImageIcon i = new ImageIcon(image);
        BufferedImage b = new BufferedImage(i.getIconWidth(), i.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        b.createGraphics().drawImage(image, null, null);
        int x = imgWidth * (imgIndex % rows);
        int y = imgHeight * (imgIndex / rows);
        return b.getSubimage(x, y, imgWidth, imgHeight);
    }
    
    public boolean animate(boolean frame, int animSpeed) {
        //this works for two-frame animations only
        if(time % (fps / animSpeed) == 0) {
            frame = !frame;
        }
        return frame;
    }
    
    public void run() {
          while(true) {
              repaint(); //calls update(g) which calls paint(g)
              
              //sleep
              try {
                  Thread.sleep(1000/fps);
              }
              catch(ArithmeticException e) { ; } //i don't know why this doesn't work at first
              catch (InterruptedException e) { ; }
          }
    }
    
    public void paint(Graphics g) {
        offscr.setFont(font);
        if(!loaded) {
            g.drawImage(imgLoading, 0, 0, null); //replace with actual loading image
            offscr.drawString("", 0, 0); //loads the font, I'm not sure how else to do it
            offscr.clearRect(0, 0, appWidth, appHeight);
            loaded = true;
        }
        
        currentLevel.draw(offscr, -viewX, -viewY, this);
        
        //draw the player's health
        if(player != null) {
            offscr.setColor(Color.RED);
            offscr.fillRect(10 + viewX, 10 + viewY, 100, 20);
            offscr.setColor(Color.GREEN);
            offscr.fillRect(10 + viewX, 10 + viewY, (int)(100.0 * player.health / player.maxHealth), 20);
        }
        
        //draw the current MessageOverlay if it exists
        if(currentMessage != null) {
            currentMessage.draw(offscr, viewX, viewY);
        }
        
        //draw the inventory
        if(inventoryOpen) {
            ImageIcon invBGIcon = new ImageIcon(imgInventoryBG);
            ImageIcon itemSelectedIcon = new ImageIcon(imgItemSelected);
            int bgY = appHeight - invBGIcon.getIconHeight();
            int selectedOffsetX = invBGIcon.getIconWidth() / 10 - itemSelectedIcon.getIconWidth();
            int selectedOffsetY = (invBGIcon.getIconHeight() - itemSelectedIcon.getIconHeight()) / 2;
            int selectedX = player.inventorySlot * (itemSelectedIcon.getIconWidth() + selectedOffsetX);
            offscr.drawImage(imgInventoryBG, viewX, viewY + bgY, null);
            offscr.drawImage(imgItemSelected, viewX + selectedX, viewY + bgY + selectedOffsetY, null);
            offscr.drawString("" + player.inventorySlot, viewX, viewY + 100);
            //draw the items too!
            for(int i = 0; i < player.inventory.size(); i++) {
                offscr.drawImage(player.inventory.get(i).image, viewX + (itemSelectedIcon.getIconWidth() + selectedOffsetX) * i, viewY + bgY + selectedOffsetY, null);
            }
        }
        
        g.drawImage(offscreenImage, -viewX, -viewY, null);
    }

    public void update(Graphics g) {
        time++;
        if(!paused) {
            if(player != null && currentLevel != null) {
                //move the player
                //try {
                player.move(currentLevel.allSolids, this);
                //} catch(NullPointerException npe) { ; } //i don't know why this doesn't work at first
            
                ArrayList<Solid> toRemove = new ArrayList<Solid>();
                if(currentLevel != null)
                    for(Solid solid : currentLevel.allSolids) {     //sometimes throws ConcurrentModificationException
                        //update the solids (only used for bullet movement right now)
                        solid.update();
                        //check if the solid is hit by an attack
                        for(Attack attack : Attack.allAttacks) {    //sometimes throws ConcurrentModificationException
                            if(attack.boundingBox.intersects(solid.boundingBox)) {
                                if(solid instanceof Actor) {
                                    if(!attack.creator.equals(solid)) {
                                        ((Actor)solid).health -= attack.damage;
                                        toRemove.add(attack);
                                    }
                                }
                                else if(!(solid instanceof Attack)){ //so that it doesn't collide with itself
                                    toRemove.add(attack);
                                }
                            }
                        }
                        //kill necessary Actors
                        if(solid instanceof Actor) {
                            if(((Actor)solid).killMe()) {
                                toRemove.add(solid);
                            }
                        }
                    }
                for(Solid removeMe : toRemove) {
                    currentLevel.allSolids.remove(removeMe);
                    if(removeMe instanceof Attack) {
                        Attack.allAttacks.remove(removeMe);
                    }
                }
                
                //move the view
                //will not move outside the level boundary
                //room size less than applet size may cause problems?
                if(player.x - viewX < viewDistFromPlayerX) {
                    viewX = Math.max(player.x - viewDistFromPlayerX, 0);
                }
                else if(viewX + appWidth - player.x < viewDistFromPlayerX) {
                    viewX = Math.min(player.x + viewDistFromPlayerX - appWidth, currentLevel.width - appWidth);
                }
                if(player.y - viewY < viewDistFromPlayerY) {
                    viewY = Math.max(player.y - viewDistFromPlayerY, 0);
                }
                else if(viewY + appHeight - player.y < viewDistFromPlayerY) {
                    viewY = Math.min(player.y + viewDistFromPlayerY - appHeight, currentLevel.height - appHeight);
                }
            }
        }
        
        //inventory timer
        if(inventoryTimer > 0) {
            inventoryTimer--;
        }
        else if(inventoryTimer == 0){
            inventoryOpen = false;
            inventoryTimer = -1;
            //paused = false;
        }
        
        paint(g);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        for(Solid s : currentLevel.allSolids) {
            if(s instanceof Button && s.boundingBox.contains(e.getX(), e.getY())) {
                ((Button)s).click(this);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            //escape
            System.exit(0);
            break;
        case KeyEvent.VK_SPACE:
            space();
            break;
        case KeyEvent.VK_E:
            //e
            if(player != null) {
                paused = !paused;
                //if the subinventory is open, close it, else
                inventoryOpen = !inventoryOpen;
            }
            break;
        case KeyEvent.VK_W:
            if(inventoryOpen) {
                //open the sub-inventory or navigate within it
            }
            else {
                if(player != null)
                player.up = true;
            }
            break;
        case KeyEvent.VK_S:
            if(inventoryOpen) {
                //navigate within the sub-inventory
            }
            else {
                if(player != null)
                player.down = true;
            }
            break;
        case KeyEvent.VK_A:
            if(inventoryOpen) {
                if(player != null) {
                    //if the subinventory is open, move within it, else
                    player.inventorySlot--;
                    if(player.inventorySlot < 0) {
                        player.inventorySlot = 9;
                    }
                }
            }
            else {
                if(player != null)
                player.left = true;
            }
            break;
        case KeyEvent.VK_D:
            if(inventoryOpen) {
                if(player != null) {
                    //if the subinventory is open, move within it, else
                    player.inventorySlot++;
                    if(player.inventorySlot > 9) {
                        player.inventorySlot = 0;
                    }
                }
            }
            else {
                if(player != null)
                player.right = true;
            }
            break;
        case KeyEvent.VK_UP:
            if(inventoryOpen) {
                //open the sub-inventory or navigate within it
            }
            else {
                if(player != null)
                player.up = true;
            }
            break;
        case KeyEvent.VK_DOWN:
            if(inventoryOpen) {
                //navigate within the sub-inventory
            }
            else {
                if(player != null)
                player.down = true;
            }
            break;
        case KeyEvent.VK_LEFT:
            if(inventoryOpen) {
                if(player != null) {
                    //if the subinventory is open, move within it, else
                    player.inventorySlot--;
                    if(player.inventorySlot < 0) {
                        player.inventorySlot = 9;
                    }
                }
            }
            else {
                if(player != null)
                player.left = true;
            }
            break;
        case KeyEvent.VK_RIGHT:
            if(inventoryOpen) {
                if(player != null) {
                    //if the subinventory is open, move within it, else
                    player.inventorySlot++;
                    if(player.inventorySlot > 9) {
                        player.inventorySlot = 0;
                    }
                }
            }
            else {
                if(player != null)
                player.right = true;
            }
            break;
        case KeyEvent.VK_N:
            //n
            nextLevel();
            break;
        case KeyEvent.VK_1:
            //1
            if(player != null) {
                player.inventorySlot = 0;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_2:
            //2
            if(player != null) {
                player.inventorySlot = 1;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_3:
            if(player != null) {
                player.inventorySlot = 2;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_4:
            if(player != null) {
                player.inventorySlot = 3;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_5:
            if(player != null) {
                player.inventorySlot = 4;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_6:
            if(player != null) {
                player.inventorySlot = 5;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_7:
            if(player != null) {
                player.inventorySlot = 6;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_8:
            if(player != null) {
                player.inventorySlot = 7;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_9:
            //9
            if(player != null) {
                player.inventorySlot = 8;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        case KeyEvent.VK_0:
            //0
            if(player != null) {
                player.inventorySlot = 9;
                if(!inventoryOpen) {
                    //paused = true;
                    inventoryOpen = true;
                    inventoryTimer = 30;
                }
            }
            break;
        default:
            break;
    }
    //put inventory swapping stuff here i guess
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_W:
            if(player != null)
                player.up = false;
            break;
        case KeyEvent.VK_S:
            if(player != null)
                player.down = false;
            break;
        case KeyEvent.VK_A:
            if(player != null)
                player.left = false;
            break;
        case KeyEvent.VK_D:
            if(player != null)
                player.right = false;
            break;
        case KeyEvent.VK_UP:
            if(player != null)
                player.up = false;
            break;
        case KeyEvent.VK_DOWN:
            if(player != null)
                player.down = false;
            break;
        case KeyEvent.VK_LEFT:
            if(player != null)
                player.left = false;
            break;
        case KeyEvent.VK_RIGHT:
            if(player != null)
                player.right = false;
            break;
        default:
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
        
    }
}

/*
Bugs to fix:
player can still animate in direction that he's facing while MessageOverlay is present
if moving using WASD, releasing arrow key will stop player
    could have memory of key pressed to move- probably not worth it
    could also use two separate variables (left = WASDleft and arrowsleft)
allOverlays does not include currentMessage (i didn't add it)
Give Items a separate image for inventory appearance?
Move player selected item choosing from player.move() to keyboard checking part
Holding down space makes constantly flashing and re-interacting MessageOverlays
Make weapons fire as fast as possible when space is first pressed?
Move arraylists of stuff to that class (to add automagically)
Make NPCs Actors? (so that they can be killed, "showing" them a sword could kill them...)
Make parsing of .txt's more lenient (stuff can be on any line)
Fix ConcurrentModificationExceptions (445, 449)
Remove n shortcut to next level (when ready to release)
Reduce size of player/NPC bounding box
Temp inventory opening is kinda annoying- just keep it open when number is pressed?

Features to add:
Inventory
    slots 1 through 9 are items, slot 0 is always blank (for holding nothing)
    note: actual slot number is one less than the number told to the player (to match up with keyboard) 
    press up to open expanded inventory for that slot
        if you select an item already in the slot, switch items with that slot
        press down to close & return to bar editing
        would only be necessary if levels will contain more than 9 items
    held item is displayed on player
Multiple dialogue options for NPCs
    Some based on what item the player is holding
Special events upon player interaction with NPCs, SceneryInteractable
    Chests containing items
        would be SceneryInteractables with an event for giving the player the item and a message
Sounds for items
Menus- instructions, settings, credits
Pause screen
More button effects
Real loading screen
Resolution?
Animations somehow...
    would involve NPCs and player being told to move wherever
    could use multiple levels
    could appear on start of level, interaction with SceneryInteractables 


Classes: (redo this, outdated)
RPG: the game itself
Solid: any object that the player can collide with                              RPG has a list of these
    Actor: enemies or the player
        Player: the player
        Enemy: an enemy
    Scenery: a solid that gets drawn
        SceneryInteractable: has a message when the player interacts with it
            NPC: an NPC, has some dialogue, maybe some other stuff too, default bounding box
        Button: a button
    Item: an item that the player can collect
        Weapon: fires an Attack
    Attack: hurts any Actor who did not create it, has a velocity               Attack has a list of these
Level: a level or GUI menu
Overlay: gets drawn above the solids (ex: fog)                                  RPG has a list of these
    note: just using solids w/out boundingbox is easier if you don't need them to be on top
    MessageOverlay: shows a portrait and some text (ex: dialogue)
CustomComparator: for sorting based on y-position, don't forget to reverse the sorted ArrayList
*/