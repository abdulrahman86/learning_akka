package chapter3.ask

import akka.actor.{Actor, Status}
import akka.actor.Actor.Receive

import scala.collection.mutable



class CacheActor extends  Actor{

  import CacheActor._

  val map = mutable.Map.empty[String, String]

  override def receive: Receive = {
    case GetKey(key) => {
      map.get(key) match {
        case Some(value: String) => sender() ! CacheResult(value)
        case None => sender() ! Status.Failure(KeyNotFoundException())
      }
    }
    case StoreKey(key, value) => map.put(key, value)
  }
}

object CacheActor {
  case class GetKey(key: String)
  case class StoreKey(key: String, value: String)
  case class CacheResult(value: String)
  case class KeyNotFoundException() extends  Exception
}
