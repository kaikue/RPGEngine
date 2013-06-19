import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.ImageIcon;


public class RPGUtils {
    
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
    
    public static int getIntFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Integer.parseInt(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to 0");
        return 0;
    }
    
    public static boolean getBooleanFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Boolean.parseBoolean(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to false");
        return false;
    }
    
    public static double getDoubleFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Double.parseDouble(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to 0.0");
        return 0.0;
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
    
    public static Image getImageFromString(String str, String start) {
        if(str.startsWith(start)) {
            return Toolkit.getDefaultToolkit().getImage(str.substring(start.length()));
        }
        System.out.println("Could not find '" + start + "' in string, defaulting to null");
        return null;
    }
    
    public static Image splitImage(Image image, int rows, int columns, int imgWidth, int imgHeight, int imgIndex) {
        ImageIcon i = new ImageIcon(image);
        BufferedImage b = new BufferedImage(i.getIconWidth(), i.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        b.createGraphics().drawImage(image, null, null);
        int x = imgWidth * (imgIndex % rows);
        int y = imgHeight * (imgIndex / rows);
        return b.getSubimage(x, y, imgWidth, imgHeight);
    }
}
