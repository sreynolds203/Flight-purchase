

public class FlightTicket {

	//private variables for the flight class
    private String departCity;

    private String arriveCity;

    private String travelDate;
    
    private CharSequence numTravelers;

    //all getter and setter methods for the variables
	public String getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(String travelDate) {
		this.travelDate = travelDate;
	}

	public String getArriveCity() {
		return arriveCity;
	}

	public void setArriveCity(String arriveCity) {
		this.arriveCity = arriveCity;
	}

	public String getDepartCity() {
		return departCity;
	}

	public void setDepartCity(String departCity) {
		this.departCity = departCity;
	}

	public CharSequence getNumTravelers() {
		return numTravelers;
	}

	public void setNumTravelers(CharSequence numTravelers) {
		this.numTravelers = numTravelers;
	}
}
