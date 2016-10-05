package chapter3.patterns

import java.util.concurrent.TimeoutException

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}


/**
  * Created by asattar on 2016-10-05.
  */
class ActorMessagePatterns (actor: ActorRef) {

  def ask(x: Any)(implicit system: ActorSystem, timeout: FiniteDuration, executionCtx: ExecutionContext): Future[Any] = {

    val p = Promise[Any]()

    system.actorOf(Props(new Actor {

      override def receive: Receive = {
        case "timeout" => {
          p.tryFailure(new TimeoutException())
        }
        case x: Throwable => {
          p.tryFailure(x)
        }
        case x => {
          p.trySuccess(x)
        }

        context stop self
      }

      actor ! x

      context.system.scheduler.scheduleOnce(timeout, self, "timeout")
    }))

    p.future
  }
}

object ActorMessagePatterns {

  implicit val actorMessagePatternAdapter = (actorRef: ActorRef) => new ActorMessagePatterns(actorRef)

}
