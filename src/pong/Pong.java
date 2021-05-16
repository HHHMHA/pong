package pong;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.concurrent.ThreadLocalRandom;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
    javafx.scene.control.Dialog
    javafx.scene.control.Alert
    javafx.scene.control.TextInputDialog
    javafx.scene.control.ChoiceDialog
*/

// more sounds and FX
// random presents for player ( increase paddle size, slow ball, increase ball speed, make ball smaller/bigger)
// LAN play
// FIX port Issue

public class Pong extends Application {
	private AudioClip hit = new AudioClip(getClass().getResource("hit.wav").toExternalForm());
	private AudioClip exp = new AudioClip(getClass().getResource("Exp.wav").toExternalForm());
	private AudioClip onBtn = new AudioClip(getClass().getResource("onButton.wav").toExternalForm());
	private AudioClip clickBtn = new AudioClip(getClass().getResource("ClickButton.wav").toExternalForm());
	private Image normalBtn = new Image( getClass().getResource("btn.png").toExternalForm() );
	private Image mouseBtn =  new Image( getClass().getResource("btnm.png").toExternalForm() );
	private Ball b;
	private IntegerProperty score1 = new SimpleIntegerProperty(0), score2 = new SimpleIntegerProperty(0);
	public static final double WIDTH = 400, HEIGHT = 400;
	private boolean pause;
	private Timeline animation;
	private Pane p;
	private StackPane stackPane;
	private VBox mainMenu, LANMenu, hostMenu, guestMenu;
	private Scene scene;
	private Paddle pad1, pad2;
	private Thread coolDown1, coolDown2;
	private Thread AI;
	private Host host;
	private TextArea hostAddressArea;
	// private Client guest;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		pause = false;
		stackPane = new StackPane();

		ImageView img = new ImageView(getClass().getResource("img.jpg").toExternalForm());
		scene = new Scene(stackPane, WIDTH, HEIGHT);
		scene.setCursor( Cursor.cursor( getClass().getResource("mouse.png").toExternalForm() ) );
		
		initMainMenu();
		initLANMenu();
		initHostMenu();
		// initGuestMneu();
		initAnimation();
		stackPane.getChildren().addAll( img, mainMenu, LANMenu, hostMenu );
		
		primaryStage.setResizable(true);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void initMainMenu() {
		mainMenu = new VBox();
		mainMenu.resize(WIDTH, HEIGHT);
		p = new Pane();
		ButtonPane btn = new ButtonPane("Local", normalBtn, mouseBtn, onBtn , clickBtn);
		ButtonPane btn2 = new ButtonPane("LAN", normalBtn, mouseBtn, onBtn , clickBtn);
		ButtonPane btn3 = new ButtonPane("AI", normalBtn, mouseBtn, onBtn , clickBtn);
		ButtonPane btn4 = new ButtonPane("Exit", normalBtn, mouseBtn, onBtn , clickBtn);
		
		btn.setOnAction( e -> {
			score1.set( 0 ); score2.set( 0 );
			playGame( 1 );
		});
		btn2.setOnAction( e -> {
			score1.set( 0 ); score2.set( 0 );
			LANMenu.setVisible( true );
			mainMenu.setVisible( false );
		});
		btn3.setOnAction(e -> {
			score1.set( 0 ); score2.set( 0 );
			playGame( 3 );
		});
		btn4.setOnAction( e -> System.exit( 0 ) );
		mainMenu.getChildren().addAll( btn, btn2, btn3, btn4 );
		mainMenu.setPadding( new Insets( 10, 10, 10, 10 ) );
	}
	
	void initLANMenu() {
		LANMenu = new VBox();
		LANMenu.setVisible( false );
		
		ButtonPane btn = new ButtonPane("Host", normalBtn, mouseBtn, onBtn , clickBtn);
		ButtonPane btn2 = new ButtonPane("Guest", normalBtn, mouseBtn, onBtn , clickBtn);
		ButtonPane btn3 = new ButtonPane("Back", normalBtn, mouseBtn, onBtn , clickBtn);
		
		btn.setOnAction( e -> {
			LANMenu.setVisible( false );
			mainMenu.setVisible( false );
			hostMenu.setVisible( true );
			host.start();
			hostAddressArea.setText( host.getIP() + host.server.getLocalPort() );
		});	
		
		btn3.setOnAction( e -> {
			LANMenu.setVisible( false );
			mainMenu.setVisible( true );
		});
		
		LANMenu.getChildren().addAll( btn, btn2, btn3 );
		LANMenu.setPadding( new Insets( 10, 10, 10, 10 ) );
	}
	
	private void initHostMenu() throws Exception {
		host = new Host();
		hostMenu = new VBox();
		hostAddressArea = new TextArea();
		hostAddressArea.setEditable( false );
		hostMenu.setVisible( false );
		
		ButtonPane btn = new ButtonPane("Back", normalBtn, mouseBtn, onBtn , clickBtn);
		btn.setOnAction( e -> {
			hostMenu.setVisible( false );
			LANMenu.setVisible( true );
			mainMenu.setVisible( false );
			try {
				host.server.close();
				host.interrupt();
				host = new Host();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		hostMenu.getChildren().addAll( btn, hostAddressArea );
		hostMenu.setPadding( new Insets( 10, 10, 10, 10 ) );
	}

	private void result() {
		Alert alert = new Alert(AlertType.INFORMATION,
				"Player " + (score1.get() > score2.get() ? "1" : "2") + " won!" );
		alert.show();
	}

	private void playGame( int btn ) throws IllegalArgumentException {
		p.getChildren().clear();
		coolDown1 = new Thread( () -> {
			try {
				Platform.runLater( new Runnable() {
					public void run() {
						pad1.setFill( Color.BLACK );
					}
				});
				Thread.sleep(5000);
				Platform.runLater( new Runnable() {
					public void run() {
						pad1.setFill( Color.ORANGE );
					}
				});
			} catch (InterruptedException e1) {}				
		} );
		coolDown1.start();
		coolDown2 = new Thread( () -> {
			try {
				Platform.runLater( new Runnable() {
					public void run() {
						pad2.setFill( Color.BLACK );
					}
				});
				Thread.sleep(5000);
				Platform.runLater( new Runnable() {
					public void run() {
						pad2.setFill( Color.ORANGE );
					}
				});
			} catch (InterruptedException e1) {
			}
		} );
		coolDown2.start();
		scene.setCursor( Cursor.NONE );
		mainMenu.setVisible( false );
		Label lbl1 = new Label(), lbl2 = new Label();
		lbl2.setLayoutX(WIDTH - 10);
		lbl2.setLayoutY(0);
		lbl1.textProperty().bind(score1.asString());
		lbl2.textProperty().bind(score2.asString());
		b = new Ball( 200, 200, 10 );
		pad1 = new Paddle(); pad2 = new Paddle();
		pad1.setY(15);
		pad2.setY(HEIGHT - 5 - 15);
		stackPane.getChildren().add(p);
		p.setOnMouseMoved(e -> {
			if (!pause)
				pad1.setX(e.getSceneX() - 15);
		});
		
		p.getChildren().addAll(b, pad1, pad2, lbl1, lbl2);
		switch( btn ) {
		case 1: initHuman2(); break;
		case 3: initAI(); break;
		default: throw new IllegalArgumentException();
		}

		
		p.setOnMouseClicked( e -> {
			if ( coolDown1.getState() == State.TERMINATED ) {
				if ( e.getButton() == MouseButton.SECONDARY )
					b.angle = 180;
				else if ( e.getButton() == MouseButton.PRIMARY )
					b.angle = 0;
				coolDown1 = new Thread( () -> {
					try {
						Platform.runLater( new Runnable() {
							public void run() {
								pad1.setFill( Color.BLACK );
							}
						});
						Thread.sleep(5000);
						Platform.runLater( new Runnable() {
							public void run() {
								pad1.setFill( Color.ORANGE );
							}
						});
					} catch (InterruptedException e1) {}				
				} );
				coolDown1.start();
			}
		});
		
		animation.playFromStart();
	}
	
	private void initHuman2() {
		scene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.LEFT && pad2.getX() > 0 && !pause)
				pad2.setX(pad2.getX() - 20);
			else if (e.getCode() == KeyCode.RIGHT && pad2.getX() + pad2.getWidth() < WIDTH && !pause)
				pad2.setX(pad2.getX() + 20);
			else if (e.getCode() == KeyCode.P)
				pause = !pause;
			else if ( coolDown2.getState() == State.TERMINATED ) {
				if ( e.getCode() == KeyCode.DOWN )
					b.angle = 0;
				else if ( e.getCode() == KeyCode.UP )
					b.angle = 180;
				coolDown2 = new Thread( () -> {
					try {
						Platform.runLater( new Runnable() {
							public void run() {
								pad2.setFill( Color.BLACK );
							}
						});
						Thread.sleep(5000);
						Platform.runLater( new Runnable() {
							public void run() {
								pad2.setFill( Color.ORANGE );
							}
						});
					} catch (InterruptedException e1) {
					}
				} );
				coolDown2.start();
			}
		});
	}
	
	private void initAI() {
		scene.setOnKeyPressed( e-> {
			if ( e.getCode() == KeyCode.P )
				pause = !pause;
		});
		AI = new Thread( () -> {
			while ( true ) {
				Platform.runLater( () -> {
					if ( b.getCenterX() < pad2.getX() )
						pad2.setX( pad2.getX() - 20 );
					else if ( b.getCenterX() > pad2.getX() + pad2.getWidth() )
						pad2.setX(pad2.getX() + 20);
				});
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					break;
				}
			}
		});
		AI.start();
	}
	
	private void initAnimation() {
		animation = new Timeline(new KeyFrame(Duration.millis(30), e -> {
			if (!pause) {
				b.move();
				if (score1.get() >= 10 || score2.get() >= 10) {
					if ( AI != null )
						AI.interrupt();
					animation.stop();
					result();
					p.getChildren().clear();
					stackPane.getChildren().remove( p );
					scene.setCursor( Cursor.cursor( getClass().getResource("mouse.png").toExternalForm() ) );
					mainMenu.setVisible( true );
				}
				if (b.getCenterY() + b.getRadius() < 0) {
					score2.set(score2.get() + 1);
					p.getChildren().remove(b);
					b = new Ball(200, 200, 10);
					if ( score2.get() == 9 || score1.get() == 9 )
						fireBall();
					p.getChildren().add(b);
				} else if (b.getCenterY() - b.getRadius() >= HEIGHT) {
					score1.set(score1.get() + 1);
					p.getChildren().remove(b);
					b = new Ball(200, 200, 10);
					if ( score2.get() == 9 || score1.get() == 9 )
						fireBall();
					p.getChildren().add(b);
				} else if (b.getCenterX() >= pad1.getX() && b.getCenterX() <= pad1.getX() + 100
						&& b.getCenterY() - b.getRadius() <= pad1.getY() + 5) {
					if ( b.getFill() == Color.ORANGE ) {
						p.getChildren().remove( pad1 );
						new Thread( () -> {
							try {
								Thread.sleep( 1000 );
								Platform.runLater( new Runnable() {
									
									@Override
									public void run() {
										p.getChildren().add( pad1 );
									}
								});
							} catch (InterruptedException e1) {}
						} ).start();
						exp.play();
					}
					else
						hit.play();
					b.angle = 180 - b.angle + + ThreadLocalRandom.current().nextDouble(-20, 20);
					b.setCenterY(pad1.getY() + 5 + b.getRadius() + 1);
				} else if (b.intersects(pad2.getBoundsInLocal())) {
					if ( b.getFill() == Color.ORANGE ) {
						p.getChildren().remove( pad2 );
						new Thread( () -> {
							try {
								Thread.sleep( 1000 );
								Platform.runLater( new Runnable() {
									
									@Override
									public void run() {
										p.getChildren().add( pad2 );
									}
								});
							} catch (InterruptedException e1) {}
						} ).start();
						exp.play();
					}
					else
						hit.play();
					b.angle = 180 - b.angle + ThreadLocalRandom.current().nextDouble(-20, 20);
					b.setCenterY(pad2.getY() - 5 - b.getRadius() - 1);
				}
			}
		}));
		animation.setCycleCount(Timeline.INDEFINITE);
	}
	
	private void fireBall() {
		b.setFill( Color.ORANGE );
		b.setEffect( new Bloom( 0.1 ) );
	}

}

/*
 * public class Pong extends Application { public static Connection con = null;
 * public static void main(String[] args) { launch(args); }
 * 
 * @Override public void start( Stage primaryStage ) { new JDBC(); ResultSet rs;
 * String url = "jdbc:sqlite:LeaderBoard.db"; try { con =
 * DriverManager.getConnection( url ); Statement st = con.createStatement();
 * st.execute("CREATE TABLE IF NOT EXISTS Leaders (\r\n" +
 * "    ID        INTEGER PRIMARY KEY ASC AUTOINCREMENT,\r\n" +
 * "    FirstName TEXT    NOT NULL\r\n" +
 * "                      CHECK (length(FirstName) > 0),\r\n" +
 * "    LastName  TEXT    NOT NULL\r\n" +
 * "                      CHECK (length(LastName) > 0),\r\n" +
 * "    Score     BIGINT  NOT NULL\r\n" + ");"); //rs =
 * st.executeQuery("SELECT * FROM Leaders ORDER BY Score DESC"); } catch
 * (SQLException e) { e.printStackTrace(); }
 * 
 * primaryStage.show(); }
 * 
 * }
 */