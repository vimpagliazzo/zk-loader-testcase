import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;

import java.util.List;

/**
 * Created by impagliazzo on 08/02/16.
 */
public class VM {
    private String shirtColor, shirtSize;
    private final static String shirtLocation = "shirt_%s_%s.png";
    private final static String iconLocation = "shirt_icon_%s.png";

    public List<String> getColors() {
        return ShirtData.getColors();
    }

    public List<String> getSizes() {
        return ShirtData.getSizes();
    }

    @Init
    public void init() {
        setShirtColor("blue");
        setShirtSize("large");
    }

    public String getShirtColor() {
        return shirtColor;
    }

    public void setShirtColor(String shirtColor) {
        this.shirtColor = shirtColor;
    }

    public void setShirtSize(String shirtSize) {
        this.shirtSize = shirtSize;
    }

    public String getShirtSize() {
        return shirtSize;
    }

    @DependsOn({"shirtSize","shirtColor"})
    public String getShirtImage() {
        if(shirtSize==null || shirtColor==null){
            return String.format(shirtLocation, "unknow", "unknow");
        }
        return String.format(shirtLocation, shirtColor, shirtSize);
    }

    public String getIconImage(String icon) {
        return String.format(iconLocation, icon);
    }
}
