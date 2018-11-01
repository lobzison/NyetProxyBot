import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models.{InputFile, Message, User}

final case class Bot(token: String, owner: Long)
  extends TelegramBot with Polling with Commands{
  val client = new ScalajHttpClient(token)
  val startCommand = "/start"
  val startText = scala.util.Properties.envOrElse("START_TEXT",
    "Привет! Если у тебя есть вопрос или предложение - пиши его сюда. Я отвечу в ближайшее время.")

  onCommand("/start") { implicit msg =>
    reply(startText)
  }

  def isCommand(text: String): Boolean = text == startCommand

  def isOwner(msg: Message):Boolean = msg.chat.id==owner

  onMessage{implicit msg =>
    if (isOwner(msg))
      sendToUser(msg)
    else
      forwardToOwner(msg)
  }

  def sendToUser(message: Message): Unit = {
    message.replyToMessage match {
      case Some(x) => x.forwardFrom.foreach(y => anonymize(message ,y).foreach(request(_)))
      case None => request(SendMessage(owner, "Reply to message to send it"))
    }
  }

  def anonymize(message: Message, user: User): Option[Request[Message]] = {
    val userId = user.id.toLong
    val text: Option[_ <:Request[Message]] =
      message.text.map(x => SendMessage(userId, x))
    val photo: Option[_ <: Request[Message]] =
      message.photo.flatMap(l => l.reverse.headOption.map(photo => SendPhoto(userId, InputFile(photo.fileId))))
    text.orElse(photo)
  }


  def forwardToOwner(message: Message): Unit = {
    message.text match {
      case Some(x) if x == startCommand => ()
      case _ => request(ForwardMessage(owner, message.chat.id, messageId = message.messageId))
    }
  }


  def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] = (a, b) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(x), Some(y)) => Some(f(x, y))
  }
}