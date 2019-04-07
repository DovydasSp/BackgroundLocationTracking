package speckauskas.dovydas.backgroundlocationtracking;


public class DBClass {
    // fields
    private int dataID;
    private int tripID;
    private double latitude;
    private double longitude;
    // constructors
    public DBClass() {}
    public DBClass(int dataID, int tripID, double latitude, double longitude) {
        this.dataID = dataID;
        this.tripID = tripID;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // properties
    public void setID(int id) {
        this.dataID = id;
    }
    public int getID() {
        return this.dataID;
    }
    public void setTripID(int id) {
        this.tripID = id;
    }
    public int getTripID() {
        return this.tripID;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
}