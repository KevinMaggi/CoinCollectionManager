package io.github.kevinmaggi.coin_collection_manager.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
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
	
	// Utility
	private final String ALBUM_INSERT = 
			"INSERT INTO albums (location, name, number_of_slots, number_of_occupied_slots, volume, id) " +
			"VALUES ('%s', '%s', %s, %s, %s, '%s')";
	private final String ALBUM_DELETE = 
			"DELETE FROM albums WHERE name='%s' AND volume=%d";
	private final String ALBUM_MODIFY = 
			"UPDATE albums SET location = '%s' WHERE id='%s'";
	private final String COIN_INSERT = 
			"INSERT INTO coins (album, country, description, grade, minting_year, note, id) " +
			"VALUES ('%s', '%s', '%s', %s, %s, '%s', '%s')";
	private final String COIN_DELETE = 
			"DELETE FROM coins WHERE grade=%d AND country='%s' AND description='%s' AND note='%s' AND minting_year=%d";
	private final String COIN_MODIFY = 
			"UPDATE coins SET album = '%s' WHERE id='%s'";

	// Tests
	private static final int TIMEOUT = 5000;
	
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	private FrameFixture window;

	@BeforeClass
	public static void setUpTestCase() {
		Map<String, String> propertiesOverriding = new HashMap<>();
		String port = System.getProperty("postgres.port", "5432");
		propertiesOverriding.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:" + port + "/collection");
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
	
	@Test @GUITest
	public void testSearchAlbumFunction() {
		window.textBox("albumSearchName").enterText(ALBUM_NAME);
		window.textBox("albumSearchVolume").enterText("1");
		window.button(JButtonMatcher.withText("Search")).click();
		
		pause(new Condition("List contain single element") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length == 1;
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.list("albumList").contents()).containsOnly(ALBUM_NAME + " vol.1 [0/" + ALBUM_SLOTS + "] (" + ALBUM_LOCATION + ")");
	}
	
	@Test @GUITest
	public void testAllAlbumsFunction() {
		insertAlbum("Cassaforte", "Pre-euro", 30, 1, "550e8400-e29b-41d4-a716-446655440005");
		
		window.button(JButtonMatcher.withText("All albums")).click();
		
		pause(new Condition("List contain three element") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length == 3;
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.list("albumList").contents())
			.anySatisfy(e -> assertThat(e).contains(
					"Pre-euro vol.1 [0/30] (Cassaforte)"
			));
	}
	
	@Test @GUITest
	public void testAlbumSelectionFunctionOnSuccess() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("albumSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("albumSelection").text()).contains("Euro commemorative volume 1");
	}
	
	@Test @GUITest
	public void testAlbumSelectionFunctionAfterDBChange() {
		modifyAlbum("550e8400-e29b-41d4-a716-446655440000", "cassaforte");
		window.list("albumList").selectItem(Pattern.compile(".*Euro commemorative vol.1.*"));
		
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("albumSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("albumSelection").text()).contains("cassaforte");
	}
	
	@Test @GUITest
	public void testAlbumSelectionFunctionOnFail() {
		deleteAlbum(ALBUM_NAME, 1);
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
	}
	
	@Test @GUITest
	public void testAlbumDeselectionFunction() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("albumSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		window.list("albumList").clearSelection();
		
		pause(new Condition("Label empty") {
			@Override
			public boolean test() {
				return window.label("albumSelection").text() == " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("albumSelection").text().trim()).isEmpty();
	}
	
	@Test @GUITest
	public void testDeleteAlbumFunctionOnSuccess() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		window.button(JButtonMatcher.withText("Delete album")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("successfully deleted");
	}
	
	@Test @GUITest
	public void testDeleteAlbumFunctionOnFail() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		deleteAlbum(ALBUM_NAME, 1);
		window.button(JButtonMatcher.withText("Delete album")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
	}
	
	@Test @GUITest
	public void testMoveAlbumFunctionOnSuccess() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		window.button(JButtonMatcher.withText("Move album")).click();
		window.dialog().textBox().enterText("New location");
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("successfully moved");
	}
	
	@Test @GUITest
	public void testMoveAlbumFunctionOnFail() {
		window.list("albumList").selectItem(Pattern.compile("Euro commemorative vol.1 .*"));
		deleteAlbum(ALBUM_NAME, 1);
		window.button(JButtonMatcher.withText("Move album")).click();
		window.dialog().textBox().enterText("New location");
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
	}
	
	@Test @GUITest
	public void testAddAlbumFunctionOnSuccess() {
		window.textBox("albumFormName").enterText("Pre-euro");
		window.textBox("albumFormVolume").enterText("1");
		window.textBox("albumFormLocation").enterText("Cassaforte");
		window.textBox("albumFormSlots").enterText("30");
		window.button(JButtonMatcher.withText("Save album")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("successfully added");
	}
	
	@Test @GUITest
	public void testAddAlbumFunctionOnFail() {
		window.textBox("albumFormName").enterText(ALBUM_NAME);
		window.textBox("albumFormVolume").enterText("1");
		window.textBox("albumFormLocation").enterText(ALBUM_LOCATION);
		window.textBox("albumFormSlots").enterText(String.valueOf(ALBUM_SLOTS));
		window.button(JButtonMatcher.withText("Save album")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("already exists");
	}
	
	@Test @GUITest
	public void testFilterCoinFunction() {
		window.textBox("coinFilterDescription").enterText(COIN_1_DESCRIPTION);
		window.button(JButtonMatcher.withText("Filter")).click();
		
		pause(new Condition("List contain single element") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length == 1;
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.list("coinList").contents()).containsOnly(
				"[" + COIN_1_COUNTRY + " " + COIN_YEAR + "] {" + COIN_GRADE.toString() + "} " + COIN_1_DESCRIPTION + " (" + COIN_NOTE + ")"
		);
	}
	
	@Test @GUITest
	public void testAllCoinsFunction() {
		insertCoin("550e8400-e29b-41d4-a716-446655440008", "Italy", "500 L.", Grade.PO, "1975", "", "550e8400-e29b-41d4-a716-446655440009");
		
		window.button(JButtonMatcher.withText("All coins")).click();
		
		pause(new Condition("List contain three element") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length == 3;
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.list("coinList").contents())
			.anySatisfy(e -> assertThat(e).contains(
					"[Italy 1975] {PO} 500 L. ()"
			));
	}
	
	@Test @GUITest
	public void testCoinSelectionFunctionOnSuccess() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("coinSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("coinSelection").text()).contains("World Food Programme");
	}
	
	@Test @GUITest
	public void testCoinSelectionFunctionAfterDBChange() {
		modifyCoin("550e8400-e29b-41d4-a716-446655440002", "550e8400-e29b-41d4-a716-446655440001");
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("coinSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("coinSelection").text()).contains("vol.2");
	}
	
	@Test @GUITest
	public void testCoinSelectionFunctionOnFail() {
		deleteCoin(COIN_GRADE, COIN_1_COUNTRY, COIN_1_DESCRIPTION, COIN_NOTE, COIN_YEAR);
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
	}
	
	@Test @GUITest
	public void testCoinDeselectionFunction() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		pause(new Condition("Label change value") {
			@Override
			public boolean test() {
				return window.label("coinSelection").text() != " ";
			}
		}, timeout(TIMEOUT));
		window.list("coinList").clearSelection();
		
		pause(new Condition("Label empty") {
			@Override
			public boolean test() {
				return window.label("coinSelection").text() == " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("coinSelection").text().trim()).isEmpty();
	}
	
	@Test @GUITest
	public void testDeleteCoinFunctionOnSuccess() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		window.button(JButtonMatcher.withText("Delete coin")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("successfully deleted");
	}
	
	@Test @GUITest
	public void testDeleteCoinFunctionOnFail() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		deleteCoin(COIN_GRADE, COIN_1_COUNTRY, COIN_1_DESCRIPTION, COIN_NOTE, COIN_YEAR);
		window.button(JButtonMatcher.withText("Delete coin")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
	}
	
	@Test @GUITest
	public void testMoveCoinFunctionOnSuccess() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		window.button(JButtonMatcher.withText("Move coin")).click();
		window.dialog().comboBox().selectItem(Pattern.compile("Euro commemorative vol.2.*"));
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("successfully moved");
	}
	
	@Test @GUITest
	public void testMoveCoinFunctionOnFail() {
		window.list("coinList").selectItem(Pattern.compile(".*World Food Programme.*"));
		deleteCoin(COIN_GRADE, COIN_1_COUNTRY, COIN_1_DESCRIPTION, COIN_NOTE, COIN_YEAR);
		window.button(JButtonMatcher.withText("Move coin")).click();
		window.dialog().comboBox().selectItem(Pattern.compile("Euro commemorative vol.2.*"));
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		pause(new Condition("Status change value") {
			@Override
			public boolean test() {
				return window.label("status").text() != " ";
			}
		}, timeout(TIMEOUT));
		
		assertThat(window.label("status").text()).contains("doesn't exist");
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
		insertAlbum(
				ALBUM_LOCATION, 
				ALBUM_NAME, 
				ALBUM_SLOTS,
				1, 
				"550e8400-e29b-41d4-a716-446655440000"
		);
		insertAlbum(
				ALBUM_LOCATION, 
				ALBUM_NAME, 
				ALBUM_SLOTS, 
				2, 
				"550e8400-e29b-41d4-a716-446655440001"
		);
		
		insertCoin(
				"550e8400-e29b-41d4-a716-446655440000", 
				COIN_1_COUNTRY,
				COIN_1_DESCRIPTION, 
				COIN_GRADE, 
				COIN_YEAR, 
				COIN_NOTE, 
				"550e8400-e29b-41d4-a716-446655440002"
		);
		insertCoin(
				"550e8400-e29b-41d4-a716-446655440000", 
				COIN_2_COUNTRY,
				COIN_2_DESCRIPTION, 
				COIN_GRADE, 
				COIN_YEAR, 
				COIN_NOTE, 
				"550e8400-e29b-41d4-a716-446655440003"
		);
	}
	
	private void insertAlbum(String location, String name, int slots, int volume, String id) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(ALBUM_INSERT, location, name, slots, 0, volume, id))
				.executeUpdate();
		em.getTransaction().commit();
	}
	
	private void insertCoin(String album, String country, String description, Grade grade, String year, String note, String id) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(COIN_INSERT, album, country, description, grade.ordinal(), Year.parse(year), note, id))
				.executeUpdate();
		em.getTransaction().commit();
	}
	
	private void deleteAlbum(String name, int volume) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(ALBUM_DELETE, name, volume)).executeUpdate();
		em.getTransaction().commit();
	}
	
	private void deleteCoin(Grade grade, String country, String description, String note, String year) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(COIN_DELETE, grade.ordinal(), country, description, note, Integer.valueOf(year)))
			.executeUpdate();
		em.getTransaction().commit();
	}

	private void modifyAlbum(String id, String newLocation) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(ALBUM_MODIFY, newLocation, id)).executeUpdate();
		em.getTransaction().commit();
	}
	
	private void modifyCoin(String coinId, String newAlbumId) {
		em.getTransaction().begin();
		em.createNativeQuery(String.format(COIN_MODIFY, newAlbumId, coinId)).executeUpdate();
		em.getTransaction().commit();
	}
}
