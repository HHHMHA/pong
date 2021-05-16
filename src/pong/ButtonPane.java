package pong;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;

class ButtonPane extends StackPane {
	private Label lbl;
	@SuppressWarnings("unused")
	private Image img1, img2;
	private ImageView img;
	private AudioClip a1, a2;
	
	public ButtonPane( String txt, Image img1, Image img2, AudioClip a1, AudioClip a2 ) {
		this.lbl = new Label( txt );
		this.img1 = img1;
		this.img2 = img2;
		this.a1 = a1;
		this.a2 = a2;
		img = new ImageView( img1 );
		img.setFitWidth( Pong.WIDTH - 12 );
		
		setOnMouseEntered( e -> {
			setCursor( Cursor.HAND );
			img.setImage( img2 );
			if ( a1 != null )
				a1.play();
		});
		
		setOnMouseExited( e -> {
			setCursor( Cursor.DEFAULT );
			img.setImage( img1 );
		});
		
		setOnMousePressed( e -> {
			if ( a2 != null )
			a2.play();
		} );
		
		getChildren().addAll( img, lbl );
	}
	
	public ImageView getImage() {
		return img;
	}
	
	public void setA1( AudioClip a1 ) {
		this.a1 = a1;
	}
	
	public void setA2( AudioClip a2 ) {
		this.a2 = a2;
	}
	
	public AudioClip getA1() {
		return a1;
	}
	
	public AudioClip getA2() {
		return a2;
	}
	
	public void setOnAction( EventHandler< MouseEvent > e ) {
		setOnMouseClicked( e );
	}
}