package client.utils;

public class KeyStrokeUtil {
    private int keystrokeCount;
    private static final int triggerCount = 10; //This is temporarily hardcoded

    /**
     * Constructor
     */
    public KeyStrokeUtil() {
        this.keystrokeCount = 0;
    }

    /**
     * Method for increasing the counter of the KeyStroke
     */
    public void increaseCounter() {
        keystrokeCount++;
        //System.out.println("Key counter increased"); This line is solely for debugging purpose
    }

    /**
     * Getter for the counter
     * @return int with the number of keystrokes pressed
     */
    public int getCounter() {
        return keystrokeCount;
    }

    /**
     * Getter for the number of keystrokes needed for triggering an action
     * @return int with the number of keystrokes needed for triggering
     */
    public int getTrigger() {
        return triggerCount;
    }

    /**
     * Resets the keystrokes counter
     */
    public void counterReset() {
        this.keystrokeCount = 0;
    }
}
