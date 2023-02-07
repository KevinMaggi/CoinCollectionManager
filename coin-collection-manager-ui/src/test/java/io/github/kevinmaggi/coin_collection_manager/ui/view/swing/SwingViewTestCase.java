package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import static org.mockito.Mockito.verify;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;

@RunWith(GUITestRunner.class)
public class SwingViewTestCase extends AssertJSwingJUnitTestCase {
	// Test variables
	private static String ALBUM_ACTUAL_ALL = "All albums:";
	
	// Tests
	private FrameFixture window;
	
	private AutoCloseable closeable;

	@Mock
	private CoinPresenter coinPresenter;
	@Mock
	private AlbumPresenter albumPresenter;
	
	private SwingView view;
	
	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			view = new SwingView();
			view.setPresenters(coinPresenter, albumPresenter);
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}
	
	@Test
	public void testSetPresenters() {
		// setPresenter is already called in setup method.
		// It is important to remember this also in next tests because the following calls are by default.
		
		verify(coinPresenter).getAllCoins();
		verify(albumPresenter).getAllAlbums();
	}
	
	@Test @GUITest
	public void testInitialState() {
		// Album panel
		window.label(JLabelMatcher.withText("Key name: "));
		window.textBox("albumSearchNameValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Key volume: "));
		window.textBox("albumSearchVolumeValue").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Search")).requireEnabled();
		
		/*The label is initialized empty, only in the setPresenter method it will be written by the showAll methods 
		 *called by presenters (here mocked, so the label remains empty; anyway later will be tested the showAll methods)*/
		window.label(JLabelMatcher.withName("albumMainActualValue").andText(" "));
		window.list("albumMainList").requireNoSelection();
		
		window.button(JButtonMatcher.withText("Delete album")).requireDisabled();
		window.button(JButtonMatcher.withText("Move album")).requireDisabled();
		
		window.label(JLabelMatcher.withText("Name: "));
		window.textBox("albumFormNameValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Volume: "));
		window.textBox("albumFormVolumeValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Location: "));
		window.textBox("albumFormLocationValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Slots: "));
		window.textBox("albumFormSlotsValue").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		// Coin panel
		window.label(JLabelMatcher.withText("Key description: "));
		window.textBox("coinFilterDescriptionValue").requireEnabled().requireEmpty();
		
		/*The label is initialized empty, only in the setPresenter method it will be written by the showAll methods 
		 *called by presenters (here mocked, so the label remains empty; anyway later will be tested the showAll methods)*/
		window.label(JLabelMatcher.withName("coinMainActualValue").andText(" "));
		window.list("coinMainList").requireNoSelection();
		
		window.button(JButtonMatcher.withText("Delete coin")).requireDisabled();
		window.button(JButtonMatcher.withText("Move coin")).requireDisabled();
		
		window.label(JLabelMatcher.withText("Description: "));
		window.textBox("coinFormDescriptionValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Grade: "));
		window.comboBox("coinFormGradeValue").requireEnabled().requireNoSelection();
		window.label(JLabelMatcher.withText("Country: "));
		window.textBox("coinFormCountryValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Year: "));
		window.textBox("coinFormYearValue").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Album: "));
		window.comboBox("coinFormAlbumValue").requireEnabled().requireNoSelection();
		window.label(JLabelMatcher.withText("Note: "));
		window.textBox("coinFormNoteValue").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		// Status bar
		window.label(JLabelMatcher.withName("statusValue").andText(" "));
		window.button(JButtonMatcher.withText("Filter")).requireEnabled();
	}
	
	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}
}
