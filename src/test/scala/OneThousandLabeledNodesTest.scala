import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.parsing.json.{JSONObject, JSONArray}

class OneThousandLabeledNodesTest extends Simulation {
  val httpConf = http
    .baseURL("http://localhost:7474")
    .acceptHeader("application/json")
    .shareConnections
    .basicAuth("neo4j", "swordfish")
  //  Uncomment to see the response of each request.
  //    .extraInfoExtractor(extraInfo => {
  //      println(extraInfo.response.body.string)
  //      Nil
  //    }).disableResponseChunksDiscarding


  val random = new util.Random

  val cypher = """CREATE ( me:User { user_id: {user_id} } )"""
  val oneThousand = JSONArray.apply(
    List.fill(1000)(
      JSONObject.apply(
        Map("statement" -> cypher,
          "parameters" -> JSONObject.apply(
            Map("user_id" -> random.nextInt())))
      )
    )
  )
  val statements = JSONObject.apply(Map("statements" -> oneThousand))

  val scn = scenario("Create 1000 Labeled Nodes")
    .during(30 seconds) {
      exec(
        http("create 1000 labeled nodes")
          .post("/db/data/transaction/commit")
          .basicAuth("neo4j", "swordfish")
          .body(StringBody(statements.toString()))
          .asJSON
          .check(status.is(200))
      )
    }


  setUp(
    scn.inject( atOnceUsers(8) ).protocols(httpConf)
  )

}
