package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresAlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresCoinRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers
class PostgresTransactionManagerFactoryTestCase {

	// Need to create EntityManager and for this is necessary an EntityManagerFactory and so a DB
	@Container
	private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
																		.withDatabaseName("databasename")
																		.withUsername("postgres-test")
																		.withPassword("postgres-test");
	
	private static EntityManagerFactory emf;
	private EntityManager em;
	private PostgresTransactionManagerFactory factory;
	
	@BeforeAll
	static void setUpTestCase() {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}
	
	@BeforeEach
	void setUp() {
		em = emf.createEntityManager();
		factory = new PostgresTransactionManagerFactory(em);
	}
	
	@Test
	@DisplayName("Test that ::getCoinRepository the first time instantiate and return")
	void testGetCoinRepositoryWhenFirstTimeCallThenInstantiateAndReturn() {
		assertThat(factory.getCoinRepository()).isNotNull();
	}
	
	@Test
	@DisplayName("Test that ::getCoinRepository the next time returns the same repository")
	void testGetCoinRepositoryWhenFirstTimeCallThenReturnTheSame() {
		PostgresCoinRepository cr = factory.getCoinRepository();
		
		assertThat(factory.getCoinRepository()).isSameAs(cr);
	}

	@Test
	@DisplayName("Test that ::getAlbumRepository the first time instantiate and return")
	void testGetAlbumRepositoryWhenFirstTimeCallThenInstantiateAndReturn() {
		assertThat(factory.getAlbumRepository()).isNotNull();
	}
	
	@Test
	@DisplayName("Test that ::getAlbumRepository the next time returns the same repository")
	void testGetAlbumRepositoryWhenFirstTimeCallThenReturnTheSame() {
		PostgresAlbumRepository ar = factory.getAlbumRepository();
		
		assertThat(factory.getAlbumRepository()).isSameAs(ar);
	}
	
	@Test
	@DisplayName("Test that ::getTransactionManagerRepository the first time instantiate and return")
	void testGetTransactionManagerRepositoryWhenFirstTimeCallThenInstantiateAndReturn() {
		assertThat(factory.getTransactionManager()).isNotNull();
	}
	
	@Test
	@DisplayName("Test that ::getTransactionManagermRepository the next time returns the same transaction manager")
	void testGetTransactionManagerRepositoryWhenFirstTimeCallThenReturnTheSame() {
		PostgresTransactionManager tm = factory.getTransactionManager();
		
		assertThat(factory.getTransactionManager()).isSameAs(tm);
	}
	
	@AfterEach
	void cleanTest() throws Exception {
		em.clear();
		em.close();
	}
	
	@AfterAll
	static void cleanTestCase() {
		emf.close();
	}
}
