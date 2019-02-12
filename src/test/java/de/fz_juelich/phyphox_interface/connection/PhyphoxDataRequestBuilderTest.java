package de.fz_juelich.phyphox_interface.connection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class PhyphoxDataRequestBuilderTest {
	
	@Test
	public void test() {
		List<String> buffers = Arrays.asList(new String[] {"buffer_x", "buffer_y", "time"});
		List<String> offsets = Arrays.asList(new String[] {"0.0", "2.0", "42.0"});
		
		PhyphoxDataRequest request = new PhyphoxDataRequest(buffers, offsets);
		
		PhyphoxDataRequestBuilder builder = new PhyphoxDataRequestBuilder();
		builder.setBuffer(0, "buffer_x").setBuffer(1, "buffer_y").setBuffer(2, "time").setOffset(0, 0).setOffset(1, 2).setOffset(2, 42);
		
		PhyphoxDataRequest built = builder.build();
		
		assertEquals(request, built);
	}
	
	@Test
	public void testSetBufferOffset() {
		PhyphoxDataRequestBuilder builder = new PhyphoxDataRequestBuilder();
		builder.setBuffer(1, "y").setBuffer(0, "x").setOffset(0, 42);
		builder.setOffsetToBuffer(1, 0, 42);
		
		PhyphoxDataRequest request = builder.build();
		
		assertEquals("x=42.0&y=42.0|x", request.getAsString());
	}
}