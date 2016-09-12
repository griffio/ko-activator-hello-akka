import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Inbox
import akka.actor.Props
import akka.actor.UntypedActor
import scala.concurrent.duration.Duration
import java.io.Serializable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

sealed class Greet : Serializable {
  object Message : Greet()
  class WhoToGreet(val who: String) : Greet()
  class Greeting(val message: String) : Greet()
}

class Greeter(internal var greeting: String = "") : UntypedActor() {
  override fun onReceive(message: Any) {
    when (message) {
      is Greet.WhoToGreet -> greeting = "hello, ${message.who}"
      is Greet.Message -> sender.tell(Greet.Greeting(greeting), self)
      else -> unhandled(message)
    }
  }
}

fun main(args: Array<String>) {
  try {
    // Create the 'hello-akka' actor system
    val system = ActorSystem.create("hello-akka")

    // Create the 'greeter' actor
    val greeter = system.actorOf(Props.create(Greeter::class.java), "greeter")

    // Create the "actor-in-a-box"
    val inbox = Inbox.create(system)

    // Tell the 'greeter' to change its 'greeting' message
    greeter.tell(Greet.WhoToGreet("akka"), ActorRef.noSender())

    // Ask the 'greeter for the latest 'greeting'
    // Reply should go to the "actor-in-a-box"
    inbox.send(greeter, Greet.Message)

    // Wait 5 seconds for the reply with the 'greeting' message
    val greeting1 = inbox.receive(Duration.create(5, TimeUnit.SECONDS)) as Greet.Greeting
    println("Greeting: ${greeting1.message}")

    // Change the greeting and ask for it again
    greeter.tell(Greet.WhoToGreet("light-bend"), ActorRef.noSender())
    inbox.send(greeter, Greet.Message)
    val greeting2 = inbox.receive(Duration.create(5, TimeUnit.SECONDS)) as Greet.Greeting
    println("Greeting: ${greeting2.message}")

    // after zero seconds, send a Greet message every second to the greeter with a sender of the GreetPrinter
    val greetPrinter = system.actorOf(Props.create(GreetPrinter::class.java))
    system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), greeter, Greet.Message, system.dispatcher(), greetPrinter)
  } catch (ex: TimeoutException) {
    println("Got a timeout waiting for reply from an actor")
    println(ex)
  }
}

class GreetPrinter : UntypedActor() {
  override fun onReceive(message: Any) {
    if (message is Greet.Greeting)
      println(message.message)
  }
}