package com.omni;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Omni extends JFrame implements MouseMotionListener, MouseListener,
		KeyListener, FocusListener, Runnable {

	private static Logger logger = Logger.getLogger(Omni.class.getName());
	private ScheduledFuture<?> motion;
	private Point oPoint;
	private RoundRectangle2D tBounds;
	private StringBuilder input;
	private Pattern allowed;
	private Theme mode;
	private Theme previous;
	private long previousSet;
	private boolean locked;
	private final int width;
	private String suggestion;

	private static final int COLLAPSE_LENGTH = 50;
	private static final double TRANSISTION_TIME = 500L;
	private static final int PADDING = 10;
	private static final double INPUT_ARC = 5D;
	private static final Font INPUT_FONT = new Font("sansserif", Font.PLAIN, 20);
	private static Word word;
	private static final float INTERVAL = 10.0F;

	public Omni(int width) {
		super("Omni bar");
		this.width = width;
		this.tBounds = new RoundRectangle2D.Double(PADDING, PADDING, width
				- (PADDING * 2), COLLAPSE_LENGTH - (PADDING * 2), INPUT_ARC,
				INPUT_ARC);
		this.input = new StringBuilder();
		this.allowed = Pattern.compile("[a-zA-Z0-9 ]");
		this.mode = Theme.DICT;
		setAlwaysOnTop(true);
		setUndecorated(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setOpacity(.95F);
		setLocationRelativeTo(null);
		setSize(width, 400);
		setFocusTraversalKeysEnabled(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addFocusListener(this);
		// motion(COLLAPSE_LENGTH, 5);
	}

	public void updateOutput() {

	}

	public void motion(final int height, final int interval) {
		if (motion != null) {
			logger.info("Cannot expand due to motion already occuring.");
			return;
		}
		final int resolve = Math.abs(interval)
				* (height >= getHeight() ? 1 : -1);
		motion = Controller.getService().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if ((Math.max(height, getHeight())
						- Math.min(height, getHeight()) <= (interval + 1))) {
					motion.cancel(true);
					motion = null;
				}
				setSize(getWidth(), getHeight() + resolve);
			}
		}, 0L, 10L, TimeUnit.MILLISECONDS);
	}

	@Override
	public void paint(Graphics g) {
		BufferedImage buffer = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = (Graphics2D) buffer.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		if (Words.isLoaded()) {
			
			int tX = 0;
			if (previous != null) {
				g2.setColor(previous.getBackground());
				long lapse = System.currentTimeMillis() - previousSet;
				if (lapse > TRANSISTION_TIME) {
					previous = null;
				} else {
					double percentage = (TRANSISTION_TIME - (double) lapse)
							/ TRANSISTION_TIME;
					tX = (int) (getWidth() * percentage);
				}
				g2.fillRect(getWidth() - tX, 0, getWidth(), getHeight());
			}
			g2.setColor(mode.getBackground());
			g2.fillRect(tX, 0, getWidth(), getHeight());
		} else {
			
			g2.setColor(new Color(mode.getBackground().getRed(), (int) (mode.getBackground().getGreen() * Words.getPercent()), (int) (mode.getBackground().getBlue() * Words
					.getPercent())));
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setColor(new Color(mode.getBackground().getRed(), mode.getBackground().getGreen(), (int) (mode.getBackground().getBlue() * Words
					.getPercent())));
			int tX = (int) (getWidth() * Words.getPercent());
			setSize((int) (width * Words.getPercent()) + 1, getHeight());
			g2.fillRect(getWidth() - tX, 0, getWidth(), getHeight());
		}
		g2.setColor(Color.WHITE);
		int y = COLLAPSE_LENGTH - PADDING;
		if (word != null) {
			FontMetrics metrics = g2.getFontMetrics();
			y = COLLAPSE_LENGTH + PADDING;
			int x = PADDING * 2;
			int maxX = getWidth() - PADDING * 2;
			if (word.getPronunciation() != null) {
				g2.drawString(
						word.getPronunciation(),
						getWidth() / 2
								- metrics.stringWidth(word.getPronunciation())
								/ 2, y);
				x = PADDING * 2;
				y += metrics.getHeight() + PADDING;
			}
			if (mode.equals(Theme.DICT)) {
				for (String string : word.getDefinitions()) {
					String[] division = string.split(" ");
					for (String append : division) {
						int width = metrics.stringWidth(append + " ");
						if (x + width >= maxX) {
							x = PADDING * 2;
							y += metrics.getHeight();
						}
						g2.drawString(append, x, y);
						x += width;
					}
					x = PADDING * 2;
					y += metrics.getHeight() + PADDING;
				}
			} else if (mode.equals(Theme.THES)) {
				if (!word.getSynonyms().isEmpty()) {
					x = PADDING * 4 + metrics.stringWidth(word.getText());
				}
				int storedY = y;
				int height = 0;
				int count = 0;
				for (String string : word.getSynonyms()) {
					if(count == 10) {
						break;
					}
					int width = metrics.stringWidth(string + "->");
					if (x + width >= maxX) {
						x = PADDING * 4 + metrics.stringWidth(word.getText());
						y += metrics.getHeight();
						height += metrics.getHeight();
					}
					g2.drawString(string, x, y);
					x += width;
					count++;
				}

				if (!word.getSynonyms().isEmpty()) {
					y += metrics.getHeight() + PADDING;
					x = PADDING * 2;
					int tY = (storedY += height / 2) - metrics.getHeight() / 2;
					g2.drawString(word.getText(), x, tY);
					g2.setColor(Color.YELLOW);
					g2.drawLine(x + metrics.stringWidth(word.getText()), tY
							- metrics.getHeight() / 4,
							x + metrics.stringWidth(word.getText()) + PADDING,
							tY);
					g2.drawLine(x + metrics.stringWidth(word.getText()), tY
							- metrics.getHeight() / 4,
							x + metrics.stringWidth(word.getText()) + PADDING,
							tY - metrics.getHeight() / 2);
				}
			} else {
				y -= word.getPronunciation() != null ? PADDING : 0;
			}
		}
		if (!(Math.max(y, getHeight()) - Math.min(y, getHeight()) <= (INTERVAL + 1))) {
			setSize(getWidth(), (int) (getHeight() + Math.abs(INTERVAL)
					* (y >= getHeight() ? 1 : -1)));
		}
		g2.setColor(Color.WHITE);
		g2.fill(tBounds);
		g2.setColor(Color.BLACK);
		g2.draw(tBounds);
		g2.setFont(INPUT_FONT);
		String text = input.toString() + "";
		float sY = (float) (tBounds.getY() + 22);
		float x = (float) (tBounds.getX() + PADDING / 2D);
		Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
		g2.drawString(text, x, sY);
		g2.setColor(Color.BLUE);
		if (suggestion != null)
			g2.drawString(suggestion, (float) (x + bounds.getWidth()), sY);
		g.drawImage(buffer, 0, 0, null);
	}

	@Override
	public void run() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setShape(new RoundRectangle2D.Double(0, 0, getWidth(),
						getHeight(), 15D, 15D));
			}
		});
		setVisible(true);
		while (true) {
			try {
				repaint();
				Thread.sleep(20L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			System.exit(-1);
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Character typed = e.getKeyChar();
		switch (typed) {
		case KeyEvent.VK_BACK_SPACE:
			if (input.length() > 0) {
				input.deleteCharAt(input.length() - 1);
				if (!Words.isDictionaryWord(input.toString()))
					suggestion = Words.ending(input.toString());
				else
					suggestion = "";
			}
			word = null;
			break;
		case KeyEvent.VK_ENTER:
			if (suggestion != null && suggestion.length() > 0) {
				input.append(suggestion);
				suggestion = "";
			}
			word = Words.getWord(input.toString());
			break;
		case KeyEvent.VK_TAB:
			previous = mode;
			previousSet = System.currentTimeMillis();
			mode = Theme.next(mode);
			break;
		}
		if (allowed.matcher(typed.toString()).matches()) {
			input.append(typed);
			String focus = input.substring(input.lastIndexOf(" ") + 1);
			word = Words.getWord(focus);
			suggestion = word == null ? Words.ending(focus) : "";
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		oPoint = e.getPoint();
		locked = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		locked = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (locked) {
			int dX = e.getPoint().x - oPoint.x;
			int dY = e.getPoint().y - oPoint.y;
			setLocation(getX() + dX, getY() + dY);
		}
	}

	public void stopExpansion() {
		if (motion != null) {
			motion.cancel(true);
			motion = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
	}

	public Theme getMode() {
		return mode;
	}

	public void setMode(Theme mode) {
		this.mode = mode;
	}

}
