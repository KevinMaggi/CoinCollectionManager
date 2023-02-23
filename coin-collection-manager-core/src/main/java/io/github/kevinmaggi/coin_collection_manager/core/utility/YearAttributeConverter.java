package io.github.kevinmaggi.coin_collection_manager.core.utility;

import java.time.Year;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Implements a converter between {@code Year} and {@code Integer} types, for database purposes.
 */
@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

	/**
	 * Converts an {@code Year} to an {@code Integer}.
	 *
	 * @param attribute		{@code Year} to convert
	 */
	@Override
	public Integer convertToDatabaseColumn(Year attribute) {
		if (attribute != null)
			return attribute.getValue();
		else
			return null;
	}

	/**
	 * Converts an {@code Integer} to an {@code Year}.
	 *
	 * @param dbData		{@code Integer} to convert
	 */
	@Override
	public Year convertToEntityAttribute(Integer dbData) {
		if (dbData != null)
			return Year.of(dbData);
		return null;
	}
}