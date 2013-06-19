import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

public class CombatScreen {
    Image playerImg, enemyImg, imgBG;
    Image[] playerSwordImages, playerArmImages, enemySwordImages, enemyArmImages;
    ImageIcon playerIcon, enemyIcon;
    int topBorder, sideBorder;
    int[] playerPos;
    int[] enemyPos;
    int playerHealth, enemyHealth;
    boolean onePlayer;
    
    public CombatScreen(RPG rpg) {
        playerImg = rpg.getImage(rpg.getCodeBase(), "data/images/playercombat.png");
        enemyImg = rpg.getImage(rpg.getCodeBase(), "data/images/enemycombat.png");
        playerIcon = new ImageIcon(playerImg);
        enemyIcon = new ImageIcon(enemyImg);
        imgBG = rpg.getImage(rpg.getCodeBase(), "data/images/hallwaybglarge.png");
        playerSwordImages = new Image[3];
        playerSwordImages[0] = rpg.getImage(rpg.getCodeBase(), "data/images/swordverticallarge.png");
        playerSwordImages[1] = rpg.getImage(rpg.getCodeBase(), "data/images/sworddiagonallarge.png");
        playerSwordImages[2] = rpg.getImage(rpg.getCodeBase(), "data/images/swordhorizontallarge.png");
        playerArmImages = new Image[3];
        playerArmImages[0] = rpg.getImage(rpg.getCodeBase(), "data/images/playerarmlarge-1.png");
        playerArmImages[1] = rpg.getImage(rpg.getCodeBase(), "data/images/playerarmlarge0.png");
        playerArmImages[2] = rpg.getImage(rpg.getCodeBase(), "data/images/playerarmlarge1.png");
        enemySwordImages = new Image[3];
        enemySwordImages[0] = rpg.getImage(rpg.getCodeBase(), "data/images/enemyswordverticallarge.png");
        enemySwordImages[1] = rpg.getImage(rpg.getCodeBase(), "data/images/enemysworddiagonallarge.png");
        enemySwordImages[2] = rpg.getImage(rpg.getCodeBase(), "data/images/enemyswordhorizontallarge.png");
        enemyArmImages = new Image[3];
        enemyArmImages[0] = rpg.getImage(rpg.getCodeBase(), "data/images/enemyarmlarge-1.png");
        enemyArmImages[1] = rpg.getImage(rpg.getCodeBase(), "data/images/enemyarmlarge0.png");
        enemyArmImages[2] = rpg.getImage(rpg.getCodeBase(), "data/images/enemyarmlarge1.png");
        topBorder = 200;
        sideBorder = 150;
        playerPos = new int[2];
        enemyPos = new int[2];
        enemyPos[0] = 1;
        onePlayer = false;
    }
    
    public void draw(Graphics g, RPG rpg) {
        g.drawImage(imgBG, 0, 0, null);
        
        //draw player
        g.drawImage(playerImg, sideBorder, topBorder, null);
        //draw player arm
        g.drawImage(playerArmImages[playerPos[1] + 1], sideBorder + playerIcon.getIconWidth(), topBorder + 80, null);
        //draw player sword
        g.drawImage(playerSwordImages[playerPos[0] + 1], sideBorder + playerIcon.getIconWidth() + 40, topBorder + (32 * playerPos[1]), null);
        
        //draw enemy
        g.drawImage(enemyImg, rpg.appWidth - enemyIcon.getIconWidth() - sideBorder, topBorder, null);
        //draw enemy arm
        g.drawImage(enemyArmImages[enemyPos[1] + 1], rpg.appWidth - enemyIcon.getIconWidth() - sideBorder - 72, topBorder + 80, null);
        //draw enemy sword
        g.drawImage(enemySwordImages[enemyPos[0] + 1], rpg.appWidth - enemyIcon.getIconWidth() - sideBorder - 184, topBorder + (32 * enemyPos[1]), null);
    }
    
    public void update() {
        //System.out.println(Arrays.toString(playerPos));
        
        //figure out collisions based on positions
        if(playerPos[0] == 1 || enemyPos[0] == 1) {
            //if somebody is attacking
            if(playerPos[1] == enemyPos[1]) {
                //they're at the same height
                if(playerPos[0] == -1 || enemyPos[0] == -1) {
                    //one is blocking, don't do anything
                    System.out.println("BLOCKED!");
                }
                else {
                    if(playerPos[0] == 1) {
                        //deal damage to enemy
                        System.out.println("ENEMY HIT- same height");
                    }
                    if(enemyPos[0] == 1) {
                        //deal damage to player
                        System.out.println("PLAYER HIT- same height");
                    }
                }
            }
            else {
                //they're at different heights
                if(playerPos[0] == 1) {
                    //deal damage to enemy
                    System.out.println("ENEMY HIT- different heights");
                }
                if(enemyPos[0] == 1) {
                  //deal damage to player
                    System.out.println("PLAYER HIT- different heights");
                }
            }
        }
        
        if(onePlayer) {
            //determine enemy position for next turn
            enemyPos[0] = (int)(3 * Math.random()) - 1;
            enemyPos[1] = (int)(3 * Math.random()) - 1;
        }
    }
}
