package chapter3.ask

import akka.actor.{Actor, ActorPath, ActorRef, Props, Status}
import akka.actor.Actor.Receive

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceActor(cacheActorPath: ActorPath, serverActorPath:ActorPath) extends  Actor{

  import  ServiceActor._
  import CacheActor._
  import ServerActor._

  implicit val Tout = Timeout(5 seconds)
  val cacheActor = context.actorSelection(cacheActorPath)
  val serverActor = context.actorSelection(serverActorPath)

  override def receive: Receive = {
    case Get(s) => {
      val senderRef = sender()
      val cacheResult = cacheActor ? GetKey(s)
      cacheResult recoverWith({
        case e : Exception => {
          serverActor ? s
        }
      }) onComplete {
        case scala.util.Success(CacheResult(x:String)) => senderRef ! Cache(s)
        case scala.util.Success(ServerResult(x:String)) => {
          cacheActor ! StoreKey(s, x)
          senderRef ! Server(s)
        }
        case scala.util.Failure(t) => sender()! Status.Failure(Fail())
      }
    }
  }
}

object ServiceActor {

  case class Get(key: String)
  case class Cache(value: String)
  case class Server(value: String)
  case class Fail() extends  Exception

  def props(cacheActorPath: ActorPath, serverActorPath: ActorPath): Props = {
    Props(classOf[ServiceActor], cacheActorPath, serverActorPath)
  }
}

