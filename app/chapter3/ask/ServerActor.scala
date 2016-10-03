package chapter3.ask

import akka.actor.Actor
import akka.actor.Actor.Receive
import chapter3.ask.ServerActor.ServerResult


class ServerActor extends  Actor{
  override def receive: Receive = {
    case x: String => sender() ! ServerResult(x)
  }
}

object ServerActor {

  case class ServerResult(value: String)
}
