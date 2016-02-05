import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class SingleLabeledNodeTest extends Simulation {
  val httpConf = http
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
  //  Uncomment to see the response of each request.
  //  .extraInfoExtractor(extraInfo => {
  //    println(extraInfo.response.body.string)
  //    Nil
  //  }).disableResponseChunksDiscarding


  val random = new util.Random
  val feeder = Iterator.continually(Map("user_id" -> random.nextInt()))

  val cypher = """CREATE ( me:User { user_id: {user_id} } )"""
  val statements = """{"statements" : [{"statement" : "%s", "parameters": {"user_id": %s}}]}"""

  val scn = scenario("Create Labeled Nodes")
    .during(30 seconds) {
      feed(feeder)
        .exec(
          http("create labeled node")
            .post("/db/data/transaction/commit")
            .basicAuth("neo4j", "swordfish")
            .body(StringBody(statements.format(cypher, "${user_id}")))
            .asJSON
            .check(status.is(200))
        )
    }

  setUp(
    scn.inject( atOnceUsers(8) ).protocols(httpConf)
  )

}
