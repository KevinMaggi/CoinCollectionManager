package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers
public class PostgresCoinRepositoryTestCase {
	// Test objects to populate database
	private UUID ALBUM_UUID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private UUID ALBUM_UUID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	
	private Year YEAR = Year.of(2004);
	
	private UUID INVALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426652340000");
	private String INVALID_COUNTRY = "Finland";
	private String INVALID_DESCRIPTION = "New 10 member of EU";
	private Coin INVALID_COIN = new Coin(Grade.AG, INVALID_COUNTRY, YEAR, INVALID_DESCRIPTION, "", ALBUM_UUID_1);
	
	private String DESCRIPTION_1 = "2€ comm. World Food Programme";
	private String DESCRIPTION_2 = "2€ comm. Olympics Game of Athen 2004";
	private String COUNTRY_1 = "Italy";
	private String COUNTRY_2 = "Greece";
	private Coin COIN_1 = new Coin(Grade.AG, COUNTRY_1, YEAR, DESCRIPTION_1, "", ALBUM_UUID_1);
	private Coin COIN_2 = new Coin(Grade.AG, COUNTRY_2, YEAR, DESCRIPTION_2, "", ALBUM_UUID_2);
	
	// Tests
	@Container
	private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
																		.withDatabaseName("databasename")
																		.withUsername("postgres-test")
																		.withPassword("postgres-test");
	
	private static EntityManagerFactory emf;
	private EntityManager em;
	
	private PostgresCoinRepository repo;
	
	@BeforeAll
	public static void setUpTestCase() {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}
	
	@BeforeEach
	public void setUpTest() {
		em = emf.createEntityManager();
		repo = new PostgresCoinRepository(em);
		
		// Ensure to start the next test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::findAll")
	class FindAll {
		@Test
		@DisplayName("Test that an empty list is returned if no coins are in the database")
		void testFindAllReturnsEmptyListWhenDbIsEmpty() {
			assertThat(repo.findAll()).isEmpty();
		}
		
		@Test
		@DisplayName("Test that the correct list is returned if there are coins in the database")
		void testFindAllReturnsCorrectListWhenDbIsNotEmpty() {
			populateDB();
			
			assertThat(repo.findAll()).containsOnly(COIN_1, COIN_2);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::findById")
	class FindById {
		@Test
		@DisplayName("Test that exception is thrown if null id is passed")
		void testFindByIdThrowsExceptionWhenNullIsPassed() {
			assertThatThrownBy(() -> repo.findById(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("ID can't be null");
		}
		
		@Test
		@DisplayName("Test that is returned null if the id is not found")
		void testFindByIdReturnsNullWhenIdIsNotFound() {
			assertThat(repo.findById(INVALID_UUID)).isNull();
		}
		
		@Test
		@DisplayName("Test that is returned the (a) correct object if the id is found")
		void testFindByIdReturnsCorrectObjectWhenIdIsFound() {
			populateDB();
			UUID uuid = COIN_1.getId();
			
			assertThat(repo.findById(uuid)).isEqualTo(COIN_1);
		}
	
		@Test
		@DisplayName("Test that is returned the updated object in the case the object in DB is modified")
		void testFindByIdReturnesUpdatedObjectWhenDBIsModified() {
			populateDB();
			UUID uuid = COIN_1.getId();
			
			EntityManager otherEm = emf.createEntityManager();
			otherEm.getTransaction().begin();
			Coin otherCoin = otherEm.find(Coin.class, uuid);
			otherCoin.setDescription("new description");
			otherEm.merge(otherCoin);
			otherEm.getTransaction().commit();
			
			assertThat(repo.findById(uuid).getDescription()).isEqualTo("new description");
		}
		
		@Test
		@DisplayName("Test that is returned null if the object is removed from DB")
		void testFindByIdReturnesNullWhenIsRemovedFromDB() {
			populateDB();
			UUID uuid = COIN_1.getId();
			
			EntityManager otherEm = emf.createEntityManager();
			otherEm.getTransaction().begin();
			Coin otherCoin = otherEm.find(Coin.class, uuid);
			otherEm.remove(otherCoin);
			otherEm.getTransaction().commit();
			
			assertThat(repo.findById(uuid)).isNull();
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::save")
	class Save {
		@Test
		@DisplayName("Test that exception is thrown if null value is passed")
		void TestSaveWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Coin to save can't be null");
		}
		
		@Test
		@DisplayName("Test new entity is added to database and it is returned when it is not persisted")
		void testSaveWhenNewEntityIsPassedShouldBeAddedToDbAndReturned() {
			em.getTransaction().begin();
			Coin coin = repo.save(COIN_1);
			em.getTransaction().commit();
			
			assertThat(coin).isSameAs(COIN_1);
			assertThat(em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList()).contains(COIN_1);
		}
		
		@Test
		@DisplayName("Test entity is updated in database and it is returned when it is already persisted and managed")
		void testSaveWhenPersistedManagedEntityIsPassedShouldBeUpdatedInDbAndReturned() {
			em.getTransaction().begin();
			em.persist(COIN_1);
			em.getTransaction().commit();
			COIN_1.setGrade(Grade.MS);
			
			em.getTransaction().begin();
			Coin coin = repo.save(COIN_1);
			em.getTransaction().commit();
			
			assertThat(coin).isSameAs(COIN_1);
			assertThat(em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList()).contains(COIN_1);
			assertThat(em.find(Coin.class, COIN_1.getId()).getGrade()).isEqualTo(Grade.MS);
		}
		
		@Test
		@DisplayName("Test entity is updated in database and it is returned an equal one when it is already persisted but not managed")
		void testSaveWhenPersistedUnmanagedEntityIsPassedShouldBeUpdatedInDbAndReturned() {
			em.getTransaction().begin();
			em.persist(COIN_1);
			em.getTransaction().commit();
			em.clear();
			COIN_1.setGrade(Grade.MS);
			
			em.getTransaction().begin();
			Coin coin = repo.save(COIN_1);
			em.getTransaction().commit();
			
			assertThat(coin).isEqualTo(COIN_1);
			assertThat(em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList()).contains(COIN_1);
			assertThat(em.find(Coin.class, COIN_1.getId()).getGrade()).isEqualTo(Grade.MS);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::delete")
	class Delete {
		@Test
		@DisplayName("Test that exception is thrown if null value is passed")
		void testDeleteWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.delete(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Coin to delete can't be null");
		}
		
		@Test
		@DisplayName("Test that the database remains untouched if is passed a not persisted coin")
		void testDeleteWhenNonPersistedCoinIsPassedShouldUntouchTheDb() {
			populateDB();
			
			em.getTransaction().begin();
			repo.delete(INVALID_COIN);
			em.getTransaction().commit();
			
			assertThat(em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList()).containsOnly(COIN_1, COIN_2);
		}
		
		@Test
		@DisplayName("Test that a coin is removed if it is persisted")
		void testDeleteWhenPersistedCoinIsPassedShouldRemoveIt() {
			populateDB();
			
			em.getTransaction().begin();
			repo.delete(COIN_1);
			em.getTransaction().commit();
			
			assertThat(em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList()).containsOnly(COIN_2);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::findByDescription")
	class FindByDescription {
		@Test
		@DisplayName("Test that exception is thrown if null value is passed")
		void testFindByDescriptionWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByDescription(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description can't be null");
		}
		
		@Test
		@DisplayName("Test that empty list is returned if description is not found")
		void testFindByDescriptionWhenTargetIsNotFoundShouldReturnEmptyList() {
			assertThat(repo.findByDescription(INVALID_DESCRIPTION)).isEmpty();
		}
		
		@Test
		@DisplayName("Test that correct list is returned if descrition is found")
		void testFindByDescriptionWhenTargetIsFoundShouldReturnCorrectList() {
			populateDB();
			
			assertThat(repo.findByDescription(DESCRIPTION_1)).containsExactly(COIN_1);
		}
		
		@Test
		@DisplayName("Test that correct list is returned if part of descrition is found")
		void testFindByDescriptionWhenPartOfTargetIsFoundShouldReturnCorrectList() {
			populateDB();
			
			assertThat(repo.findByDescription(DESCRIPTION_1.substring(5))).containsExactly(COIN_1);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::findByAlbum")
	class FindByAlbum {
		@Test
		@DisplayName("Test that exception is thrown if null value is passed")
		void testFindByAlbumWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByAlbum(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Album's id can't be null");
		}
		
		@Test
		@DisplayName("Test that empty list is returned if album is not found")
		void testFindByAlbumWhenTargetIsNotFoundShouldReturnEmptyList() {
			assertThat(repo.findByAlbum(INVALID_UUID)).isEmpty();
		}
		
		@Test
		@DisplayName("TEst that correct list is return if album is found")
		void testFindByAlbumWhenTargetIsFoundShouldReturnCorrectList() {
			populateDB();
			
			assertThat(repo.findByAlbum(ALBUM_UUID_1)).containsExactly(COIN_1);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresCoinRepository::findByGradeCountryYearDescriptionAndNote")
	class FindByGradeCountryYearDescriptionAndNote {
		@Test
		@DisplayName("Test that exception is thrown if null Grade value is passed")
		void testfindByGradeCountryYearDescriptionAndNoteWhenNullGradeIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByGradeCountryYearDescriptionAndNote(null, COUNTRY_1, YEAR, DESCRIPTION_1, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Grade can't be null");
		}
		
		@Test
		@DisplayName("Test that exception is thrown if null country value is passed")
		void testfindByGradeCountryYearDescriptionAndNoteWhenNullCountryIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, null, YEAR, DESCRIPTION_1, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Country can't be null");
		}
		
		@Test
		@DisplayName("Test that exception is thrown if null year value is passed")
		void testfindByGradeCountryYearDescriptionAndNoteWhenNullYearIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, COUNTRY_1, null, DESCRIPTION_1, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Year can't be null");
		}
		
		@Test
		@DisplayName("Test that exception is thrown if null description value is passed")
		void testfindByGradeCountryYearDescriptionAndNoteWhenNullDescriptionIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, COUNTRY_1, YEAR, null, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description can't be null");
		}
		
		@Test
		@DisplayName("Test that exception is thrown if null note value is passed")
		void testfindByGradeCountryYearDescriptionAndNoteWhenNullNoteIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, COUNTRY_1, YEAR, DESCRIPTION_1, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Note can't be null");
		}
		
		@Test
		@DisplayName("Test that null is returned if the coin is not found")
		void testfindByGradeCountryYearDescriptionAndNoteWhenCoinIsNotFoundShoudReturnNull() {
			populateDB();
			
			assertThat(
					repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, INVALID_COUNTRY, YEAR, INVALID_DESCRIPTION, INVALID_DESCRIPTION)
					).isNull();
		}
		
		@Test
		@DisplayName("Test that correct coin si returned if the coin is found")
		void testfindByGradeCountryYearDescriptionAndNoteWhenCoinIsFoundShouldReturnIt() {
			populateDB();
			
			assertThat(
					repo.findByGradeCountryYearDescriptionAndNote(Grade.AG, COUNTRY_2, YEAR, DESCRIPTION_2, "")
					).isEqualTo(COIN_2);
		}
	}
	
	@AfterEach
	public void cleanTest() {
		em.clear();
		em.close();
	}
	
	@AfterAll
	public static void cleanTestCase() {
		emf.close();
	}
	
	// Private methods
	private void populateDB() {
		em.getTransaction().begin();
		em.persist(COIN_1);
		em.persist(COIN_2);
		em.getTransaction().commit();
	}
}
