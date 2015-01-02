package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 03/01/15.
 */
/**
 * Representation of an iBeacon
 */
class Beacon extends BasicElement {
    private String uuid;
    private int rssi;
    private int txPower;
    private double accuracy;

    public Beacon(BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());
        this.rssi = 0;
        this.txPower = -65;
        this.accuracy = calculateAccuracy();
    }

    public void setUuid (String uuid_) {
        this.uuid = uuid_;
    }

    public String getUuid () {
        return this.uuid;
    }

    public double getAccuracy(){
        return this.accuracy;
    }

    public void setRssi (int rssi) {
        this.rssi = rssi;
        this.accuracy = calculateAccuracy();
    }

    public int getRssi(){
        return this.rssi;
    }

    public boolean equals (Beacon beacon) {
        if (beacon.getId() == this.getId()) {
            return true;
        }

        return false;
    }

    public void setAccuracy(int _accuracy){
        this.accuracy = _accuracy;
    }

    public boolean isNear(double distance){
        return (distance <= this.accuracy);
    }

    private double calculateAccuracy() {
        if (this.rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = this.rssi*1.0/this.txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
}

