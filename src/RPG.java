import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import javax.swing.ImageIcon;

public class RPG extends Applet implements Runnable, MouseListener, KeyListener {
    
    static final long serialVersionUID = 1L;
    static final int LOADING = -1;
    static final int MENU = 0;
    static final int PAUSE = 1;
    static final int GAME = 2;
    static final int TALK = 3;
    static final int INVENTORY = 4;
    
    long time;
    int fps;
    int appWidth, appHeight;
    Thread t;
    Player player;
    double interactionDistance;
    Image offscreenImage;
    int tileSize;
    Graphics offscr;
    Image imgDialogBG, imgInventoryBG, imgItemSelected, imgLoading;
    MessageOverlay currentMessage;
    Font font;
    int gameState, prevState;
    boolean loaded;
    String[] gameData, pauseLevelData, pauseSolidsData;
    String[][] levelsData, solidsData;
    Hashtable<String, Solid> solidDefs = new Hashtable<String, Solid>();
    Set<String> solidKeys;
    int viewX, viewY;
    int viewDistFromPlayerX, viewDistFromPlayerY;
    int numLevels;
    static Level currentLevel;
    Level prevLevel, pauseScreen;
    ArrayList<Level> levels;
    boolean[] wasdKeys, arrowKeys;
    boolean spacePressed, messageAdvanced;
    BufferedImage capture;
    Point pos;
    
    public void init() {
        
        t = new Thread(this);
        t.start();
        setFocusable(true);
        requestFocus();
        
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
            solidsData[i] = readFile("data/game/objects" + i + ".txt");
        }
        pauseLevelData = readFile("data/game/pauselevel.txt");
        pauseSolidsData = readFile("data/game/pauseobjects.txt");
        
        //do some game initialization stuff
        
        //take a screenshot
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            capture = new Robot().createScreenCapture(screenRect);
        } catch (AWTException e) {
            System.out.println("Screen capture failed. Exiting...");
            System.exit(1);
        }
        
        time = 0;
        gameState = LOADING;
        wasdKeys = new boolean[4];
        arrowKeys = new boolean[4];
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
            //Overlay fog = new Overlay(imgFog, 0, 0)
            //allOverlays.add(fog);
            
            levels.add(level);
        }
        pauseScreen = new Level(pauseLevelData, this);
        pauseScreen.learnSolids(pauseSolidsData, this);
        
        currentLevel = levels.get(0);
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        
        addMouseListener(this);
        addKeyListener(this);
    }
    
    public void talk() {
        if(!currentMessage.equals(null)) {
            if(currentMessage.isLast()) {
                gameState = GAME;
            }
            currentMessage = currentMessage.nextMessage;
        }
        messageAdvanced = true;
    }
    
    public void nextLevel() {
        int nextLevel = levels.indexOf(currentLevel) + 1;
        if(nextLevel >= levels.size()) {
            System.out.println("Cannot move to room after last room");
            System.exit(1);
        }
        switchLevel(nextLevel);
    }
    
    public void previousLevel() {
        int previousLevel = levels.indexOf(currentLevel) - 1;
        if(previousLevel < 0) {
            System.out.println("Cannot move to room before first room");
            System.exit(1);
        }
        switchLevel(previousLevel);
    }
    
    public void switchLevel(int levelIndex) {
        currentLevel.bgMusic.stop();
        currentLevel = levels.get(levelIndex);
        //currentLevel.bgMusic.loop(); //?
        //currentLevel.learnSolids(solidsData[nextLevel], this);
        //currentLevel.createObjects(this);
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        currentLevel.bgMusic.loop();
        player = currentLevel.player;
        if(player == null) {
            gameState = MENU;
        }
        else {
            gameState = GAME;
        }
    }
    
    public void pause() {
        prevState = gameState;
        gameState = PAUSE;
        currentLevel.bgMusic.stop();
        prevLevel = currentLevel;
        currentLevel = pauseScreen;
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        player.left = false;
        player.up = false;
        player.right = false;
        player.down = false;
        player = null;
        //currentLevel.bgMusic.loop();
    }
    
    public void unpause() {
        gameState = prevState;
        //currentLevel.bgMusic.stop();
        currentLevel = prevLevel;
        offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        //currentLevel.bgMusic.loop();
    }
    
    public MessageOverlay createMessageOverlay(Image portrait, String message, MessageOverlay nextMessage) {
        //uses some default stuff to make things easier
        return new MessageOverlay(imgDialogBG, portrait, 0, appHeight - new ImageIcon(imgDialogBG).getIconHeight(), message, nextMessage);
    }
    
    public void interact() {
        //check if there is a SceneryInteractable near the player
        //if there is, set currentMessage to its messageOverlay and pause the game, so that dialogue can occur
        Solid s;
        SceneryInteractable interactable;
        for(int i = 0; i < currentLevel.allSolids.size(); i++) {
            s = currentLevel.allSolids.get(i);
            if(s instanceof SceneryInteractable && player.distanceTo(s) < interactionDistance) {
                System.out.println("Talking to " + s);
                interactable = (SceneryInteractable)s;
                gameState = TALK;
                currentMessage = interactable.overlay;
                spacePressed = false;
                messageAdvanced = true;
                return;
            }
        }
    }
    
    public void attack() {
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
    
    public static Font getFontFromString(String str, String start) {
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
    
    public void invSelect(int index, boolean unpause) {
        if(gameState == GAME) {
            gameState = INVENTORY;
            player.inventorySlot = index;
        }
        else if(gameState == INVENTORY) {
            player.inventorySlot = index;
            if(unpause) {
                gameState = GAME;
            }
        }
        player.inventorySlot = (player.inventorySlot + 10) % 10; //need to add 10 in case it has gone to -1 (in which case you want 9)
        
        player.updateHeldItem();
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
              catch(ArithmeticException e) { ; } //I don't know why this doesn't work at first
              catch (InterruptedException e) { ; }
          }
    }
    
    public void paint(Graphics g) {
        offscr.setFont(font);
        if(gameState == LOADING) {
            g.drawImage(imgLoading, 0, 0, null);
            offscr.drawString("", 0, 0); //loads the font, I'm not sure how else to do it
            offscr.clearRect(0, 0, appWidth, appHeight);
            gameState = MENU;
        }
        
        else if(gameState == MENU) {
            currentLevel.draw(offscr, -viewX, -viewY, this);
        }
        
        else if(gameState == GAME || gameState == TALK || gameState == INVENTORY) {
            currentLevel.draw(offscr, -viewX, -viewY, this);
            //draw the player's health
            offscr.setColor(Color.RED);
            offscr.fillRect(10 + viewX, 10 + viewY, 100, 20);
            offscr.setColor(Color.GREEN);
            offscr.fillRect(10 + viewX, 10 + viewY, (int)(100.0 * player.health / player.maxHealth), 20);
            
            //draw the current MessageOverlay if it exists
            if(currentMessage != null) {
                currentMessage.draw(offscr, viewX, viewY);
            }
            
            //draw the inventory
            if(gameState == INVENTORY) {
                ImageIcon invBGIcon = new ImageIcon(imgInventoryBG);
                ImageIcon itemSelectedIcon = new ImageIcon(imgItemSelected);
                int bgY = appHeight - invBGIcon.getIconHeight();
                int selectedOffsetX = invBGIcon.getIconWidth() / 10 - itemSelectedIcon.getIconWidth();
                int selectedOffsetY = (invBGIcon.getIconHeight() - itemSelectedIcon.getIconHeight()) / 2;
                int selectedX = player.inventorySlot * (itemSelectedIcon.getIconWidth() + selectedOffsetX);
                offscr.drawImage(imgInventoryBG, viewX, viewY + bgY, null);
                offscr.drawImage(imgItemSelected, viewX + selectedX, viewY + bgY + selectedOffsetY, null);
                //draw the items too
                for(int i = 0; i < player.inventory.size(); i++) {
                    offscr.drawImage(player.inventory.get(i).image, viewX + (itemSelectedIcon.getIconWidth() + selectedOffsetX) * i, viewY + bgY + selectedOffsetY, null);
                }
            }
        }
        
        g.drawImage(offscreenImage, -viewX, -viewY, null);
    }
    
    public void drawPlayer(Graphics g) {
        if(player.dir.equals("up")) {
            if(player.up && gameState == GAME) {
                player.image = getPlayerAnim();
            }
            else {
                player.image = player.imgUp;
            }
        }
        else if(player.dir.equals("down")) {
            if(player.down && gameState == GAME) {
                player.image = getPlayerAnim();
            }
            else {
                //not animated
                player.image = player.imgDown;
            }
        }
        else if(player.dir.equals("left")) {
            if(player.left && gameState == GAME) {
                //animated
                player.image = getPlayerAnim();
            }
            else {
                //not animated
                player.image = player.imgLeft;
            }
        }
        else if(player.dir.equals("right")) {
            if(player.right && gameState == GAME) {
                //animated
                player.image = getPlayerAnim();
            }
            else {
                //not animated
                player.image = player.imgRight;
            }
        }
        g.drawImage(player.image, player.x, player.y, null);
    }
    
    public Image getPlayerAnim() {
        //boolean based animation: for the discerningly lazy programmer
        player.frame = animate(player.frame, 5); //every 5 steps, advance the animation
        if(player.dir.equals("up")) {
            if(player.frame) {
                return player.animUp1;
            }
            return player.animUp2;
        }
        if(player.dir.equals("down")) {
            if(player.frame) {
                return player.animDown1;
            }
            return player.animDown2;
        }
        if(player.dir.equals("left")) {
            if(player.frame) {
                return player.animLeft1;
            }
            return player.animLeft2;
        }
        if(player.dir.equals("right")) {
            if(player.frame) {
                return player.animRight1;
            }
            return player.animRight2;
        }
        return null;
    }
    
    public void drawTransparency(Graphics g, Transparency t) {
        //move the capture to -pos.x, -pos.y, then draw a clipped rectangle
        int realX = pos.x + t.x;
        int realY = pos.y + t.y;
        g.drawImage(capture, t.x, t.y, t.x + t.drawSpace.width, t.y + t.drawSpace.height, realX, realY, realX + t.drawSpace.width, realY + t.drawSpace.height, null);
    }
    
    public void update(Graphics g) {
        time++;
        pos = getLocationOnScreen();
        if(gameState == GAME) {
            //move the player
            player.move(currentLevel.allSolids, this);
            
            //remove necessary Solids
            ArrayList<Solid> toRemove = new ArrayList<Solid>();
            if(currentLevel != null)
                for(Solid solid : currentLevel.allSolids) {     //sometimes throws ConcurrentModificationException?
                    //update the solids (only used for Attacks right now)
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
            //room size less than applet size causes problems, don't do that
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
            
            if(spacePressed) {
                attack();
                if(!messageAdvanced) {
                    interact();
                }
            }
        }
        else if(gameState == TALK) {
            if(spacePressed &&!messageAdvanced) {
                talk();
            }
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
    public void mouseEntered(MouseEvent e) { }
    
    @Override
    public void mouseExited(MouseEvent e) { }
    
    @Override
    public void mousePressed(MouseEvent e) { }
    
    @Override
    public void mouseReleased(MouseEvent e) { }
    
    public void pressDirection(int dir, boolean wasd) {
        if(gameState == INVENTORY) {
            //open/close the sub-inventory or navigate within it
            if(dir == 2) {
                invSelect(player.inventorySlot - 1, false);
            }
            else if(dir == 3) {
                invSelect(player.inventorySlot + 1, false);
            }
        }
        else {
            if(wasd) {
                wasdKeys[dir] = true;
            }
            else {
                arrowKeys[dir] = true;
            }
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            //escape
            if(gameState == GAME || gameState == TALK || gameState == INVENTORY) {
                pause();
            }
            else if(gameState == PAUSE) {
                unpause();
            }
            break;
        case KeyEvent.VK_SPACE:
            spacePressed = true;
            if(gameState == INVENTORY) {
                gameState = GAME;
            }
            break;
        case KeyEvent.VK_E:
            //if the subinventory is open, close it
            if(gameState == INVENTORY) {
                gameState = GAME;
            }
            else if(gameState == GAME) {
                gameState = INVENTORY;
            }
            break;
        case KeyEvent.VK_W:
            pressDirection(0, true);
            break;
        case KeyEvent.VK_S:
            pressDirection(1, true);
            break;
        case KeyEvent.VK_A:
            pressDirection(2, true);
            break;
        case KeyEvent.VK_D:
            pressDirection(3, true);
            break;
        case KeyEvent.VK_UP:
            pressDirection(0, false);
            break;
        case KeyEvent.VK_DOWN:
            pressDirection(1, false);
            break;
        case KeyEvent.VK_LEFT:
            pressDirection(2, false);
            break;
        case KeyEvent.VK_RIGHT:
            pressDirection(3, false);
            break;
        case KeyEvent.VK_N:
            nextLevel();
            break;
        case KeyEvent.VK_B:
            previousLevel();
            break;
        case KeyEvent.VK_1:
            invSelect(0, true);
            break;
        case KeyEvent.VK_2:
            invSelect(1, true);
            break;
        case KeyEvent.VK_3:
            invSelect(2, true);
            break;
        case KeyEvent.VK_4:
            invSelect(3, true);
            break;
        case KeyEvent.VK_5:
            invSelect(4, true);
            break;
        case KeyEvent.VK_6:
            invSelect(5, true);
            break;
        case KeyEvent.VK_7:
            invSelect(6, true);
            break;
        case KeyEvent.VK_8:
            invSelect(7, true);
            break;
        case KeyEvent.VK_9:
            invSelect(8, true);
            break;
        case KeyEvent.VK_0:
            invSelect(9, true);
            break;
        default:
            break;
        }
        if(gameState == GAME || gameState == TALK || gameState == INVENTORY) {
            player.up = wasdKeys[0] || arrowKeys[0];
            player.down = wasdKeys[1] || arrowKeys[1];
            player.left = wasdKeys[2] || arrowKeys[2];
            player.right = wasdKeys[3] || arrowKeys[3];
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_SPACE:
            spacePressed = false;
            messageAdvanced = false;
            break;
        case KeyEvent.VK_W:
            wasdKeys[0] = false;
            break;
        case KeyEvent.VK_S:
            wasdKeys[1] = false;
            break;
        case KeyEvent.VK_A:
            wasdKeys[2] = false;
            break;
        case KeyEvent.VK_D:
            wasdKeys[3] = false;
            break;
        case KeyEvent.VK_UP:
            arrowKeys[0] = false;
            break;
        case KeyEvent.VK_DOWN:
            arrowKeys[1] = false;
            break;
        case KeyEvent.VK_LEFT:
            arrowKeys[2] = false;
            break;
        case KeyEvent.VK_RIGHT:
            arrowKeys[3] = false;
            break;
        default:
            break;
        }
        if(gameState == GAME || gameState == TALK || gameState == INVENTORY) {
            player.up = wasdKeys[0] || arrowKeys[0];
            player.down = wasdKeys[1] || arrowKeys[1];
            player.left = wasdKeys[2] || arrowKeys[2];
            player.right = wasdKeys[3] || arrowKeys[3];
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        ;
    }
}

/*
Bugs to fix:
allOverlays does not include currentMessage (I didn't add it)
Make weapons fire as fast as possible when space is first pressed
Make NPCs Actors? (so that they can be killed, "showing" them a sword could kill them...)
Fix ConcurrentModificationExceptions (445, 449)
Remove n and b shortcuts to levels (when game is finished)
Player side animation needs improvement
Loading screen takes too long to load
Pause screen doesn't draw
    May just be due to lack of content
Level-switching issues
    player's position is not reset- regenerate objects
    viewport sometimes shows part of old level (boundary issues)
    using the previousLevel door then n brings you back to start with a NullPointerException
Player draws on top when at very bottom
Rate should be at least age to prevent multiple melee weapons
Remove:
    Attack.allAttacks (line 428 is problem- iterate through all objects and see if they're an attack?)
    Item.isWeapon
Keep inventory between levels if specified

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
Special events upon player interaction with NPCs, SceneryInteractables
    Chests containing items
        would be SceneryInteractables with an event for giving the player the item and a message
Sounds for items
Give Items a separate image for inventory appearance?
Menus- instructions, settings, credits
Pause screen
More button effects
Real loading screen
Resolution?
Cutscenes somehow...
    would involve NPCs and player being told to move wherever
    slideshow
    could use multiple levels
    could appear on start of level, interaction with SceneryInteractables 
Make text in MessageOverlays appear over time, also wrap around
Make parsing of .txt's more lenient (stuff can be on any line)

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