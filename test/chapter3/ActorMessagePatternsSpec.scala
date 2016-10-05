package chapter3

import akka.actor.Actor.Receive

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import chapter3.ask.ServiceActor.{Get, Server}
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}
import chapter3.patterns.ActorMessagePatterns._

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


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  feature("Test no value in cache") {
    scenario("Expect reply from server") {
      Given("a service actor")
      When("a message is sent to get value for key")
      val future  = actor.ask("Ping").mapTo[String]
      Then("the actor should send Server(\"Ping\")")
      val result = Await.result(future.mapTo[String], timeout)
    }
  }

}
