package pong;

import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Paddle extends Rectangle {
	public Paddle() {
		super( 100, 5, Color.BLACK );
		setStroke( Color.GREEN );
		setStrokeWidth( 1.5 );
		setArcHeight( 1.0 );
		setArcWidth( 1.0 );
		Bloom bloom = new Bloom(); 
	    bloom.setThreshold(0.5); 
		setEffect( bloom );
	}
}