package pcd.ass03.view;


import akka.actor.ActorRef;
import pcd.ass03.actors.SupervisorActor;
import pcd.ass03.model.Boid;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;

/**
 * The BoidsView class is responsible for creating the graphical user interface (GUI) for the Boids simulation.
 * It allows users to interact with the simulation, including starting, pausing, and stopping it.
 * The view also provides sliders to adjust the weights of the boids behaviors (cohesion, separation, and alignment).
 */
public class ViewImpl implements ChangeListener, View {

	private final static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	private final static int SIDE_SIZE = Math.min(SCREEN_SIZE.width, SCREEN_SIZE.height) * 4 / 5;

	private final BoidsPanel boidsPanel;
	private boolean isPaused = false;
	private ActorRef supervisorActor, viewActor;

	/**
	 * Constructor for the BoidsView class.
	 */
	public ViewImpl() {
        JFrame frame = setFrame();

		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());

		// Create a panel for the buttons (stop and pause/resume)
		JPanel buttonsPanel = getButtonsPanel();
		cp.add(BorderLayout.NORTH, buttonsPanel);

		// Create a panel for the boids
		this.boidsPanel = new BoidsPanel(frame.getWidth(), frame.getHeight());
		cp.add(BorderLayout.CENTER, boidsPanel);

		// Create a panel for the sliders
        JPanel slidersPanel = new JPanel();
        JSlider cohesionSlider = makeSlider();
        JSlider separationSlider = makeSlider();
        JSlider alignmentSlider = makeSlider();
        slidersPanel.add(new JLabel("Separation"));
		slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
		slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
		slidersPanel.add(cohesionSlider);
		cp.add(BorderLayout.SOUTH, slidersPanel);

        frame.setContentPane(cp);
        frame.setVisible(true);
    }

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(e ->
				supervisorActor.tell(new SupervisorActor.StopMsg(), ActorRef.noSender())
		);
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener(e -> {
			supervisorActor.tell(
					this.isPaused ? new SupervisorActor.ResumeMsg() : new SupervisorActor.PauseMsg(),
					ActorRef.noSender()
			);
			pauseButton.setText(this.isPaused ? "Pause" : "Resume");
			stopButton.setEnabled(this.isPaused);
			this.isPaused = !this.isPaused;
		});
		buttonsPanel.add(stopButton);
		buttonsPanel.add(pauseButton);
		return buttonsPanel;
	}

	private JFrame setFrame() {
		final JFrame frame;
		frame = new JFrame("Boids Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(SIDE_SIZE, SIDE_SIZE);
		frame.setResizable(false);
		return frame;
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);        
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);
		return slider;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO: Handle slider changes or put handler directly in the constructor (line 105)
	}

	@Override
	public void setSupervisorActor(ActorRef supervisorActor) {
		this.supervisorActor = supervisorActor;
	}

	@Override
	public void setViewActor(ActorRef viewActor) {
		this.viewActor = viewActor;
	}

	@Override
	public void render(int FPS, List<Boid> boids) {
		this.boidsPanel.setFrameRate(FPS);
		this.boidsPanel.setBoids(boids);
		this.boidsPanel.repaint();
	}
}
