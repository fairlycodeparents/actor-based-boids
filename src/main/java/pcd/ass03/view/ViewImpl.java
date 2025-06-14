package pcd.ass03.view;


import akka.actor.ActorRef;
import pcd.ass03.actors.SupervisorActor;
import pcd.ass03.actors.SupervisorActor.Weights;
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
	private final JFrame frame;
	private final JSlider cohesionSlider, separationSlider, alignmentSlider;
	private JButton stopButton, pauseButton;
	private boolean isPaused = false;
	private ActorRef supervisorActor, viewActor;

	/**
	 * Constructor for the BoidsView class.
	 */
	public ViewImpl() {
		this.frame = setFrame();

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
        this.cohesionSlider = makeSlider();
        this.separationSlider = makeSlider();
        this.alignmentSlider = makeSlider();
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

	@Override
	public void start() {
		supervisorActor.tell(new SupervisorActor.StartMsg(this.getBoidCountFromUser(frame), SIDE_SIZE), ActorRef.noSender());
	}

	@Override
	public void updatePauseState(boolean isPaused) {
		this.isPaused = isPaused;
		pauseButton.setText(this.isPaused ? "Resume" : "Pause");
		stopButton.setEnabled(!this.isPaused);
	}

	private Integer getBoidCountFromUser(JFrame frame) {
		String input;
		do {
			input = JOptionPane.showInputDialog(
					frame,
					"Insert number of boids:",
					"Input",
					JOptionPane.QUESTION_MESSAGE
			);
			if (input == null) {	// Handles the "cancel" button
				frame.dispose();
				System.exit(0);
			}
		} while (input.isEmpty());
		return Integer.parseInt(input);
	}

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		this.stopButton = new JButton("Stop");
		this.stopButton.addActionListener(e ->
				supervisorActor.tell(new SupervisorActor.StopMsg(), ActorRef.noSender()));
		this.pauseButton = new JButton("Pause");
		this.pauseButton.addActionListener(e -> supervisorActor.tell(
                this.isPaused ? new SupervisorActor.ResumeMsg() : new SupervisorActor.PauseMsg(),
                ActorRef.noSender()
        ));
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
		if (e.getSource() == this.separationSlider) {
			var val = this.separationSlider.getValue();
			this.supervisorActor.tell(
					new SupervisorActor.UpdateWeightsMsg(Weights.SEPARATION, 0.1 * val),
					ActorRef.noSender()
			);
		} else if (e.getSource() == this.cohesionSlider) {
			var val = this.cohesionSlider.getValue();
			this.supervisorActor.tell(
					new SupervisorActor.UpdateWeightsMsg(Weights.COHESION, 0.1 * val),
					ActorRef.noSender()
			);
		} else {
			var val = this.alignmentSlider.getValue();
			this.supervisorActor.tell(
					new SupervisorActor.UpdateWeightsMsg(Weights.ALIGNMENT, 0.1 * val),
					ActorRef.noSender()
			);
		}
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
