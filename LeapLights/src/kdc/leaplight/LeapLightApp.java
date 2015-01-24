package kdc.leaplight;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kdc.leaplight.control.LightController;
import kdc.leaplight.control.LightState;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;

public class LeapLightApp {

	private static final float MIN_PINCH_STRENGTH = 0.3f;
	private static final float MIN_FINGERTIP_DISTANCE = 50.0f;
	
	private final LightController lightController = new LightController();
	private boolean running = true;

	private LightState currentLightState = new LightState(LightState.Brightness.BRIGHT, LightState.Color.WHITE);
	private LightState pendingLightState = null;
	private int framesPending = 0;

	private static final int REFRESH_RATE = 100;

	public static void main(String[] args) {
		new LeapLightApp().run();
	}

	private void run() {
		System.out.println("Running");
		Controller controller = new Controller();

		while (running) {
			LightState newLightState = getNewLightState(controller.frame());
			if (newLightState.isDifferentThan(currentLightState)) {
				if (newLightState.isDifferentThan(pendingLightState)) {
					pendingLightState = newLightState;
					framesPending = 0;
				} else if (++framesPending > 5) {
					lightController.setLightsTo(newLightState);
					currentLightState = newLightState;
					framesPending = 0;
				}
			}

			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private LightState getNewLightState(Frame frame) {
		Map<Finger.Type, Float> pinchingFingerTypes = getPinchingFingerTypes(frame);
		if (pinchingFingerTypes.size() > 0) System.out.println("Pinching fingers: " + pinchingFingerTypes);
		return createNewLightStateFor(pinchingFingerTypes);
	}
	
	private Map<Finger.Type, Float> getPinchingFingerTypes(Frame frame) {
		Map<Finger.Type, Float> pinchingFingers = new HashMap<Finger.Type, Float>();
		for (Hand hand : frame.hands()) {
			if (hand.pinchStrength() > 0.25f) System.out.println("Pinch strength:  " + hand.pinchStrength());
			if (hand.pinchStrength() < MIN_PINCH_STRENGTH) continue;
			Finger thumb = findThumb(hand);
			if (thumb == null) continue;
			for (Finger finger : findFingers(hand)) {
				float distance = finger.stabilizedTipPosition().distanceTo(thumb.stabilizedTipPosition());
				System.out.println("Distance (" + finger.type() + "): " + distance + " isExtended: " + finger.isExtended());
				if (distance < MIN_FINGERTIP_DISTANCE) {
					pinchingFingers.put(finger.type(), distance);
				}
			}
		}
		return pinchingFingers;
	}
	
	private Finger findThumb(Hand hand) {
		for (Finger finger : hand.fingers()) {
			if (finger.type() == Finger.Type.TYPE_THUMB) return finger;
		}
		return null;
	}

	private Set<Finger> findFingers(Hand hand) {
		Set<Finger> fingers = new HashSet<Finger>();
		for (Finger finger : hand.fingers()) {
			if (finger.isValid() && finger.type() != Finger.Type.TYPE_THUMB && finger.type() != Finger.Type.TYPE_PINKY) {
				fingers.add(finger);
			}
		}
		return fingers;
	}

	private LightState createNewLightStateFor(Map<Finger.Type, Float> pinchingFingers) {
		if (pinchingFingers.size() == 0) return currentLightState;
		switch (findFingerTypeWithShortestDistance(pinchingFingers)) {
			case TYPE_INDEX:
				return new LightState(LightState.Brightness.BRIGHT, LightState.Color.WHITE);
			case TYPE_MIDDLE:
				return new LightState(LightState.Brightness.DIM, LightState.Color.WHITE);
			case TYPE_RING:
				return new LightState(LightState.Brightness.DIM, LightState.Color.BLUE);
			default:
				return currentLightState;
		}
	}

	private Finger.Type findFingerTypeWithShortestDistance(Map<Finger.Type, Float> pinchingFingers) {
		Map.Entry<Finger.Type, Float> entryWithShortestDistance = pinchingFingers.entrySet().iterator().next();
		for (Map.Entry<Finger.Type, Float> mapEntry : pinchingFingers.entrySet()) {
			if (mapEntry.getValue().compareTo(entryWithShortestDistance.getValue()) < 0) {
				entryWithShortestDistance = mapEntry;
			}
		}
		return entryWithShortestDistance.getKey();
	}

}
