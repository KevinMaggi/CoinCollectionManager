package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import javax.swing.JComboBox;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * This class exposes some static methods for utilities.
 */
public class SwingViewUtilities {

	private SwingViewUtilities() {}

	/**
	 * Transforms a {@code JComboBox} in an array containing the same albums.
	 *
	 * @param cb	{@code JComboBox} source
	 * @return 		array of {@code Album}s
	 */
	public static Album[] comboBoxToArray(JComboBox<Album> cb) {
		int num = cb.getItemCount();
		Album[] array = new Album[num+1];

		array[0] = null;
		for (int i = 0; i < num; i++)
			array[i+1] = cb.getItemAt(i);

		return array;
	}

}
