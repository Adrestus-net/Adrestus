package io.Adrestus.core.Resourses;


//This class checks nodes that run syncnotexisted class and fail to sync on time. Base
//on counter tryn to sync ever n time
public class CachedCheckPoint {

    private static volatile CachedCheckPoint instance;
    private int CheckPointCounter;

    private CachedCheckPoint() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.setCheckPointCounter(0);
    }

    public static CachedCheckPoint getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (CachedCheckPoint.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedCheckPoint();
                }
            }
        }
        return result;
    }

    public int getCheckPointCounter() {
        return CheckPointCounter;
    }

    public void setCheckPointCounter(int checkPointCounter) {
        CheckPointCounter = checkPointCounter;
    }
}
