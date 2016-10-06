package chapter3

import akka.actor.Actor.Receive

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import chapter3.ask.ServiceActor.{Get, Server}
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}
import chapter3.patterns.ActorAsk._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

/**
  * Created by asattar on 2016-10-05.
  */
class ActorMessagePatternsSpec  extends TestKit(ActorSystem("test-system")) with ImplicitSender
  with FeatureSpecLike with GivenWhenThen with Matchers with BeforeAndAfterAll{

  implicit val timeout = 5 seconds

  val actor: ActorRef = TestActorRef(Props(new Actor{
    override def receive: Receive = {
      case x => sender ! x
    }
  }))

  val actor1: ActorRef = TestActorRef(Props(new Actor with ActorLogging{
    override def receive: Receive = {
      case "Ping" => {
        val replyTo = sender
        actor2 ask "Ping" onSuccess {
          case x => replyTo ! x
        }
      }
    }
  }))

  val actor2: ActorRef = TestActorRef(Props(new Actor with ActorLogging{
    override def receive: Receive = {
      case x => {
        actor3 forwardMessage x
      }
    }
  }), "actor2")

  val actor3: ActorRef = TestActorRef(Props(new Actor with ActorLogging{
    override def receive: Receive = {
      case "Ping"=> {
        sender ! "Pong"
      }
    }
  }), "actor3")


  val actor4: ActorRef = TestActorRef(Props(new Actor with ActorLogging{
    override def receive: Receive = {
      case "Ping"=> {
        actor5 ask "Pong" pipe(sender())
      }
    }
  }), "actor4")

  val actor5: ActorRef = TestActorRef(Props(new Actor with ActorLogging{
    override def receive: Receive = {
      case "Pong"=> {
        sender ! "Ping"
      }
    }
  }), "actor5")


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  feature("Test ask pattern") {
    scenario("Expect Pint") {
      Given("an actor")
      When("ask actor message Ping")
      val future  = actor.ask("Ping").mapTo[String]
      Then("the actor should send Server(\"Ping\")")
      val result = Await.result(future.mapTo[String], timeout)
    }
  }

  feature("Test forward pattern") {
    scenario("Given actor1 actor2 actor 3") {
      When("actor 1 is asked with message Ping")
      val future  = actor1.ask("Ping").mapTo[String]
      Then("the actor3 should send actor 1(\"Pong\")")
      val result = Await.result(future.mapTo[String], timeout)
    }
  }

  feature("Test pipe pattern") {
    scenario("Given actor4 actor5") {
      When("actor 4 is asked with message Ping and respnose is piped to actor 5")
      val future  = actor4 ! "Ping"
      Then("the actor5 should send message(\"Ping\")")
      expectMsg(timeout, "Ping")

    }
  }

}
