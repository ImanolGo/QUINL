package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import org.json.JSONObject;

/**
 * An class defining a sample class
 */

public class Sample extends BasicElement {

    protected static final String TAG_NAME = "file";
    protected static final String TAG_VERSION = "version";
    protected static final String TAG_ID = "id";
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
            JSONObject zoneJson  = reader.getJSONObject("sound");


            int id = zoneJson.getInt(TAG_ID);
            double version = zoneJson.getDouble(TAG_VERSION);
            String name = zoneJson.getString(TAG_NAME);
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
