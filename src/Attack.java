import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;

public class Attack extends Solid {
    
    int[] velocity;
    int speed;
    int maxAge, age;
    int damage;
    int knockback;
    int boundingBoxOffsetX, boundingBoxOffsetY;
    Actor creator;
    Image imgLeft, imgRight, imgUp, imgDown;
    Rectangle boundingBoxHoriz, boundingBoxVert;
    public static final int[] LEFT = {-1, 0};
    public static final int[] RIGHT = {1, 0};
    public static final int[] UP = {0, -1};
    public static final int[] DOWN = {0, 1};
    
    public Attack(Image image, int x, int y, Rectangle boundingBox, int[] velocity, int speed, int age, int damage, int knockback, Actor creator) {
        super(x, y, boundingBox);
        this.image = image;
        this.velocity = velocity;
        this.speed = speed;
        maxAge = age;
        this.age = age;
        this.damage = damage;
        this.knockback = knockback;
        this.creator = creator;
        boundingBoxOffsetX = boundingBox.x;
        boundingBoxOffsetY = boundingBox.y;
        this.boundingBoxHoriz = new Rectangle(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        this.boundingBoxVert = new Rectangle(boundingBox.x, boundingBox.y, boundingBox.height, boundingBox.width);
        if(image != null) {
            new ImageIcon(image); //load the image so that its dimensions are known (required for rotation)
            rotateImage();
        }
    }
    
    public void position(Actor actor) {
        if(actor.image == null) {
            System.out.println(actor + " image is null, not positioning " + this);
            return;
        }
        if(actor instanceof Enemy) {
            x = actor.x;
            y = actor.y;
            boundingBox.x = actor.x + boundingBoxOffsetX;
            boundingBox.y = actor.y + boundingBoxOffsetY;
            return;
        }
        
        int originX = actor.x + actor.image.getWidth(null) / 2;
        int originY = actor.y + actor.image.getHeight(null) / 4;
        
        if(Arrays.equals(velocity, LEFT)) {
            image = imgLeft;
            boundingBox = boundingBoxHoriz;
            x = originX - image.getWidth(null);
            y = originY  - image.getHeight(null) / 2;
            boundingBox.x = boundingBoxOffsetX + originX - boundingBox.width;
            boundingBox.y = boundingBoxOffsetY + originY - image.getHeight(null) / 2;
        }
        else if(Arrays.equals(velocity, RIGHT)) {
            image = imgRight;
            boundingBox = boundingBoxHoriz;
            x = originX;
            y = originY - image.getHeight(null) / 2;
            boundingBox.x = boundingBoxOffsetX + originX;
            boundingBox.y = boundingBoxOffsetY + originY - image.getHeight(null) / 2;
        }
        else if(Arrays.equals(velocity, UP)) {
            image = imgUp;
            boundingBox = new Rectangle(0, 0, boundingBoxVert.width, boundingBoxVert.height); //can't be boundingBoxVert because its height gets changed sometimes
            x = originX - image.getWidth(null) * 5 / 8;
            y = originY + actor.image.getHeight(null) / 4 - image.getHeight(null);
            boundingBox.x = -boundingBoxOffsetX + originX;
            boundingBox.y = -boundingBoxOffsetY + originY + actor.image.getHeight(null) / 4 - image.getHeight(null);
        }
        else if(Arrays.equals(velocity, DOWN)) {
            image = imgDown;
            boundingBox = new Rectangle(0, 0, boundingBoxVert.width, boundingBoxVert.height);
            x = originX - actor.image.getHeight(null) * 3 / 8;
            y = originY + actor.image.getHeight(null) / 4;
            boundingBox.x = boundingBoxOffsetX + originX - actor.image.getHeight(null) * 3 / 8;
            boundingBox.y = -boundingBoxOffsetY + originY + actor.image.getHeight(null) / 4;
            //move the bounding box down so that the attack draws above its creator
            if(boundingBox.y < actor.boundingBox.y) {
                int h = actor.boundingBox.y - boundingBox.y + 1;
                boundingBox.y = actor.boundingBox.y + 1;
                boundingBox.height -= h;
            }
        }
    }
    
    public void update(RPG rpg) {
        if(age == 0) {
            rpg.currentLevel.allSolids.remove(this);
            return;
        }
        if(age > 0) {
            age--;
        }
        x += velocity[0] * speed;
        y += velocity[1] * speed;
        boundingBox.x += velocity[0] * speed;
        boundingBox.y += velocity[1] * speed;
        if(speed == 0) { //if the player can become null while an attack exists, then add  && rpg.player != null
            position(creator);
        }
    }
    
    public void rotateImage() {
        //create rotated images (source image should face right)
        //warning: the following code was partially copied from various online tutorials
        
        BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 0, 0, null);
        imgRight = buffered;
        
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        imgLeft = op.filter(buffered, null);
        
        buffered = new BufferedImage(image.getHeight(null), image.getWidth(null), BufferedImage.TYPE_INT_ARGB);
        tx = new AffineTransform();
        tx.translate(image.getHeight(null) / 2, image.getWidth(null) / 2);
        tx.rotate(-Math.PI / 2);
        tx.translate(-image.getWidth(null) / 2, -image.getHeight(null) / 2);
        Graphics2D g2 = buffered.createGraphics();
        g2.drawImage(image, tx, null);
        imgUp = op.filter(buffered, null);
        
        tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getWidth(null));
        op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        imgDown = op.filter(buffered, null);
    }
    
    public void checkCollision(ArrayList<Solid> solids) {
        for(int i = 0; i < solids.size(); i++) {
            Solid solid = solids.get(i);
            if(boundingBox.intersects(solid.boundingBox)) {
                if(!(solid instanceof Attack || solid instanceof Actor)){
                    solids.remove(this);
                    return;
                }
            }
        }
    }
    
}
