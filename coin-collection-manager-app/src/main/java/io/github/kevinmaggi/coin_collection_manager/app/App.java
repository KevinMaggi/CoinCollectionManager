package io.github.kevinmaggi.coin_collection_manager.app;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void> {
	
	private static final Logger LOGGER = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		LOGGER.info("Starting app");
		new CommandLine(new App()).execute(args);
	}

	@Override
	public Void call() {
		EventQueue.invokeLater(() -> {
			try {
				
			} catch (Exception e) {
				LOGGER.error(() -> "Something went wrong");
				LOGGER.debug(() -> String.format("Caught Exception: %s", ExceptionUtils.getStackTrace(e)));
			}
		});
		return null;
	}
}
