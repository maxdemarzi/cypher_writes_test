import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.parsing.json.{JSONObject, JSONArray}

class OneThousandIndexedNodesTest extends Simulation {
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

  val setup = scenario("Create Index")
    .exec(http("Create Index")
      .post("/db/data/schema/index/User")
      .basicAuth("neo4j", "swordfish")
      .body(StringBody("""{"property_keys" : ["user_id"] } """))
      .asJSON
      .check(status.in(200,409))
      .silent
    )

  val scn = scenario("Create 1000 Indexed Nodes")
    .during(30 seconds) {
      exec(
        http("create 1000 indexed nodes")
          .post("/db/data/transaction/commit")
          .basicAuth("neo4j", "swordfish")
          .body(StringBody(statements.toString()))
          .asJSON
          .check(status.is(200))
      )
    }


  setUp(
    setup.inject( atOnceUsers(1) ).protocols(httpConf),
    scn.inject( atOnceUsers(8) ).protocols(httpConf)
  )

}
