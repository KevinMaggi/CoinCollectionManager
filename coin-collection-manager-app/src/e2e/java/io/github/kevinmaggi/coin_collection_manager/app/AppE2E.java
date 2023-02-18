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

import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class AppE2E extends AssertJSwingJUnitTestCase {
	// Test variables
	private final String ALBUM_NAME = "Euro commemorative";
	private final String ALBUM_LOCATION = "Armadio";
	private final int ALBUM_SLOTS = 50;
	
	private final Grade COIN_GRADE = Grade.AG;
	private final String COIN_1_COUNTRY = "Italy";
	private final String COIN_2_COUNTRY = "Greece";
	private final String COIN_YEAR = "2004";
	private final String COIN_1_DESCRIPTION = "2€ comm. World Food Programme";
	private final String COIN_2_DESCRIPTION = "2€ comm. Olympics Game of Athen 2004";
	private final String COIN_NOTE = "";

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
			.anySatisfy(e -> assertThat(e).contains(
					ALBUM_NAME + " vol.1 [0/" + ALBUM_SLOTS + "] (" + ALBUM_LOCATION + ")"
			))
			.anySatisfy(e -> assertThat(e).contains(
					ALBUM_NAME + " vol.2 [0/" + ALBUM_SLOTS + "] (" + ALBUM_LOCATION + ")"
			));
		
		assertThat(window.list("coinList").contents())
			.anySatisfy(e -> assertThat(e).contains(
					"[" + COIN_1_COUNTRY + " " + COIN_YEAR + "] {" + COIN_GRADE.toString() + "} " + COIN_1_DESCRIPTION + " (" + COIN_NOTE + ")"
			))
			.anySatisfy(e -> assertThat(e).contains(
					"[" + COIN_2_COUNTRY + " " + COIN_YEAR + "] {" + COIN_GRADE.toString() + "} " + COIN_2_DESCRIPTION + " (" + COIN_NOTE + ")"
			));
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
		String ALBUM_INSERT = 
				"INSERT INTO albums (location, name, number_of_slots, number_of_occupied_slots, volume, id) " +
				"VALUES ('%s', '%s', %s, %s, %s, '%s')";
		String COIN_INSERT = 
				"INSERT INTO coins (album, country, description, grade, minting_year, note, id) " +
				"VALUES ('%s', '%s', '%s', %s, %s, '%s', '%s')";
		
		em.getTransaction().begin();
		em.createNativeQuery(String.format(ALBUM_INSERT, 
				ALBUM_LOCATION, 
				ALBUM_NAME, 
				ALBUM_SLOTS,
				0, 
				1, 
				"550e8400-e29b-41d4-a716-446655440000"
		)).executeUpdate();
		em.createNativeQuery(String.format(ALBUM_INSERT, 
				ALBUM_LOCATION, 
				ALBUM_NAME, 
				ALBUM_SLOTS, 
				0, 
				2, 
				"550e8400-e29b-41d4-a716-446655440001"
		)).executeUpdate();
		
		em.createNativeQuery(String.format(COIN_INSERT, 
				"550e8400-e29b-41d4-a716-446655440000", 
				COIN_1_COUNTRY, COIN_1_DESCRIPTION, 
				COIN_GRADE.ordinal(), 
				Year.parse(COIN_YEAR), 
				COIN_NOTE, 
				"550e8400-e29b-41d4-a716-446655440002"
		)).executeUpdate();
		em.createNativeQuery(String.format(COIN_INSERT, 
				"550e8400-e29b-41d4-a716-446655440000", 
				COIN_2_COUNTRY, COIN_2_DESCRIPTION, 
				COIN_GRADE.ordinal(), 
				Year.parse(COIN_YEAR), 
				COIN_NOTE, 
				"550e8400-e29b-41d4-a716-446655440003"
		)).executeUpdate();
		em.getTransaction().commit();
	}
}
