package kdc.leaplight.control

class LightState {
	
	public enum Brightness {
		BRIGHT(value: 255), DIM(value: 46)
		
		final int value
	}
	
	public enum Color {
		WHITE(x: 0.4443f, y: 0.4064f), BLUE(x: 0.2255f, y: 0.1555f)
		
		final float x
		final float y
	}
	
	final Brightness brightness
	final Color color
		
	public LightState(Brightness brightness, Color color) {
		this.brightness = brightness
		this.color = color
	}

	public boolean isDifferentThan(LightState otherLightState) {
		return !equals(otherLightState)
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof LightState)) return false
		if(this.is(other)) return true
		return brightness == other.brightness && color == other.color
	}

	@Override
	public int hashCode() {
		return brightness.hashCode() * color.hashCode()
	}
}
