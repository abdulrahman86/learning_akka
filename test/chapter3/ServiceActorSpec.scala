package chapter3

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import chapter2.PongActor
import chapter3.ask.{ServiceActor, CacheActor, ServerActor}
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}

/**
  * Created by asattar on 2016-10-03.
  */
class ServiceActorSpec extends TestKit(ActorSystem("test-system")) with ImplicitSender
  with FeatureSpecLike with GivenWhenThen with Matchers with BeforeAndAfterAll {

  import chapter3.ask.ServiceActor._

  val cacheActorRef = TestActorRef(Props(classOf[CacheActor]))
  val serverActorRef = TestActorRef(Props(classOf[ServerActor]))
  val serviceActorRef = TestActorRef(ServiceActor.props(cacheActorRef.path, serverActorRef.path))
  val Tout = 2 seconds
  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  feature("Test no value in cache") {
    scenario("Expect reply from server") {
      Given("a service actor")
      When("a message is sent to get value for key")
      serviceActorRef ! Get("Ping")
      Then("the actor should send Server(\"Ping\")")
      expectMsg(Tout, Server("Ping"))
    }
  }

  feature("Test value from cache") {
    scenario("Expect reply from server") {
      Given("a service actor")
      When("a message is sent to get value for key")
      serviceActorRef ! Get("Pong")
      Then("the actor should send Server(\"Pong\")")
      expectMsg(Tout, Server("Pong"))
      serviceActorRef ! Get("Pong")
      Then("the actor should send Cache(\"Pong\")")
      expectMsg(Tout, Cache("Pong"))
    }
  }
}
