package de.fz_juelich.phyphox_interface.data;

import java.util.List;

/**
 * Transforms the raw data, received from the phone (as Strings), into usable data (e.g. doubles).
 */
public interface PhyphoxDataTransformer<T> {
	
	public T transform(String rawInput);
	public List<T> transform(List<String> rawinput);
}