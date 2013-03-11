import java.util.Comparator;

public class CustomComparator implements Comparator<Solid> {

    public int compare(Solid s1, Solid s2) {
        int y1 = s1.boundingBox.y;
        int y2 = s2.boundingBox.y;
        return (y1>y2 ? -1 : (y1==y2 ? 0 : 1));
    }
}
