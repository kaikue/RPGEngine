import java.awt.*;
import javax.swing.ImageIcon;

public class Button extends Scenery {
    
    String effect;
    
    public Button(Image image, int x, int y, String effect) {
        super(image, x, y, new Rectangle(x, y, new ImageIcon(image).getIconWidth(), new ImageIcon(image).getIconHeight()));
        this.effect = effect;
    }
    
    public void click(RPG rpg) {
        rpg.performEffect(effect);
    }
    
    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }
}