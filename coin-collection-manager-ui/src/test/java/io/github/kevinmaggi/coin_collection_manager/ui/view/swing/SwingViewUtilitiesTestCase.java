package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import static io.github.kevinmaggi.coin_collection_manager.ui.view.swing.SwingViewUtilities.comboBoxToArray;
import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JComboBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

class SwingViewUtilitiesTestCase {

	JComboBox<Album> cb;

	@BeforeEach
	void setUp() {
		cb = new JComboBox<>();
	}

	@Test
	@DisplayName("Test when combobox is empty should return array with null")
	void testComboBoxToArrayWhenIsEmpty() {
		Album[] array = comboBoxToArray(cb);

		assertThat(array).containsOnly((Album)null);
	}

	@Test
	@DisplayName("Test when combobox contains an album should return array with null and the element")
	void testComboBoxToArrayWhenContainsOneAlbum() {
		Album album = new Album("Pre-euro", 1, "armadio", 50, 0);
		cb.addItem(album);
		Album[] array = comboBoxToArray(cb);

		assertThat(array).containsExactly((Album)null, album);
	}

	@Test
	@DisplayName("Test when combobox contains multiple album should return array")
	void testComboBoxToArrayWhenContainsMoreAlbum() {
		Album album1 = new Album("Pre-euro", 1, "armadio", 50, 0);
		Album album2 = new Album("Pre-euro", 2, "armadio", 50, 0);
		cb.addItem(album1);
		cb.addItem(album2);
		Album[] array = comboBoxToArray(cb);

		assertThat(array).containsExactly((Album)null, album1, album2);
	}
}
