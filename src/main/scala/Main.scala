import com.bot4s.telegram.models.ChatId

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  val token = scala.util.Properties.envOrNone("TOKEN")
  val owner = scala.util.Properties.envOrNone("OWNER")
  (token, owner) match {
    case (None, None) => println("Token and owner not found")
    case (None, _) => println("Token not found")
    case (_, None) => println("Owner not found")
    case (Some(t), Some(o)) => {
      val bot = Bot(t,o.toLong)
      val eol = bot.run()
      println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
      scala.io.StdIn.readLine()
      bot.shutdown() // initiate shutdown
      // Wait for the bot end-of-life
      Await.result(eol, Duration.Inf) // ScalaJs wont't let you do this
    }
  }
}
