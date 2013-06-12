import java.applet.AudioClip;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Level {
    
    int width, height;
    String[] data;
    Player player;
    ArrayList<Solid> allSolids;
    ArrayList<Overlay> allOverlays;
    Image imgBG;
    public AudioClip bgMusic;
    //boolean keepInventory;
    boolean visited;
    
    public Level(String[] data, RPG rpg) {
        this.data = data;
        width = Math.max(data[0].length() * rpg.tileSize, 800);
        height = Math.max(data.length * rpg.tileSize, 600);
        visited = false;
    }
    
    public void learnSolids(String[] solids, RPG rpg) {
        String className;
        if(solids[0].startsWith("music = ")) {
            bgMusic = rpg.getAudioClipFromString(solids[0], "music = ");
        }
        /*if(solids[1].equals("clearInventory")) {
            keepInventory = false;
        }
        else {
            keepInventory = true;
        }*/
        for(int i = 0; i < solids.length; i++) {
            if(solids[i].startsWith("begin ")) {
                if(solids[i+1].startsWith("class = ")) {
                    className = solids[i+1].substring(8);
                    if(className.equals("Player")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image playerImage = rpg.getImageFromString(solids[i+2], "image = ");
                        int playerWidth = RPGUtils.getIntFromString(solids[i+3], "width = ");
                        int playerHeight = RPGUtils.getIntFromString(solids[i+4], "height = ");
                        int playerSpeed = RPGUtils.getIntFromString(solids[i+5], "speed = ");
                        int playerSheetOffsetX = RPGUtils.getIntFromString(solids[i+6], "sheetOffsetX = ");
                        int playerSheetOffsetY = RPGUtils.getIntFromString(solids[i+7], "sheetOffsetY = ");
                        int playerHealth = RPGUtils.getIntFromString(solids[i+8], "health = ");
                        player = new Player(playerImage, 0, 0, playerWidth, playerHeight, playerSpeed, playerSheetOffsetX, playerSheetOffsetY, playerHealth);
                        rpg.solidDefs.put(letter, player);
                    }
                    else if(className.equals("Scenery")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        Scenery scenery = new Scenery(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight));
                        rpg.solidDefs.put(letter, scenery);
                    }
                    else if(className.equals("SceneryInteractable")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        //find the last message
                        int currentLine = i+7;
                        while(solids[currentLine].startsWith("-")) {
                            currentLine++;
                        }
                        currentLine--;
                        //move upwards
                        MessageOverlay message = null;
                        while(solids[currentLine].startsWith("-")) {
                            String[] line = solids[currentLine].split(", ");
                            Image img = rpg.getImageFromString(line[0], "-");
                            String text = line[1];
                            message = rpg.createMessageOverlay(img, text, message);
                            currentLine--;
                        }
                        SceneryInteractable sceneryInteractable = new SceneryInteractable(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight), message);
                        rpg.solidDefs.put(letter, sceneryInteractable);
                    }
                    else if(className.equals("InputInteractable")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        String code = RPGUtils.getSubstringFromString(solids[i+7], "code = ");
                        String effect = RPGUtils.getSubstringFromString(solids[i+8], "effect = ");
                        //find the last message
                        int currentLine = i+9;
                        while(solids[currentLine].startsWith("-")) {
                            currentLine++;
                        }
                        currentLine--;
                        //move upwards
                        String[] line = solids[currentLine].split(", ");
                        Image img = rpg.getImageFromString(line[0], "-");
                        String text = line[1];
                        MessageOverlay message = rpg.createInputMessage(img, text, null, code, effect);
                        currentLine--;
                        while(solids[currentLine].startsWith("-")) {
                            line = solids[currentLine].split(", ");
                            img = rpg.getImageFromString(line[0], "-");
                            text = line[1];
                            message = rpg.createMessageOverlay(img, text, message);
                            currentLine--;
                        }
                        SceneryInteractable inputInteractable = new SceneryInteractable(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight), message);
                        rpg.solidDefs.put(letter, inputInteractable);
                    }
                    else if(className.equals("NPC")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        //find the last message
                        int currentLine = i+3;
                        while(solids[currentLine].startsWith("-")) {
                            currentLine++;
                        }
                        currentLine--;
                        //move upwards
                        MessageOverlay message = null;
                        while(solids[currentLine].startsWith("-")) {
                            String[] line = solids[currentLine].split(", ");
                            Image img = rpg.getImageFromString(line[0], "-");
                            String text = line[1];
                            message = rpg.createMessageOverlay(img, text, message);
                            currentLine--;
                        }
                        NPC npc = new NPC(image, 0, 0, message);
                        rpg.solidDefs.put(letter, npc);
                    }
                    else if(className.equals("Item")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        Item item = new Item(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight));
                        rpg.solidDefs.put(letter, item);
                    }
                    else if(className.equals("Enemy")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        int health = RPGUtils.getIntFromString(solids[i+7], "health = ");
                        Enemy enemy = new Enemy(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight), health);
                        rpg.solidDefs.put(letter, enemy);
                    }
                    else if(className.equals("Weapon")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        int rate = RPGUtils.getIntFromString(solids[i+7], "rate = ");
                        Image attackImage = rpg.getImageFromString(solids[i+8], "attackImage = ");
                        int attackBBW = RPGUtils.getIntFromString(solids[i+9], "attackBBW = ");
                        int attackBBH = RPGUtils.getIntFromString(solids[i+10], "attackBBH = ");
                        int attackBBX = RPGUtils.getIntFromString(solids[i+11], "attackBBX = ");
                        int attackBBY = RPGUtils.getIntFromString(solids[i+12], "attackBBY = ");
                        int attackSpeed = RPGUtils.getIntFromString(solids[i+13], "attackSpeed = ");
                        int attackAge = RPGUtils.getIntFromString(solids[i+14], "attackAge = ");
                        int attackDamage = RPGUtils.getIntFromString(solids[i+15], "attackDamage = ");
                        int[] attackVelocity = {0, 0};
                        Attack attack = new Attack(attackImage, 0, 0, new Rectangle(attackBBX, attackBBY, attackBBW, attackBBH), attackVelocity, attackSpeed, attackAge, attackDamage, null);
                        Weapon weapon = new Weapon(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight), attack, rate);
                        rpg.solidDefs.put(letter, weapon);
                    }
                    else if(className.equals("Button")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        String effect = RPGUtils.getSubstringFromString(solids[i+3], "effect = ");
                        Button button = new Button(image, 0, 0, effect);
                        rpg.solidDefs.put(letter, button);
                    }
                    else if(className.equals("LevelWarper")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        Image image = rpg.getImageFromString(solids[i+2], "image = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+3], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+4], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+5], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+6], "boundingBoxY = ");
                        String effect = RPGUtils.getSubstringFromString(solids[i+7], "effect = ");
                        boolean keepInventory = RPGUtils.getBooleanFromString(solids[i+8], "keepInventory = ");
                        LevelWarper levelWarper = new LevelWarper(image, 0, 0, new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight), effect, keepInventory);
                        rpg.solidDefs.put(letter, levelWarper);
                    }
                    else if(className.equals("Transparency")) {
                        String letter = RPGUtils.getSubstringFromString(solids[i], "begin ");
                        int drawSpaceWidth = RPGUtils.getIntFromString(solids[i+2], "drawSpaceWidth = ");
                        int drawSpaceHeight = RPGUtils.getIntFromString(solids[i+3], "drawSpaceHeight = ");
                        int boundingBoxWidth = RPGUtils.getIntFromString(solids[i+4], "boundingBoxWidth = ");
                        int boundingBoxHeight = RPGUtils.getIntFromString(solids[i+5], "boundingBoxHeight = ");
                        int boundingBoxX = RPGUtils.getIntFromString(solids[i+6], "boundingBoxX = ");
                        int boundingBoxY = RPGUtils.getIntFromString(solids[i+7], "boundingBoxY = ");
                        Transparency transparency = new Transparency(new Rectangle(0, 0, drawSpaceWidth, drawSpaceHeight), new Rectangle(boundingBoxX, boundingBoxY, boundingBoxWidth, boundingBoxHeight));
                        rpg.solidDefs.put(letter, transparency);
                    }
                    //more object types...
                }
            }
        }
        rpg.solidKeys = rpg.solidDefs.keySet();
    }
    
    public void createObjects(RPG rpg) {
        //create & position the objects
        allSolids = new ArrayList<Solid>();
        allOverlays = new ArrayList<Overlay>();
        
        String letter;
        Solid solid;
        for(int i = 0; i < data.length; i++) {
            for(int j = 0; j < data[i].length(); j++) {
                letter = String.valueOf(data[i].charAt(j));
                if(rpg.solidKeys.contains(letter)) {
                    solid = rpg.solidDefs.get(letter);
                    if(solid instanceof Player) {
                        //for multiple players, change this part
                        solid.x += j * rpg.tileSize;
                        solid.y += i * rpg.tileSize;
                        solid.boundingBox.x += j * rpg.tileSize;
                        solid.boundingBox.y += i * rpg.tileSize;
                        allSolids.add(solid);
                    }
                    else if(solid instanceof NPC) {
                        NPC npc = new NPC(solid.image, j * rpg.tileSize, i * rpg.tileSize, ((NPC)solid).overlay);
                        allSolids.add(npc);
                    }
                    else if(solid instanceof SceneryInteractable) {
                        SceneryInteractable sceneryInteractable = new SceneryInteractable(solid.image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height), ((SceneryInteractable)solid).overlay);
                        allSolids.add(sceneryInteractable);
                    }
                    else if(solid instanceof Transparency) {
                        Transparency transparency = new Transparency(new Rectangle(j * rpg.tileSize, i * rpg.tileSize, ((Transparency)solid).drawSpace.width, ((Transparency)solid).drawSpace.height), new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height));
                        allSolids.add(transparency);
                    }
                    else if(solid instanceof Weapon) {
                        Weapon weapon = new Weapon(((Weapon)solid).image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height), ((Weapon)solid).attack, ((Weapon)solid).rate);
                        allSolids.add(weapon);
                    }
                    else if(solid instanceof Item) {
                        Item item = new Item(((Item)solid).image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height));
                        allSolids.add(item);
                    }
                    else if(solid instanceof Enemy) {
                        Enemy enemy = new Enemy(((Enemy)solid).image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height), ((Enemy)solid).health);
                        allSolids.add(enemy);
                    }
                    else if(solid instanceof Button) {
                        Button button = new Button(((Button)solid).image, j * rpg.tileSize, i * rpg.tileSize, ((Button)solid).effect);
                        allSolids.add(button);
                    }
                    else if(solid instanceof LevelWarper) {
                        LevelWarper levelWarper = new LevelWarper(((LevelWarper)solid).image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height), ((LevelWarper)solid).effect, ((LevelWarper)solid).keepInventory);
                        allSolids.add(levelWarper);
                    }
                    else if(solid instanceof Scenery) {
                        Scenery scenery = new Scenery(solid.image, j * rpg.tileSize, i * rpg.tileSize, new Rectangle(solid.boundingBox.x + j * rpg.tileSize, solid.boundingBox.y + i * rpg.tileSize, solid.boundingBox.width, solid.boundingBox.height));
                        allSolids.add(scenery);
                    }
                }
            }
        }
        
        //top border
        if(player != null) {
            solid = new Scenery(null, -10, -10, new Rectangle(-10, -10, width + 20, 10 + player.height * 3 / 4));
        }
        else {
            solid = new Scenery(null, -10, -10, new Rectangle(-10, -10, width + 20, 10));
        }
        allSolids.add(solid);
        //right border
        solid = new Scenery(null, width, -10, new Rectangle(width, -10, 10, height + 20));
        allSolids.add(solid);
        //bottom border
        solid = new Scenery(null, -10, height, new Rectangle(-10, height, width + 20, 10));
        allSolids.add(solid);
        //left border
        solid = new Scenery(null, -10, -10, new Rectangle(-10, -10, 10, height + 20));
        allSolids.add(solid);
        
        //start music
        bgMusic.loop();
    }
    
    public void draw(Graphics g, int x, int y, RPG rpg) {
        g.drawImage(imgBG, 0, 0, null);
        
        //draw the solids
        //does not sort allSolids itself (player collision depends on that), but need to draw based on y-order
        //draw items with lowest y first so they are under sprites with higher y's
        ArrayList<Solid> sortedAllSolids = allSolids;
        Collections.sort(sortedAllSolids, new SolidComparator());
        Collections.reverse(sortedAllSolids);
        Solid solid;
        for(int i = 0; i < sortedAllSolids.size(); i++) {
            solid = sortedAllSolids.get(i);
            if(solid instanceof Player) {
                rpg.drawPlayer(g);
            }
            else if(solid instanceof Transparency) {
                rpg.drawTransparency(g, (Transparency)solid);
            }
            else {
                solid.draw(g);
            }
            
            //debug- draw bounding boxes
            g.setColor(Color.BLACK);
            g.drawRect(solid.boundingBox.x, solid.boundingBox.y, solid.boundingBox.width, solid.boundingBox.height);
        }
        
        //draw the overlays
        //maybe sort these?
        Overlay overlay;
        for(int i = 0; i < allOverlays.size(); i++) {
            overlay = allOverlays.get(i);
            overlay.draw(g, x, y);
        }
    }
}
