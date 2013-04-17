package com.omni;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class Controller implements Runnable {
	
	private static Logger logger = Logger.getLogger(Controller.class.getName());
	private static ExecutorService pool = Executors.newCachedThreadPool();
	private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private static Omni window;
	
	public static void main(String[] args) {
		logger.info("Launching omni bar");
		window = new Omni(300);
		register(new Controller());
		try {
			window.setIconImage(ImageIO.read(Controller.class.getClassLoader().getResourceAsStream("res/icon.png")));
		} catch(Exception e) {
			e.printStackTrace();
		}
		register(window);
	}
	
	public static void register(Runnable runnable) {
		logger.info("Registered: " + runnable.getClass().getCanonicalName());
		pool.submit(runnable);
	}

	@Override
	public void run() {
		Words.init(this.getClass().getClassLoader().getResourceAsStream("res/omni.i"));
		while(true) {
			try {
				Thread.sleep(100L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static ScheduledExecutorService getService() {
		return service;
	}
	

}
