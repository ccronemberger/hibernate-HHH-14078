package com.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.BiConsumer;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DuplicateInsertionTest {
	private static EntityManagerFactory emf;
	private static EntityManager em;

	@BeforeAll
	public static void setUp() {
		emf = Persistence.createEntityManagerFactory("TestPersistenceUnit");
	}

	@BeforeEach
	public void createEntityManager() {
		em = emf.createEntityManager();
	}

	@AfterEach
	public void closeEntityManager() {
		em.close();
		em = null;
	}

	@Test
	void queryTest() {
		em.getTransaction().begin();
		Parent parent = new Parent();
		Child child = new Child();
		//parent.getChildren().add(child);
		//child.setParent(parent);

		em.persist(parent);
		em.getTransaction().commit();
		em.close();

		em = emf.createEntityManager();

		List l = em.createQuery("select p from Parent p")
				   .getResultList();
		System.out.println(l.size());
	}

	/**
	 * This is a reasonable workaround for the bug because it works and at the same time we can do the correct
	 * thing by updating both sides of the relationship.
	 * The problem does not happen when we operate the relationship with both ends persisted.
	 */
	@Test
	void testDuplicateInsertion() {
		// persist parent entity in a transaction
		em.getTransaction().begin();

		Parent parent = new Parent();
		em.persist(parent);
		int id = parent.getId();

		em.getTransaction().commit();
		em.close();

		// relate and persist child entity in another transaction
		em = emf.createEntityManager();
		em.getTransaction().begin();

		try {
			parent = em.find(Parent.class, id);
			Child child = new Child();

			em.persist(child);

			// update both sides of the relationship
			//child.setParent(parent);
			//parent.getChildren().add(child);

			//assertEquals(1, parent.getChildren().size());
		} finally {
			em.getTransaction().commit();
		}

		parent = em.find(Parent.class, id);
		//assertEquals(1, parent.getChildren().size());
	}

	BiConsumer<Parent, Child> bothSides = (parent, child) -> {
		//child.setParent(parent);
		//parent.getChildren().add(child);
	};

	BiConsumer<Parent, Child> parentSide = (parent, child) -> System.out.println();
		//parent.getChildren().add(child);

	BiConsumer<Parent, Child> childSide = (parent, child) -> System.out.println();
		//child.setParent(parent);

	/**
	 * Bidirectional relationship being handled correctly, it works if Child is persisted before
	 * the operation with the relationship.
	 */
	@Test
	void testUpdatingBothSidesWithPersistBefore() {
		testDuplicateInsertionWithVariations(bothSides, false, true);
	}

	/**
	 * Bidirectional relationship being handled correctly.
	 */
	@Test
	void testUpdatingBothSidesWithPersistBeforeInTx() {
		testDuplicateInsertionWithVariations(bothSides, true, true);
	}

	/**
	 * Bidirectional relationship being handled correctly.
	 */
	@Test
	void testUpdatingBothSidesWithPersistAfter() {
		testDuplicateInsertionWithVariations(bothSides, false, false);
	}

	/**
	 * Bidirectional relationship being handled correctly. This is buggy: inside the tx it reports two children.
	 * in {@link #testUpdatingBothSidesWithPersistAfter()} it works because there we check only outside of the
	 * transaction. As we can see from the test
	 * {@link #testUpdatingBothSidesWithPersistAfter()} the problem only happens inside the tx.
	 */
	@Test
	void testUpdatingBothSidesWithPersistAfterInTx() {
		testDuplicateInsertionWithVariations(bothSides, true, false);
	}

	/**
	 * This is wrong because it only updates one side of the relationship. No need to pass this test then.
	 */
	@Test
	void testUpdatingParentSideWithPersistBefore() {
		testDuplicateInsertionWithVariations(parentSide, false, true);
	}

	/**
	 * This is wrong because it only updates one side of the relationship.
	 * This test does not need to pass because this is an invalid scenario, but it is interesting to
	 * notice how accessing the size of the collection inside the tx makes
	 * {@link #testUpdatingParentSideWithPersistBefore()} pass.
	 * This is a bad sign because getting the size of the collection should not change the outcome of
	 * the process.
	 */
	@Test
	void testUpdatingParentSideWithPersistBeforeInTx() {
		testDuplicateInsertionWithVariations(parentSide, true, true);
	}

	/**
	 * This is wrong because it only updates one side of the relationship. No need to pass this test then.
	 */
	@Test
	void testUpdatingParentSideWithPersistAfter() {
		testDuplicateInsertionWithVariations(parentSide, false, false);
	}

	/**
	 * This is wrong because it only updates one side of the relationship.
	 * This test does not need to pass because this is an invalid scenario, but it is interesting to
	 * notice how accessing the size of the collection inside the tx makes
	 * {@link #testUpdatingParentSideWithPersistAfter()} pass.
	 * This is a bad sign because getting the size of the collection should not change the outcome of
	 * the process.
	 */
	@Test
	void testUpdatingParentSideWithPersistAfterInTx() {
		testDuplicateInsertionWithVariations(parentSide, true, false);
	}

	/**
	 * This is wrong because it only updates one side of the relationship.
	 */
	@Test
	void testUpdatingChildSideWithPersistBefore() {
		testDuplicateInsertionWithVariations(childSide, false, true);
	}

	/**
	 * This is wrong because it only updates one side of the relationship. No need to pass this test then.
	 * On the other hand this test only fails if we check the size of the collection inside the tx.
	 * Outside of the tx it is OK as we can see from test {@link #testUpdatingChildSideWithPersistBefore()}
	 */
	@Test
	void testUpdatingChildSideWithPersistBeforeInTx() {
		testDuplicateInsertionWithVariations(childSide, true, true);
	}

	/**
	 * This is wrong because it only updates one side of the relationship.
	 */
	@Test
	void testUpdatingChildSideWithPersistAfter() {
		testDuplicateInsertionWithVariations(childSide, false, false);
	}

	/**
	 * This is wrong because it only updates one side of the relationship.
	 */
	@Test
	void testUpdatingChildSideWithPersistAfterInTx() {
		testDuplicateInsertionWithVariations(childSide, true, false);
	}

	/**
	 * This test shows a nasty aspect of the bug: the bug only happens when we do proper relationship handling.
	 * This means that the bug is inducing developers to update a single side of the relationship letting
	 * the relationship corrupted which can later reflect in corrupted entries in cache.
	 */
	void testDuplicateInsertionWithVariations(BiConsumer<Parent, Child> consumer, boolean checkInsideTx,
											  boolean persistBefore) {
		// persist parent entity in a transaction
		em.getTransaction().begin();

		Parent parent = new Parent();
		em.persist(parent);
		int id = parent.getId();

		em.getTransaction().commit();
		em.close();

		// relate and persist child entity in another transaction
		em = emf.createEntityManager();
		em.getTransaction().begin();

		try {
			parent = em.find(Parent.class, id);
			Child child = new Child();

			if (persistBefore) {
				em.persist(child);
			}

			consumer.accept(parent, child);

			if (!persistBefore) {
				em.persist(child);
			}

			if (checkInsideTx) {
				System.out.println("checking size inside tx");
				// verify the number of children
				//assertEquals(1, parent.getChildren().size());
			}
		} finally {
			em.getTransaction().commit();
		}

		System.out.println("checking size outside tx");
		parent = em.find(Parent.class, id);
		//assertEquals(1, parent.getChildren().size());
	}

	@AfterAll
	public static void tearDown() {
		emf.close();
	}
}
