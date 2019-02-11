package de.fz_juelich.phyphox_interface.data;

/**
 * Transforms the raw data, received from the phone (as Strings), into usable data (e.g. doubles).
 */
public interface PhyphoxDataTransformer<T> {
	
	public T transform(String rawInput);
}