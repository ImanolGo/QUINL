package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


/**
 * An class defining a sample class
 */

public class Sample extends BasicElement {

    private String m_url;

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public Sample(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());
    }

    public String getUrl() {
        return m_url;
    }

    public void setUrl(String url) {
        this.m_url = url;
    }
}
