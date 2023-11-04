import java.net.URL

import scala.annotation.tailrec
import scala.util.Random

object UrlShortener {

  case class UrlMapping(short: String, url: URL)

  private val mapping = scala.collection.concurrent.TrieMap[String, UrlMapping]()

  private val allowedChars = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toList

  def shortenUrl(url: URL): String = {
    val short = getNextUniqueKey
    mapping.put(short, UrlMapping(short, url))
    short
  }

  def getUrl(short: String): Option[UrlMapping] = {
    mapping.get(short)
  }

  private def getNextUniqueKey: String = {
    @tailrec
    def loop(current: String, length: Int): String = {
      if (!mapping.contains(current)) current
      else if (isFullyUsed(length)) loop(generateRandomString(length + 1), length + 1)
      else loop(generateRandomString(length), length)
    }

    val initialLength = math.max(1, math.ceil(math.log(mapping.size + 1) / math.log(allowedChars.size)).toInt)
    loop(generateRandomString(initialLength), initialLength)
  }

  private def isFullyUsed(length: Int): Boolean = {
    math.pow(allowedChars.size, length) == mapping.size
  }

  private def generateRandomString(length: Int): String = {
    @tailrec
    def loop(current: List[Char], remaining: Int): List[Char] = {
      if (remaining == 0) current
      else loop(allowedChars(Random.nextInt(allowedChars.size)) :: current, remaining - 1)
    }

    loop(Nil, length).mkString
  }
}

object Main extends App {
  import UrlShortener._

  val googleUrl = new URL("https://www.google.com")
  val short = shortenUrl(googleUrl)
  println("Short code: " + short)

  val short2 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short2)

  val short3 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short3)

   val short4 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short4)

  val short5 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short5)

   val short6 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short6)

  val short7 = shortenUrl(new URL("https://www.google.com/blabla"))
  println("Short code: " + short7)

  val retrieved = getUrl(short)
  println("Retrieved URL: " + retrieved.map(_.url))
}
