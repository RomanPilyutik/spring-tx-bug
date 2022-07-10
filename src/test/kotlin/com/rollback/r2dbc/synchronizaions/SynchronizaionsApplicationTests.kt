package com.rollback.r2dbc.synchronizaions

import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException
import com.rollback.r2dbc.synchronizaions.repo.Test2Entity
import com.rollback.r2dbc.synchronizaions.repo.Test2Repository
import com.rollback.r2dbc.synchronizaions.service.TestService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
class SynchronizaionsApplicationTests {

	@Autowired
	lateinit var test2Repository: Test2Repository
	@Autowired
	lateinit var testService: TestService

	@Test
	fun test() {
		val id = UUID.randomUUID()
		test2Repository.insert(Test2Entity(id, "PAYLOAD", LocalDateTime.now()))
			.test()
			.assertNext {  }
			.verifyComplete()

		testService.findEntities(id)
			.test()
			.expectError(EntityNotFoundException::class.java)
			.verify()
	}

}
