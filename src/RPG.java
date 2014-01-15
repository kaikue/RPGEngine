import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.ImageIcon;

public class RPG extends Applet implements Runnable, MouseListener, KeyListener {
    
    private static final long serialVersionUID = 1L;
    static final int LOADING = -1;
    static final int MENU = 0;
    static final int PAUSE = 1;
    static final int GAME = 2;
    static final int TALK = 3;
    static final int INVENTORY = 4;
    static final int INPUT = 5;
    
    long time;
    int fps;
    int appWidth, appHeight;
    Thread t;
    Player player;
    double interactionDistance;
    BufferedImage offscreenImage;
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
    Level currentLevel;
    Level prevLevel, pauseScreen;
    ArrayList<Level> levels;
    boolean[] wasdKeys, arrowKeys;
    boolean spacePressed, messageAdvanced;
    BufferedImage capture;
    Point pos;
    TextField text;
    java.awt.Button inputButton;
    
    public void init() {
        setFocusable(true);
        requestFocus();
        
        //open the files
        gameData = RPGUtils.readFile("data/game/game.txt");
        
        //begin parsing the game data
        int line = 0;
        numLevels = RPGUtils.getIntFromString(gameData[line++], "numLevels = ");
        fps = RPGUtils.getIntFromString(gameData[line++], "fps = ");
        appWidth = RPGUtils.getIntFromString(gameData[line++], "appWidth = ");
        appHeight = RPGUtils.getIntFromString(gameData[line++], "appHeight = ");
        interactionDistance = RPGUtils.getDoubleFromString(gameData[line++], "interactionDistance = ");
        tileSize = RPGUtils.getIntFromString(gameData[line++], "tileSize = ");
        imgDialogBG = RPGUtils.getImageFromString(gameData[line++], "imgDialogBG = ");
        imgInventoryBG = RPGUtils.getImageFromString(gameData[line++], "imgInventoryBG = ");
        imgItemSelected = RPGUtils.getImageFromString(gameData[line++], "imgItemSelected = ");
        imgLoading = RPGUtils.getImageFromString(gameData[line++], "imgLoading = ");
        viewDistFromPlayerX = RPGUtils.getIntFromString(gameData[line++], "viewDistFromPlayerX = ");
        viewDistFromPlayerY = RPGUtils.getIntFromString(gameData[line++], "viewDistFromPlayerY = ");
        font = RPGUtils.getFontFromString(gameData[line++], "font = ");
        //done parsing the game data
        
        solidsData = new String[numLevels][];
        levelsData = new String[numLevels][];
        
        for(int i = 0; i < numLevels; i++) {
            levelsData[i] = RPGUtils.readFile("data/game/level" + i + ".txt");
            solidsData[i] = RPGUtils.readFile("data/game/objects" + i + ".txt");
        }
        pauseLevelData = RPGUtils.readFile("data/game/pauselevel.txt");
        pauseSolidsData = RPGUtils.readFile("data/game/pauseobjects.txt");
        
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
        pauseScreen.createObjects(this);
        
        currentLevel = levels.get(0);
        //offscreenImage = createImage(currentLevel.width, currentLevel.height);
        offscreenImage = new BufferedImage(appWidth, appHeight, BufferedImage.TYPE_INT_RGB);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        setLayout(null);
        
        addMouseListener(this);
        addKeyListener(this);
        
        t = new Thread(this);
        t.start();
    }
    
    public void talk() {
        if(!currentMessage.equals(null)) {
            if(currentMessage.isLast()) {
                gameState = GAME;
                spacePressed = false; //so that you don't immediately attack after finishing a conversation
            }
            currentMessage.advance(this);
        }
        messageAdvanced = true;
    }
    
    public void performEffect(String effect) {
        if(effect.equals("nextLevel")) {
            nextLevel(false);
        }
        else if(effect.equals("previousLevel")) {
            previousLevel(false);
        }
        else if(effect.equals("resume")) {
            unpause();
        }
        else if(effect.equals("quit")) {
            quit();
        }
        //support for more effects...
    }
    
    public void getInput(String code, String effect) {
        text = new TextField();
        setFont(font);
        text.addKeyListener(new EnterListener(text, code, effect));
        inputButton = new java.awt.Button("OK");
        InputListener listener = new InputListener(text, code, effect);
        inputButton.addActionListener(listener);
        int textWidth = 100;
        int textHeight = getFontMetrics(font).getHeight() + 10;
        int buttonWidth = textWidth;
        int buttonHeight = textHeight;
        text.setBounds(appWidth / 2 - textWidth / 2, appHeight / 2 - textHeight, textWidth, textHeight);
        inputButton.setBounds(appWidth / 2 - buttonWidth / 2, appHeight / 2, buttonWidth, buttonHeight);
        add(text);
        text.requestFocus(); //so that the user can type without selecting the box
        add(inputButton);
        validate();
        prevState = gameState;
        gameState = INPUT;
    }
    
    private class InputListener implements ActionListener {
        private TextField field;
        private String code;
        private String effect;
        public InputListener(TextField field, String code, String effect) {
            this.field = field;
            this.code = code;
            this.effect = effect;
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            checkCode(field, code, effect);
        }
    }
    
    private class EnterListener extends KeyAdapter {
        private TextField field;
        private String code;
        private String effect;
        public EnterListener(TextField field, String code, String effect) {
            this.field = field;
            this.code = code;
            this.effect = effect;
        }
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_ENTER) {
                checkCode(field, code, effect);
            }
        }
    }
    
    public void checkCode(TextField field, String code, String effect) {
        if(field.getText().equals(code)) {
            //string is correct
            performEffect(effect);
        }
        closeInput();
    }
    
    public void closeInput() {
        remove(text);
        remove(inputButton);
        gameState = prevState;
        //why doesn't this work?
        //updatePlayerMove();
        //player.left = false;
        //player.up = false;
        //player.right = false;
        //player.down = false;
    }
    
    public void nextLevel(boolean keepInventory) {
        int nextLevel = levels.indexOf(currentLevel) + 1;
        if(nextLevel >= levels.size()) {
            System.out.println("Cannot move to room after last room");
            System.exit(1);
        }
        switchLevel(nextLevel, keepInventory);
    }
    
    public void previousLevel(boolean keepInventory) {
        int previousLevel = levels.indexOf(currentLevel) - 1;
        if(previousLevel < 0) {
            System.out.println("Cannot move to room before first room");
            System.exit(1);
        }
        switchLevel(previousLevel, keepInventory);
    }
    
    public void switchLevel(int levelIndex, boolean keepInventory) {
        ArrayList<Item> oldInv = null;
        int oldHealth = 0;
        if(player != null) {
            oldInv = player.inventory;
            oldHealth = player.health;
        }
        currentLevel.bgMusic.stop();
        currentLevel = levels.get(levelIndex);
        //currentLevel.bgMusic.loop(); //?
        
        //reset the level
        if(!currentLevel.visited) {
            currentLevel.learnSolids(solidsData[levelIndex], this); //note: if you remove this, really weird stuff happens
            currentLevel.createObjects(this);
            currentLevel.visited = true;
        }
        offscreenImage = new BufferedImage(currentLevel.width, currentLevel.height, BufferedImage.TYPE_INT_RGB);
        offscr = offscreenImage.getGraphics();
        currentLevel.bgMusic.loop();
        player = currentLevel.player;
        if(player != null) {
            gameState = GAME;
            if(keepInventory) {
                player.inventory = oldInv;
                player.health = oldHealth;
                player.updateHeldItem();
            }
        }
        else {
            gameState = MENU;
        }
    }
    
    public void pause() {
        prevState = gameState;
        gameState = PAUSE;
        currentLevel.bgMusic.stop();
        prevLevel = currentLevel;
        currentLevel = pauseScreen;
        offscreenImage = new BufferedImage(currentLevel.width, currentLevel.height, BufferedImage.TYPE_INT_RGB);
        offscr = offscreenImage.getGraphics();
        player = null;
        //currentLevel.bgMusic.loop();
    }
    
    public void unpause() {
        gameState = prevState;
        //currentLevel.bgMusic.stop();
        currentLevel = prevLevel;
        offscreenImage = new BufferedImage(currentLevel.width, currentLevel.height, BufferedImage.TYPE_INT_RGB);
        offscr = offscreenImage.getGraphics();
        player = currentLevel.player;
        //currentLevel.bgMusic.loop();
    }
    
    public MessageOverlay createMessageOverlay(Image portrait, String message, MessageOverlay nextMessage) {
        //uses some default stuff to make things easier
        return new MessageOverlay(imgDialogBG, portrait, 0, appHeight - new ImageIcon(imgDialogBG).getIconHeight(), message, nextMessage);
    }
    
    public InputMessage createInputMessage(Image portrait, String message, MessageOverlay nextMessage, String code, String effect) {
        return new InputMessage(imgDialogBG, portrait, 0, appHeight - new ImageIcon(imgDialogBG).getIconHeight(), message, code, effect);
    }
    
    public boolean interact() {
        //check if there is a SceneryInteractable near the player
        //if there is, set currentMessage to its messageOverlay and pause the game, so that dialogue can occur
        Solid s;
        SceneryInteractable interactable;
        for(int i = 0; i < currentLevel.allSolids.size(); i++) {
            s = currentLevel.allSolids.get(i);
            if(s instanceof SceneryInteractable && player.distanceTo(s) < interactionDistance) {
                interactable = (SceneryInteractable)s;
                gameState = TALK;
                currentMessage = interactable.overlay;
                spacePressed = false;
                messageAdvanced = true;
                return true;
            }
        }
        return false;
    }
    
    public static AudioClip getAudioClipFromString(String str, String start) {
        if(str.startsWith(start)) {
            //return getAudioClip(getCodeBase(), str.substring(start.length()));
            return Applet.newAudioClip(RPG.class.getResource(str.substring(start.length())));
            /*
            try {
                return Applet.newAudioClip(new URL(str.substring(start.length())));
            } catch (MalformedURLException e) {
                System.out.println("Invalid sound URL, defaulting to null");
                return null;
            }*/
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to null");
        return null;
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
            new ImageIcon(imgLoading);
            g.drawImage(imgLoading, 0, 0, null); //do I really need this?
            offscr.drawString("", 0, 0); //loads the font
            offscr.clearRect(0, 0, appWidth, appHeight);
            gameState = MENU;
        }
        
        else if(gameState == MENU) {
            currentLevel.draw(offscr, -viewX, -viewY, this);
        }
        
        else if(gameState == GAME || gameState == TALK || gameState == INVENTORY || gameState == INPUT) {
            currentLevel.draw(offscr, -viewX, -viewY, this);
            //draw the player's health
            offscr.setColor(Color.RED);
            offscr.fillRect(10 + viewX, 10 + viewY, 100, 20);
            offscr.setColor(Color.GREEN);
            offscr.fillRect(10 + viewX, 10 + viewY, (int)(100.0 * player.health / player.maxHealth), 20);
            
            //draw the current conversation
            if(gameState == TALK) {
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
        
        else if(gameState == PAUSE) {
            currentLevel.draw(offscr, 0, 0, this);
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
            updatePlayerMove();
            
            //remove necessary Solids
            ArrayList<Solid> toRemove = new ArrayList<Solid>();
            for(int i = 0; i < currentLevel.allSolids.size(); i++) {
                Solid solid = currentLevel.allSolids.get(i);
                if(solid instanceof Attack) {
                    Attack attack = (Attack)solid;
                    attack.update(this);
                    for(int j = 0; j < currentLevel.allSolids.size(); j++) {
                        Solid solid2 = currentLevel.allSolids.get(j);
                        if(attack.boundingBox.intersects(solid2.boundingBox)) {
                            if(solid2 instanceof Actor) {
                                if(!attack.creator.equals(solid2)) {
                                    //hurt the actor
                                    ((Actor)solid2).health -= attack.damage;
                                    ((Actor)solid2).knockback = findKnockback(attack.knockback, (int)attack.boundingBox.getCenterX(), (int)attack.boundingBox.getCenterY(), (int)solid2.boundingBox.getCenterX(), (int)solid2.boundingBox.getCenterY());
                                    toRemove.add(attack);
                                }
                            }
                            else if(!(solid2 instanceof Attack)){ //so that it doesn't collide with itself or other Attacks
                                toRemove.add(attack);
                            }
                        }
                    }
                }
                
                else if(solid instanceof Actor) {
                    ((Actor)solid).update(this);
                    
                    if(((Actor)solid).killMe()) {
                        toRemove.add(solid);
                    }
                }
            }
            for(Solid removeMe : toRemove) {
                removeMe.kill(this);
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
                if((!messageAdvanced && !interact()) || messageAdvanced) {
                    player.attack(this);
                }
            }
        }
        else if(gameState == INVENTORY || gameState == INPUT || gameState == TALK) {
            updatePlayerMove();
            if(gameState == TALK) {
                if(spacePressed &&!messageAdvanced) {
                    talk();
                }
            }
        }
        paint(g);
    }
    
    public static int[] findKnockback(int strength, int x1, int y1, int x2, int y2) {
        //from (x1, y1) towards (x2, y2)
        //thanks to http://www.fundza.com/vectors/normalize/
        
        //find base and height of vector
        double base = x2 - x1;
        double height = y2 - y1;
        //scale to out of 1
        double hypot = Math.sqrt(Math.pow(base, 2) + Math.pow(height, 2));
        base /= hypot;
        height /= hypot;
        //scale to strength
        base *= strength;
        height *= strength;
        int[] vector = {(int)base, (int)height};
        return vector;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if(gameState == MENU || gameState == PAUSE) {
            for(Solid s : currentLevel.allSolids) {
                if(s instanceof Button && s.boundingBox.contains(e.getX(), e.getY())) {
                    ((Button)s).click(this);
                }
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
    
    public void updatePlayerMove() {
        player.up = wasdKeys[0] || arrowKeys[0];
        player.down = wasdKeys[1] || arrowKeys[1];
        player.left = wasdKeys[2] || arrowKeys[2];
        player.right = wasdKeys[3] || arrowKeys[3];
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_BACK_QUOTE:
            if(gameState == GAME) {
                getInput("next", "nextLevel");
            }
            break;
        case KeyEvent.VK_ESCAPE:
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
        /*case KeyEvent.VK_N:
            nextLevel(true);
            break;
        case KeyEvent.VK_B:
            previousLevel(true);
            break;
        */
        case KeyEvent.VK_1:
            invSelect(0, false);
            break;
        case KeyEvent.VK_2:
            invSelect(1, false);
            break;
        case KeyEvent.VK_3:
            invSelect(2, false);
            break;
        case KeyEvent.VK_4:
            invSelect(3, false);
            break;
        case KeyEvent.VK_5:
            invSelect(4, false);
            break;
        case KeyEvent.VK_6:
            invSelect(5, false);
            break;
        case KeyEvent.VK_7:
            invSelect(6, false);
            break;
        case KeyEvent.VK_8:
            invSelect(7, false);
            break;
        case KeyEvent.VK_9:
            invSelect(8, false);
            break;
        case KeyEvent.VK_0:
            invSelect(9, false);
            break;
        default:
            break;
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
    }
    
    @Override
    public void keyTyped(KeyEvent e) { }
    
    public void quit() {
        //save the game
        System.exit(0);
    }
}

/*
Issues to fix:
Player side animation needs improvement
Remove loading screen image? it just loads the font
Multiple codes and effects for one input box
Remove CombatScreen, Overlay?
    MessageOverlay is the only overlay used and there's only 1, everything else can just be Scenery
Make the audio creation function static
Spamming attack while walking through doors can break them (maybe? can't reproduce this)
Pressing key while talking, then releasing during input makes player keep moving (focus issue?)
Should Attacks not destroy when hitting Solids?
    fighting near walls is kinda annoying
    just destroy when out of room
    only check for collisions if speed != 0?
Test enemy activation distance
Can't have multiple Attacks from one Actor
    Create copy in Weapon.fire()

Features needed:
Save & load game
Embed in website or make fully standalone
Things that display MessageOverlays when collided (for cutscenes)

Notes:
allOverlays does not include currentMessage (I didn't add it)
Inventory
    slots 1 through 9 are items, slot 0 is always blank (for holding nothing)
    actual slot number is one less than the number told to the player (to match up with keyboard)
Attack rate should be at least age to prevent multiple melee weapons appearing at once
There's an invisible Solid at the top of the screen to prevent player sticking his head offscreen
    don't put anything mobile there

Features that would be nice:
Inventory
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
Make NPCs Actors? (so that they can be killed, "showing" them a sword could kill them...)
Sounds for items
Give Items a separate image for inventory appearance?
Instructions, settings, credits
Resolution?
Cutscenes somehow...
    would involve NPCs and player being told to move wherever
    slideshow
    could use multiple levels
    could appear on start of level, interaction with SceneryInteractables 
Make text in MessageOverlays appear over time, also wrap around
Make parsing of .txt's more lenient (stuff can be on any line)
More enemy AIs
Doors (SceneryInteractable?) and keys (Item)

Classes: (redo this, outdated)
RPG: the game itself
Solid: any object that the player can collide with                              RPG has a list of these
    Scenery: a solid that gets drawn
        SceneryInteractable: has a message when the player interacts with it
            NPC: has a default bounding box
        Button: can be clicked
        Actor: enemies or the player
            Player: the player
            Enemy: an enemy
    Item: an item that the player can collect
        Weapon: fires an Attack
    Attack: hurts any Actor who did not create it, has a velocity
Level: a level or GUI menu
Overlay: gets drawn above the solids (ex: fog)                                  RPG has a list of these
    just use solids with boundingboxes below the level?
    MessageOverlay: shows a portrait and some text (ex: dialogue)
SolidComparator: for sorting based on y-position, don't forget to reverse the sorted ArrayList
*/