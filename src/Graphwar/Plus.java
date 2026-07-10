package Graphwar;

public class Plus {
    //不允许拿这个祸害不知情的绿玩
    //只允许和朋友游玩时或对方同意时使用(或者打机器人)
    //请阅读并同意以上要求才可启用
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
