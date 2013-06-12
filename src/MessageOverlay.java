import java.awt.*;
import javax.swing.ImageIcon;

public class MessageOverlay extends Overlay {

    Image speakerImage;
    String message;
    MessageOverlay nextMessage;
    int overlayWidth, overlayHeight, portraitWidth, portraitHeight;
    int offset;
    
    public MessageOverlay(Image overlayImage, Image speakerImage, int x, int y, String message, MessageOverlay nextMessage) {
        super(overlayImage, x, y);
        this.speakerImage = speakerImage;
        this.message = message;
        this.nextMessage = nextMessage;
        ImageIcon i = new ImageIcon(overlayImage);
        this.overlayWidth = i.getIconWidth();
        this.overlayHeight = i.getIconHeight();
        i = new ImageIcon(this.speakerImage);
        this.portraitWidth = i.getIconWidth();
        this.portraitHeight = i.getIconHeight();
        this.offset = (this.overlayHeight - this.portraitHeight) / 2;
        
    }
    
    public boolean isLast() {
        //return whether there is not another slide
        return nextMessage == null;
    }
    
    public void draw(Graphics g, int viewX, int viewY) {
        super.draw(g, viewX, viewY);
        g.drawImage(speakerImage, x + offset + viewX, y + offset + viewY, null);
        g.setColor(Color.white);
        g.drawString(message, x + 200 + viewX, y + 50 + viewY); //text wrapping maybe using rpg.font()
        //perhaps draw parts of message gradually to make text appear over time?
    }
    
    public void advance(RPG rpg) {
        rpg.currentMessage = nextMessage;
    }
}
