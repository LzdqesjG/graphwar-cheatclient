package Graphwar;

public class Plus {
    // 这个能否加入Global是硬编码的，编译的时候写死的
    public static final boolean canJoinGlobal = false;

    public static boolean SHOW_COORDINATE_SYSTEM = true;
    public static boolean SHOW_PLAYER_COORDINATES = true;
    public static boolean PREDICTION_ENABLED = true;
    public static boolean hideTyping = true;
    public static String typingMode = "expand"; // expand / text
    public static String typingTextPreview = "None of your business LOL  |  与你无关LLL";


    public static void setShowCoordinateSystem(boolean enabled) {
        SHOW_COORDINATE_SYSTEM = enabled;
    }
    
    public static boolean isShowCoordinateSystem() {
        return SHOW_COORDINATE_SYSTEM;
    }
    
    public static void setShowPlayerCoordinates(boolean enabled) {
        SHOW_PLAYER_COORDINATES = enabled;
    }
    
    public static boolean isShowPlayerCoordinates() {
        return SHOW_PLAYER_COORDINATES;
    }
    
    public static void setPredictionEnabled(boolean enabled) {
        PREDICTION_ENABLED = enabled;
    }
    
    public static boolean isPredictionEnabled() {
        return PREDICTION_ENABLED;
    }
}