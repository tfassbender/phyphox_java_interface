package de.fz_juelich.phyphox_interface.data;

public class PhyphoxBuffer {
	
	private String name;
	private double[] data;
	
	public PhyphoxBuffer(String name, double[] data) {
		this.name = name;
		this.data = data;
	}
	private PhyphoxBuffer(PhyphoxBuffer copy, int startIndex) {
		this.name = copy.name;
		this.data = new double[copy.data.length - startIndex];
		System.arraycopy(copy.data, startIndex, data, 0, data.length);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public double[] getData() {
		return data;
	}
	public void setData(double[] data) {
		this.data = data;
	}
	/**
	 * Add new data to the end of the buffer (the array size is adapted).
	 */
	public void attachData(double[] newData) {
		double[] newDataArray = new double[data.length + newData.length];
		System.arraycopy(data, 0, newDataArray, 0, data.length);
		System.arraycopy(newData, 0, newDataArray, data.length, newData.length);
		data = newDataArray;
	}
	
	/**
	 * Get a (sub-) copy of this buffer with the data starting from the given start index.
	 */
	public PhyphoxBuffer getCopyFromIndex(int startIndex) {
		return new PhyphoxBuffer(this, startIndex);
	}
}