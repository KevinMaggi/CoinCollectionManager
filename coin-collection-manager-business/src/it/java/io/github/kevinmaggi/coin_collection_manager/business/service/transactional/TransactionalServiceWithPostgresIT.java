package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

class TransactionalServiceWithPostgresIT {

	private static EntityManagerFactory emf;
	private EntityManager em;
	
	@BeforeAll
	static void setUpTestCase() {
		// In "postgres-it" persistence unit the jdbc url's port is defined using the env variable setted in POM for failsafe
		// so is just a matter of using that persistence unit
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}
	
	@BeforeEach
	void setUp() {
		em = emf.createEntityManager();
	}
	
	@Test
	void test1() {
		assertThat(true);
		//System.out.println(postgresPort);
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
