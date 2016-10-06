package chapter3.patterns

import java.util.concurrent.TimeoutException

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorContext, ActorRef, ActorSystem, Props, Status}

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
    }), "tempActor" + Math.random())

    p.future
  }

  def forwardMessage(x: Any)(implicit actorCtx: ActorContext) = actor.tell(x, actorCtx sender )
}

class FuturePipe(f: Future[Any]) {
  def pipe(actorRef: ActorRef)(implicit executionContext: ExecutionContext) = f onSuccess[Unit](PartialFunction[Any, Unit] (x => {
    actorRef ! x
  }))
}

object ActorMessagePatterns {

  implicit val actorMessagePatternAdapter = (actorRef: ActorRef) => new ActorMessagePatterns(actorRef)
  implicit val futurePipeAdapter = (f: Future[Any]) => new FuturePipe(f)

}
