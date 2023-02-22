package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers
class PostgresAlbumRepositoryTestCase {
	// Test objects to populate database
	private UUID INVALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private String INVALID_NAME = "Europa no-UE";
	private int INVALID_VOLUME = 5;
	private Album INVALID_ALBUM = new Album(INVALID_NAME, INVALID_VOLUME, "Cassetto", 50, 50);
	
	private String ALBUM_1_NAME = "Europa pre-euro";
	private String ALBUM_2_NAME = "Europa pre-euro";
	private int ALBUM_1_VOLUME = 1;
	private int ALBUM_2_VOLUME = 2;
	private Album ALBUM_1 = new Album(ALBUM_1_NAME, ALBUM_1_VOLUME, "Armadio", 50, 50);
	private Album ALBUM_2 = new Album(ALBUM_2_NAME, ALBUM_2_VOLUME, "Armadio", 50, 50);
	
	private String ALBUM_NEW_NAME = "Medio-oriente";
	
	// Tests
	@Container
	private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
																		.withDatabaseName("databasename")
																		.withUsername("postgres-test")
																		.withPassword("postgres-test");
	
	private static EntityManagerFactory emf;
	private EntityManager em;
	
	private PostgresAlbumRepository repo;
	
	@BeforeAll
	public static void setUpTestCase() {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}
	
	@BeforeEach
	public void setUpTest() {
		em = emf.createEntityManager();
		repo = new PostgresAlbumRepository(em);
		
		// Ensure to start every test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.getTransaction().commit();
	}
	
	@Nested
	@DisplayName("Tests for method PostgresAlbumRepository::findAll")
	class findAll {
		@Test
		@DisplayName("Test that an empty list is returned when there are no albums in the database")
		void testFindAllReturnsEmptyListWhenDbIsEmpty() {
			assertThat(repo.findAll()).isEmpty();
		}
		
		@Test
		@DisplayName("Test that the correct list is returned when there are albums in the database")
		void testFindAllReturnsRightListWhenDbIsNotEmpty() {
			populateDB();
			
			assertThat(repo.findAll()).containsOnly(ALBUM_1, ALBUM_2);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresAlbumRepository::findById")
	class findById {
		@Test
		@DisplayName("Test that exception is thrown if the target id is null")
		void testFindByIdWhenIdIsNullShouldThrowException() {
			assertThatThrownBy(() -> repo.findById(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("ID can't be null");
		}
		
		@Test
		@DisplayName("Test that is returned null if the target id is not found")
		void testFindByIdWhenIdIsNotFound() {
			assertThat(repo.findById(INVALID_UUID)).isNull();
		}
		
		@Test
		@DisplayName("Test that is returned the (a) correct object if the target id is found")
		void testFindByIdWhenIdIsFound() {
			populateDB();
			UUID uuid = ALBUM_1.getId();
			
			assertThat(repo.findById(uuid)).isEqualTo(ALBUM_1);
		}
	
		@Test
		@DisplayName("Test that is returned the updated object in the case the object in DB is modified")
		void testFindByIdReturnesUpdatedObjectWhenDBIsModified() {
			populateDB();
			UUID uuid = ALBUM_1.getId();
			
			EntityManager otherEm = emf.createEntityManager();
			otherEm.getTransaction().begin();
			Album otherAlbum = otherEm.find(Album.class, uuid);
			otherAlbum.setName("new name");
			otherEm.merge(otherAlbum);
			otherEm.getTransaction().commit();
			
			assertThat(repo.findById(uuid).getName()).isEqualTo("new name");
		}
		
		@Test
		@DisplayName("Test that is returned null if the object is removed from DB")
		void testFindByIdReturnesNullWhenIsRemovedFromDB() {
			populateDB();
			UUID uuid = ALBUM_1.getId();
			
			EntityManager otherEm = emf.createEntityManager();
			otherEm.getTransaction().begin();
			Album otherAlbum = otherEm.find(Album.class, uuid);
			otherEm.remove(otherAlbum);
			otherEm.getTransaction().commit();
			
			assertThat(repo.findById(uuid)).isNull();
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresAlbumRepository::findByNameAndVolume")
	class findByNameAndVolume {
		@Test
		@DisplayName("Test that exception is thrown if the target name is null")
		void testFindByNameAndVolumeWhenNameIsNullShouldThrowException() {
			assertThatThrownBy(() -> repo.findByNameAndVolume(null, 0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Name can't be null");
		}
		
		@Test
		@DisplayName("Test that is returned null if the name/volume is not found")
		void testFindByNameAndVolumeWhenTargetIsNotFound() {
			assertThat(repo.findByNameAndVolume(INVALID_NAME, 0)).isNull();
		}
		
		@Test
		@DisplayName("Test that is returned the correct object if the target is found")
		void testFindByNameAndVolumeWhenTargetIsFound() {
			populateDB();
			
			assertThat(repo.findByNameAndVolume(ALBUM_1_NAME, ALBUM_1_VOLUME)).isEqualTo(ALBUM_1);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresAlbumRepository::delete")
	class delete {
		@Test
		@DisplayName("Test that exception is thrown if null object is passed")
		void testDeleteWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.delete(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Album to delete can't be null");
		}
		
		@Test
		@DisplayName("Test that database remains untouched if not persisted album is passed")
		void testDeleteWhenNonPersistedAlbumIsPassedShouldUntouchTheDb() {
			populateDB();
			
			em.getTransaction().begin();
			repo.delete(INVALID_ALBUM);
			em.getTransaction().commit();
			
			assertThat(em.createQuery("SELECT a FROM Album a", Album.class).getResultList()).containsOnly(ALBUM_1, ALBUM_2);
		}
		
		@Test
		@DisplayName("Test that remove a persisted album")
		void testDeleteWhenPersistedAlbumShouldRemoveIt() {
			populateDB();
			
			em.getTransaction().begin();
			repo.delete(ALBUM_1);
			em.getTransaction().commit();
			
			assertThat(em.createQuery("SELECT a FROM Album a", Album.class).getResultList()).containsOnly(ALBUM_2);
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresAlbumRepository::save")
	class save {
		@Test
		@DisplayName("Test exception is thrown if null is passed")
		void testSaveWhenNullIsPassedShouldThrowException() {
			assertThatThrownBy(() -> repo.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Album to save can't be null");
		}
		
		@Test
		@DisplayName("Test new entity is added to database and it is returned when it is not persisted")
		void testSaveWhenNewEntityIsPassedShouldBeAddedToDbAndReturned() {
			em.getTransaction().begin();
			Album album = repo.save(ALBUM_1);
			em.getTransaction().commit();
			
			assertThat(album).isSameAs(ALBUM_1);
			assertThat(em.createQuery("SELECT a FROM Album a", Album.class).getResultList()).contains(ALBUM_1);
		}
		
		@Test
		@DisplayName("Test entity is updated in database and it is returned when it is already persisted and managed")
		void testSaveWhenPersistedManagedEntityIsPassedShouldBeUpdatedInDbAndReturned() {
			em.getTransaction().begin();
			em.persist(ALBUM_1);
			em.getTransaction().commit();
			ALBUM_1.setName(ALBUM_NEW_NAME);
			
			em.getTransaction().begin();
			Album album = repo.save(ALBUM_1);
			em.getTransaction().commit();
			
			assertThat(album).isSameAs(ALBUM_1);
			assertThat(em.createQuery("SELECT a FROM Album a", Album.class).getResultList()).contains(ALBUM_1);
			assertThat(em.find(Album.class, ALBUM_1.getId()).getName()).isEqualTo(ALBUM_NEW_NAME);
		}
		
		@Test
		@DisplayName("Test entity is updated in database and it is returned an equal one when it is already persisted but not managed")
		void testSaveWhenPersistedUnmanagedEntityIsPassedShouldBeUpdatedInDbAndReturned() {
			em.getTransaction().begin();
			em.persist(ALBUM_1);
			em.getTransaction().commit();
			em.clear();
			ALBUM_1.setName(ALBUM_NEW_NAME);
			
			em.getTransaction().begin();
			Album album = repo.save(ALBUM_1);
			em.getTransaction().commit();
			
			assertThat(album).isEqualTo(ALBUM_1);
			assertThat(em.createQuery("SELECT a FROM Album a", Album.class).getResultList()).contains(ALBUM_1);
			assertThat(em.find(Album.class, ALBUM_1.getId()).getName()).isEqualTo(ALBUM_NEW_NAME);
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
		em.persist(ALBUM_1);
		em.persist(ALBUM_2);
		em.getTransaction().commit();
	}

}
