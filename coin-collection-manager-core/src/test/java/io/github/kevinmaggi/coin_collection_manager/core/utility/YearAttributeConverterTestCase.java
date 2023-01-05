package io.github.kevinmaggi.coin_collection_manager.core.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.time.Year;

class YearAttributeConverterTestCase {
	private static final int VALID_INT_YEAR = 1492;
	private static final Year VALID_YEAR_YEAR = Year.of(1789);
	
	private YearAttributeConverter converter;
	
	@BeforeEach
	public void setup() {
		this.converter = new YearAttributeConverter();
	}

	@Nested
	@DisplayName("Tests for the method that operate class-to-record conversion")
	class ClassToRecord {
		@Test
		@DisplayName("Test that returns null when null year is passed")
		void testConvertToDatabaseColumnWhenYearIsNull() {
			assertThat(converter.convertToDatabaseColumn(null)).isNull();
		}
		
		@Test
		@DisplayName("Test that returns the correct short when a non null year is passed")
		void testConvertToDatabaseColumnWhenYearIsNotNull() {
			Year year = Year.of(VALID_INT_YEAR);
			assertThat(converter.convertToDatabaseColumn(year)).isEqualTo(VALID_INT_YEAR);
		}
	}
	
	@Nested
	@DisplayName("Tests for the method that operate record-to-class conversion")
	class RecordToClass {
		@Test
		@DisplayName("Test that returns null when null short is passed")
		void testConvertToEntityAttributeWhenShortIsNull() {
			assertThat(converter.convertToEntityAttribute(null)).isNull();
		}
		
		@Test
		@DisplayName("Test that returns the correct year when a non null short is passed")
		void testConvertToEntityAttributeWhenShortIsNotNull() {
			int year_int = VALID_YEAR_YEAR.getValue();
			assertThat(converter.convertToEntityAttribute(year_int)).isEqualTo(VALID_YEAR_YEAR);
		}
	}
}
