package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import org.json.JSONObject;

/**
 * An class defining a sample class
 */

public class Sample extends BasicElement {

    protected static final String TAG_FILE_NAME = "file";
    protected static final String ENDPOINT = "http://www.o-a.info/qnl/lib/sounds/";

    private String m_url;

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public Sample(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());
        m_url= "";
    }

    public String getUrl() {
        return m_url;
    }

    public void setUrl(String url) {
        this.m_url = url;
    }

    public static Sample createSampleFromJson(String jsonStr){

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject sampleJson  = reader.getJSONObject("sound");


            int id = sampleJson.getInt(TAG_ID);
            float version = (float) sampleJson.getDouble(TAG_VERSION);
            String name = sampleJson.getString(TAG_FILE_NAME);
            String url = ENDPOINT + name;

            BasicElement basicElement = new BasicElement(id,name,version);
            Sample sample = new Sample(basicElement);
            sample.setUrl(url);
            return sample;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
