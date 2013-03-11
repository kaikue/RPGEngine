import java.awt.*;

import javax.swing.ImageIcon;

public class Button extends Scenery {
    
    //Image image;
    //int x, y;
    String effect;
    
    public Button(Image image, int x, int y, String effect) {
        super(image, x, y, new Rectangle(x, y, new ImageIcon(image).getIconWidth(), new ImageIcon(image).getIconHeight()));
        //this.image = image;
        this.effect = effect;
    }
    
    public void click(RPG rpg) {
        if(effect.equals("nextLevel")) {
            rpg.nextLevel();
        }
        //support for more effects...
    }
    
    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }
}
