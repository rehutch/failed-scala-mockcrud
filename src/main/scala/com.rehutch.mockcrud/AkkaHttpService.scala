package com.rehutch.mockcrud

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.rehutch.mockcrud.CatalogHandler.{BookDeleted, BookNotFound}
import com.typesafe.config.{Config, ConfigFactory}
import redis.RedisClient
import spray.json.RootJsonFormat
import scala.concurrent.{ExecutionContextExecutor, Future}
//import reposiory.{ConcreteRedis, RedisRepoImpl}
import spray.json.DefaultJsonProtocol

case class BookId(id: String)

case class CatalogRequest(id: String)

case class AddBookRequest(id: String, title: String, genre: String)

trait Protocols extends DefaultJsonProtocol {
  implicit val delBookFormat = jsonFormat1(BookDeleted.apply)
  implicit val bookNotFoundFormat = jsonFormat1(BookNotFound.apply)
  implicit val idFormat = jsonFormat1(BookId.apply)
  //implicit val bookFormat = jsonFormat4(Book.apply)
  //implicit val bookFormat: RootJsonFormat[Nothing] = jsonFormat2(Book.apply)
}
trait BookJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookFormat = jsonFormat4(Book)
}
trait Service extends BookJsonProtocol {

  import scala.concurrent.duration._

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit def requestTimeout = Timeout(5 seconds)


  def config: Config


  def catalogHandler: ActorRef
  val logger: LoggingAdapter
  val unsecuredRoutes: Route = {
    pathPrefix("api") {
      pathPrefix("catalog") {
        path("add") {
          post {
            entity(as[Book]) { book =>
              complete {
                (catalogHandler ? CatalogHandler.AddBook(book)).map {
                  case true => OK -> s"${book.title} added!"
                  case _ => InternalServerError -> "Failed to complete your request. please try later"
                }
              }
            }
          }
        }
      }
    }
  }
}


object AkkaHttpRedisService extends App with Service with RedisData {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  val prodDb = new RedisRepoImpl {
    override def db = RedisClient(host = redisUrl.getHost, port = redisUrl.getPort)
  }
  val catalogHandler = system.actorOf(CatalogHandler.props(prodDb))

  val bindingFuture = Http().newServerAt(config.getString("http.interface"), config.getInt("http.port")).bind(unsecuredRoutes)
}