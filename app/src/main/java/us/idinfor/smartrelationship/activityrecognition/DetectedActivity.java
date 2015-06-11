package us.idinfor.smartrelationship.activityrecognition;


public class DetectedActivity {

    private String activity;
    private int confidence;

    public DetectedActivity(String activity, int confidence) {
        this.activity = activity;
        this.confidence = confidence;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
