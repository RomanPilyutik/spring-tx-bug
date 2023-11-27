package com.rollback.r2dbc.synchronizaions

import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException
import com.rollback.r2dbc.synchronizaions.repo.Test2Entity
import com.rollback.r2dbc.synchronizaions.repo.TestRepository2
import com.rollback.r2dbc.synchronizaions.service.TestService
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test

@SpringBootTest
class SynchronizationsApplicationTests {

	@Autowired
	lateinit var testRepository2: TestRepository2
	@Autowired
	lateinit var testService: TestService

	@Test
	fun testWithOneTransactionManagers() {
		val id = UUID.randomUUID()
		testRepository2.insert(Test2Entity(id, "PAYLOAD", LocalDateTime.now()))
			.test()
			.assertNext {  }
			.verifyComplete()

		testService.findEntitiesWithOneTransactionManager(id)
			.test()
			.expectError(EntityNotFoundException::class.java)
			.verify()
	}
}
