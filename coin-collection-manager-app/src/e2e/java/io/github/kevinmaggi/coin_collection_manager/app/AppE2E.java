package io.github.kevinmaggi.coin_collection_manager.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class AppE2E extends AssertJSwingJUnitTestCase {
	// Test variables
	private Album ALBUM_COMM_1, ALBUM_COMM_2;
	private Coin COIN_COMM_1, COIN_COMM_2;

	// Tests
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	private FrameFixture window;

	@BeforeClass
	public static void setUpTestCase() {
		Map<String, String> propertiesOverriding = new HashMap<>();
		propertiesOverriding.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:${postgres.port}/collection");
		propertiesOverriding.put("jakarta.persistence.jdbc.user", "postgres-user");
		propertiesOverriding.put("jakarta.persistence.jdbc.password", "postgres-password");
		
		emf = Persistence.createEntityManagerFactory("postgres", propertiesOverriding);
	}
	
	@Override
	protected void onSetUp() {
		// Ensure to start every test with an empty database
		em = emf.createEntityManager();
		
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();
		
		// Populate database
		populateDB();
		
		// Start application passing arguments
		application("io.github.kevinmaggi.coin_collection_manager.app.App")
			.withArgs(
				"--postgres-port=" + System.getProperty("postgres.port")
				// The other arguments use default values
			)
			.start();
		
		// Get reference
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Coin Collection Manager".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}
	
	@Test @GUITest
	public void testThatOnStartAllDatabaseElementsArePresent() {
		assertThat(window.list("albumList").contents())
			.anySatisfy(e -> assertThat(e).contains(ALBUM_COMM_1.toString()))
			.anySatisfy(e -> assertThat(e).contains(ALBUM_COMM_2.toString()));
		
		assertThat(window.list("coinList").contents())
			.anySatisfy(e -> assertThat(e).contains(COIN_COMM_1.toString()))
			.anySatisfy(e -> assertThat(e).contains(COIN_COMM_2.toString()));
	}
	
	@Override
	protected void onTearDown() throws Exception {
		em.clear();
		em.close();
	}
	
	@AfterClass
	public static void cleanTestCase() {
		emf.close();
	}

	private void populateDB() {
		ALBUM_COMM_1 = new Album("Euro commemorativi", 1, "Armadio", 50, 0);
		ALBUM_COMM_2 = new Album("Euro commemorativi", 2, "Armadio", 50, 0);
		
		em.getTransaction().begin();
		em.persist(ALBUM_COMM_1);
		em.persist(ALBUM_COMM_2);
		em.getTransaction().commit();
		
		COIN_COMM_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", ALBUM_COMM_1.getId());
		COIN_COMM_2 = new Coin(Grade.AG, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", ALBUM_COMM_1.getId());
		
		em.getTransaction().begin();
		em.persist(COIN_COMM_1);
		em.persist(COIN_COMM_2);
		em.getTransaction().commit();
		
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
	}
}
