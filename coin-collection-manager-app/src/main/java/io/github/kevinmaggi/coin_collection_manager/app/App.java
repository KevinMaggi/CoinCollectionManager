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
@Command(name = "Coin Collection Manager", version = "Coin Collection Manager v1.0.0", mixinStandardHelpOptions = true)
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

	private EntityManagerFactory emf;
	private EntityManager em;

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
				LOGGER.info("Connecting to DB");
				String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbUrl, dbPort, dbName);

				Map<String, String> propertiesOverriding = new HashMap<>();
				propertiesOverriding.put("jakarta.persistence.jdbc.url", jdbcUrl);
				propertiesOverriding.put("jakarta.persistence.jdbc.user", dbUser);
				propertiesOverriding.put("jakarta.persistence.jdbc.password", dbPassword);

				emf = Persistence.createEntityManagerFactory("postgres", propertiesOverriding);
				em = emf.createEntityManager();

				LOGGER.info("Connected to DB");

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

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Closing connection with DB");
				if (em != null && em.isOpen())
					em.close();
				if (emf != null && emf.isOpen())
					emf.close();
				LOGGER.info("Connection closed");
			}
		});

		return null;
	}
}
