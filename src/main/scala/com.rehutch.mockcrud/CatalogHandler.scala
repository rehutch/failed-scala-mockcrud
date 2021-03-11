package com.rehutch.mockcrud

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.pattern.pipe
import akka.protobuf.ByteString
import akka.util.ByteString
import redis.ByteStringFormatter
import spray.json.DefaultJsonProtocol

final case class Book(
                       id: String,
                       title: String,
                       genre: String,
                       checkedOut: Boolean = false
                     )



//final case class Catalog(books: List[Book])

object CatalogHandler {
  def props(db: Repo): Props = Props(new CatalogHandler(db))

  //case class AddBook(id:String,title:String,genre:String)
case class AddBook(book:Book)
  case class RemoveBook(id: String)

  case class GetBook(id: String)

  case class CheckoutBook(id: String)

  case class ReturnBook(id: String)

  case class BookNotFound(id: String)
  case class BookDeleted(id: String)
}

class CatalogHandler(db: Repo) extends Actor with ActorLogging {

  import CatalogHandler._

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case AddBook(book) =>
      db.upsert(book.id, book) pipeTo sender()

    case GetBook(id) =>
      val _sender = sender()
      db.get(id).foreach {
        case Some(i) => _sender ! i
        case None => _sender ! BookNotFound
      }

    case RemoveBook(id) =>
      val _sender = sender()
      db.del(id).foreach {
        case i if i > 0 => _sender ! BookDeleted(id)
        case _ => _sender ! BookNotFound(id)
      }
  }

}