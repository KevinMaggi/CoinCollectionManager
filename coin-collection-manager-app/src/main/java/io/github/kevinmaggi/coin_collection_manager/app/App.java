package io.github.kevinmaggi.coin_collection_manager.app;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.AlbumTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.CoinTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql.PostgresTransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.view.swing.SwingView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This class is the main class for the application.
 */
@Command(name = "Coin Collection Manager", version = "Coin Collection Manager v0.0.1-SNAPSHOT", mixinStandardHelpOptions = true)
public class App implements Callable<Void> {
	
	private static final Logger LOGGER = LogManager.getLogger(App.class);
	
	@Option(names = { "--postgres-url" }, description = "Postgres connection url")
	private String dbUrl = "localhost";
	
	@Option(names = { "--postgres-port" }, description = "Postgres connection port")
	private String dbPort = "5432";
	
	@Option(names = { "--postgres-db" }, description = "Postgres DB")
	private String dbName = "collection";

	@Option(names = { "--postgres-user" }, description = "Postgres DB user")
	private String dbUser = "postgres-user";

	@Option(names = { "--postgres-password" }, description = "Postgres DB password")
	private String dbPassword = "postgres-password";

	/**
	 * Starts the application with arguments.
	 * 
	 * @param args		arguments
	 */
	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}

	/**
	 * Run the application.
	 */
	@Override
	public Void call() {
		LOGGER.info("Starting app");
		EventQueue.invokeLater(() -> {
			try {
				String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbUrl, dbPort, dbName);
				
				Map<String, String> propertiesOverriding = new HashMap<>();
				propertiesOverriding.put("javax.persistence.jdbc.url", jdbcUrl);
				propertiesOverriding.put("javax.persistence.jdbc.user", dbUser);
				propertiesOverriding.put("javax.persistence.jdbc.password", dbPassword);
				
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgres", propertiesOverriding);
				EntityManager em = emf.createEntityManager();
				
				PostgresTransactionManagerFactory tmf = new PostgresTransactionManagerFactory(em);
				
				AlbumManager am = new AlbumTransactionalManager(tmf.getTransactionManager());
				CoinManager cm = new CoinTransactionalManager(tmf.getTransactionManager());
				
				SwingView view = new SwingView();
				
				AlbumPresenter ap = new AlbumPresenter(view, am);
				CoinPresenter cp = new CoinPresenter(view, cm, am);
				
				view.setPresenters(cp, ap);
				view.setVisible(true);
				LOGGER.info("App started");
			} catch (Exception e) {
				LOGGER.error(() -> "Something went wrong");
				LOGGER.debug(() -> String.format("Caught Exception: %s", ExceptionUtils.getStackTrace(e)));
			}
		});
		return null;
	}
}
