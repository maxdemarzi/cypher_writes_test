import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class SingleIndexedNodeTest extends Simulation {
  val httpConf = http
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
    .shareConnections
  //  Uncomment to see the response of each request.
  //  .extraInfoExtractor(extraInfo => {
  //    println(extraInfo.response.body.string)
  //    Nil
  //  }).disableResponseChunksDiscarding


  val random = new util.Random
  val feeder = Iterator.continually(Map("user_id" -> random.nextInt()))

  val cypher = """CREATE ( me:User { user_id: {user_id} } )"""
  val statements = """{"statements" : [{"statement" : "%s", "parameters": {"user_id": %s}}]}"""

  val setup = scenario("Create Index")
    .exec(http("Create Index")
      .post("/db/data/schema/index/User")
      .basicAuth("neo4j", "swordfish")
      .body(StringBody("""{"property_keys" : ["user_id"] } """))
      .asJSON
      .check(status.in(200,409))
      .silent
    )

  val scn = scenario("Create Indexed Nodes")
    .during(30 seconds) {
      feed(feeder)
        .exec(
          http("create indexed node")
            .post("/db/data/transaction/commit")
            .basicAuth("neo4j", "swordfish")
            .body(StringBody(statements.format(cypher, "${user_id}")))
            .asJSON
            .check(status.is(200))
        )
    }

  setUp(
    setup.inject( atOnceUsers(1) ).protocols(httpConf),
    scn.inject( atOnceUsers(8) ).protocols(httpConf)
  )

}
