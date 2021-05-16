package pong;

import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball extends Circle {
	private AudioClip bounce = new AudioClip( getClass().getResource("bounce.wav").toExternalForm() );
	public double speed = ThreadLocalRandom.current().nextDouble( 10, 20 ), angle = ThreadLocalRandom.current().nextDouble( 0, 361 );
	
	public Ball() {
		super( ThreadLocalRandom.current().nextDouble( 10, 20 ) );
		setAngle();
	}
	
	public Ball( double radius ) {
		super( radius );
		setAngle();
	}
	
	
	public Ball( double x, double y, double radius ) {
		super( x, y, radius );
		setAngle();
		setFill( Color.BLACK );
	}
	
	private void setAngle() {
		while ( ( angle >= 70 && angle <= 110 ) || ( angle >= 250 && angle <= 290 ) )
			angle = ThreadLocalRandom.current().nextDouble( 0, 361 );
	}
	
	public void move() {
		double a = Math.toRadians( angle );
		setCenterX( getCenterX() + ( speed * Math.sin( a ) ) );
		setCenterY( getCenterY() + ( speed * Math.cos( a ) ) );
		
		if ( getCenterX() - getRadius() < 0 || getCenterX() + getRadius() >= Pong.WIDTH ) {
			bounce.play();
			angle = 360 - angle;
		}
		
		/*if ( getCenterY() - getRadius() < 0 || getCenterY() + getRadius() >= Pong.HEIGHT ) {
			bounce.play();
			angle = 180 - angle;
		}*/
	}
}