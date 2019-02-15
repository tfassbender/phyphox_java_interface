package de.fz_juelich.phyphox_interface.connection;

public class PhyphoxDataCollectorTest {
	
	public static void main(String[] args) {
		PhyphoxConnectionSettings connection = new PhyphoxConnectionSettings("http://172.18.50.163", 8080);
		PhyphoxConnection collector = new PhyphoxConnection(connection);
		
		String request = "Bx%20(no%20offset)=full&By%20(no%20offset)=full";
		try {
			collector.getData(request);
		}
		catch (PhyphoxConnectionException e) {
			e.printStackTrace();
		}
	}
}