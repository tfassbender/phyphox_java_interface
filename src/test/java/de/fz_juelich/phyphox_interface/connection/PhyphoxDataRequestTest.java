package de.fz_juelich.phyphox_interface.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class PhyphoxDataRequestTest {
	
	@Test
	public void testGetAsString() {
		List<String> buffers = Arrays.asList(new String[] {"buffer_x", "buffer_y", "time"});
		List<String> offsets = Arrays.asList(new String[] {"0", "2", "42"});
		
		PhyphoxDataRequest request = new PhyphoxDataRequest(buffers, offsets);
		
		assertEquals("buffer_x=0&buffer_y=2&time=42", request.getAsString());
	}
	
	@Test
	public void testGetAsString_noEnoughOffsetValues() {
		List<String> buffers = Arrays.asList(new String[] {"buffer_x", "buffer_y", "time"});
		List<String> offsets = Arrays.asList(new String[] {"0", "2"});
		
		assertThrows(IllegalArgumentException.class, () -> new PhyphoxDataRequest(buffers, offsets));
	}
	
	@Test
	public void testNullValues() {
		List<String> buffers = Arrays.asList(new String[] {"buffer_x", "buffer_y", "time"});
		List<String> offsets = Arrays.asList(new String[] {"0", null, "42"});
		
		PhyphoxDataRequest request = new PhyphoxDataRequest(buffers, offsets);
		
		assertEquals("buffer_x=0&buffer_y=full&time=42", request.getAsString());
	}
}