package io.github.KevinMaggi.CoinCollectionManager.core.utility;

import java.time.Year;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Year attribute) {
		if (attribute != null)
			return attribute.getValue();
		else
			return null;
	}

	@Override
	public Year convertToEntityAttribute(Integer dbData) {
		if (dbData != null)
			return Year.of(dbData);
		return null;
	}
}