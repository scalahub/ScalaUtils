package org.sh.utils

import java.io._
import java.sql.Date
import java.text.SimpleDateFormat

import org.sh.utils.encoding.Base64
import org.sh.utils.file.TraitPlaintextFileProperties
//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorRef, ActorSystem, Cancellable}

import scala.collection.mutable.{Map => MMap, Set => MSet}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
//import scala.util.Try

//import akka.actor.Cancellable
object TaskScheduler {
  val actorSystem = ActorSystem()
  val scheduler = actorSystem.scheduler
  val task = new Runnable { def run() { println("Hello") } }
  implicit val executor = actorSystem.dispatcher
  import scala.collection.mutable.{Map => MMap}
  
  def doOnce(fn: => Unit, period:Long) = {
    val id = ctr.getAndIncrement
    val cancellable = scheduler.scheduleOnce(period.milliseconds){
      scheduledTasks.synchronized{scheduledTasks -= id}
      fn
    }
    scheduledTasks += id -> cancellable
    cancellable
  }
  def doRegularly(fn: => Unit, periodMillis:Long) = {
    val cancellable = scheduler.schedule(0.seconds, periodMillis.milliseconds)(fn)
    scheduledTasks += ctr.getAndIncrement -> cancellable
    cancellable
  }
  
  def doWhile(fn: => Unit, whileFn: => Boolean, period:Long) {
    if (whileFn) {
      fn
      doOnce(doWhile(fn, whileFn, period), period)
    }
  } 
  
  
  // following does not start immediately but after period millis.. Thats why "Tail"
  def doWhileTail(fn: => Unit, whileFn: => Boolean, period:Long) =
    doOnce(doWhile(fn, whileFn, period), period)
  
  def doWhileTailOld(fn: => Unit, whileFn: => Boolean, period:Long) {
    doOnce(if (whileFn){
             fn
             doWhileTailOld(fn, whileFn, period)
           }, period)
  } 
  val ctr = new AtomicLong(0L)
  val scheduledTasks = MMap[Long, Cancellable]()
  
  sys.addShutdownHook{
    try {
      scheduledTasks.foreach{
        case (id, task) => if (!task.isCancelled) task.cancel
      }
    } catch {
      case a:Throwable => a.printStackTrace
    }
  }
  
}

object UtilConfig extends TraitPlaintextFileProperties {
  val propertyFile = "CommonUtil.properties"
  var debug = read("debug", false)
}
object Util {
  def rand = scala.util.Random.nextInt.abs
  def debug = UtilConfig.debug
  def doInvoke[T](mSet:MSet[(Int, T => Unit)], data:T) = {
    val (negs, pos) = mSet.partition(_._1 < 0) 
    negs.foreach{case (_, f) => tryIt(f(data))} // give higher priority to neg
    pos.foreach{case (_, f) => doOnce(tryIt(f(data)), 0)} // give lower priority to pos    
  }
  def doInvokeNoTry[T](mSet:MSet[(Int, T => Unit)], data:T) = {
    val (negs, pos) = mSet.partition(_._1 < 0) 
    negs.foreach{case (_, f) => f(data)} // give higher priority to neg
    pos.foreach{case (_, f) => f(data)} // give lower priority to pos    
  }
  def addUniqueMMap[T](key:String, m:MMap[String, T], t:T) {
    if (!m.contains(key)) m.synchronized{m += (key -> t)}
  }
  //  def addUniqueMMap[T](key:String, m:MMap[String, T], t:T) {
  //    if (m.contains(key)) throw new Exception(s"Map already contains key: $key")
  //    else m.synchronized{m += (key -> t)}
  //  }

  def addUnique[T](id:Int, l:MSet[(Int, T)], t:T):Unit = if (!l.exists(x => x._1 == id)) l += ((id, t))
  def addNonUnique[T](l:MSet[(Int, T)], t:T):Unit = {
    var r = 1
    while(l.exists(_._1 == r)) {
      r = rand
    }
    addUnique(r, l, t)
  }
  val OneSec = 1000L
  val OneMin = OneSec*60
  val FiveMins = OneMin*5
  val TenMins = FiveMins*2
  val FifteenMins = FiveMins*3
  val ThirtyMins = FifteenMins*2
  val OneHour = ThirtyMins*2
  val OneDay = OneHour*24
  val OneMonth = OneDay*30
  val OneYear = OneDay*365
  val OneWeek = OneDay*7
  
  def printdbg(s:String) = if (debug) println(s)
  def getTime = System.currentTimeMillis
  def doWhile(fn: => Unit, whileFn: => Boolean, period:Long) = TaskScheduler.doWhile(fn, whileFn, period)
  def doWhileTail(fn: => Unit, whileFn: => Boolean, period:Long) = TaskScheduler.doWhileTail(fn, whileFn, period)
  def doOnce(fn: => Unit, periodMillis:Long) = TaskScheduler.doOnce(fn, periodMillis)
  def doOnceNow(fn: => Unit) = doOnce(fn, 0)
  def doRegularly(fn: => Unit, periodMillis:Long) = TaskScheduler.doRegularly(fn, periodMillis)
  def doHourly(f: => Unit) = doRegularly(f, 1000L*60*60)  // every hour
  def doEvery30Mins(f: => Unit) = doRegularly(f, 1000L*60*30)  // every 1/2 hr
  def doEvery15Mins(f: => Unit) = doRegularly(f, 1000L*60*15)  // every 15 mins
  def doEvery10Mins(f: => Unit) = doRegularly(f, 1000L*60*10)  // every 10 mins
  def doEvery5Mins(f: => Unit) = doRegularly(f, 1000L*60*5)  // every 5 mins
  def doEveryMin(f: => Unit) = doRegularly(f, 1000L*60)  // every 1 min
  def toTimeString(millis:Long) = {
    val (second, minute, hour) = ((millis / 1000) % 60, (millis / (1000 * 60)) % 60, (millis / (1000 * 60 * 60)) % 24)
    "%02d:%02d:%02d:%d".format(hour, minute, second, millis % 1000)
  }
  //  def toDateString(millis:Long) = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS").format(new Date(millis))  
  def toDateString(millis:Long) = new SimpleDateFormat("yyyy MMM dd HH:mm:ss").format(new Date(millis))  

//    import sun.misc.BASE64Encoder
//    val encoder = new BASE64Encoder
//  val d = java.security.MessageDigest.getInstance("SHA-256");
  final val englishAlphaBet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  def sha256Bytes(b:Array[Byte]):String = {
    val d = java.security.MessageDigest.getInstance("SHA-256");
//    java.util.Base64.
    Base64.encodeBytes(d.digest(b))
  }
  def sha256Bytes2Bytes(b:Array[Byte]):Array[Byte] = {
    val d = java.security.MessageDigest.getInstance("SHA-256");
    d.digest(b)
  }
  def sha256(s:String):String = sha256Bytes(s.getBytes)
  def sha256Small(s:String):String = sha256(s).substring(0, 16)
  def shaSmall(s:String):String = (cleanString(sha256(s))  match { // removes non-alphanumeric chars such as / and = 
    case x if "0123456789".contains(x(0)) => englishAlphaBet(x(0).toString.toInt)+x.substring(1)
    case x => x
  }).substring(0, 16)
  def cleanString(s:String) = if (s == null) "" else s.replaceAll("[^\\p{L}\\p{Nd}]", "") // removes non-alphanumeric chars
  val random = new scala.util.Random(new java.security.SecureRandom()) 
  
  def randomString(alphabet: String)(n: Int): String = // Generate a random string of length n from the given alphabet
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
  def randomAlphanumericString(n: Int) =     // Generate a random alphabnumeric string of length n
    randomString(englishAlphaBet)(n)
  def isFromAlphabet(alphaBet:String)(toCheck:String) = toCheck forall (alphaBet.contains(_))
  def isNonAlphaNumeric(s:String) = s.matches("^.*[^a-zA-Z0-9 ].*$");
  def stopAll(param: {def stop: Unit}*) = param.foreach(x => x.stop)
  def extractStrings(s:String):Array[String] = if (s != null) s.split(':').map(_.trim) else Array()
  def getIntList (t:Traversable[_]) = List.range(0, t.size)
  //def getIntList (a:Array[_]) = List.range(0, a.size)

  
    /** Used for generating random bytes */
  def randomBytes(numBytes:Int)= {
    var bytes = new Array[Byte](numBytes)
    scala.util.Random.nextBytes(bytes) // not secure
    bytes
  }
  
  def round(d:Double, decimals:Int) = 
    BigDecimal(d).setScale(decimals, BigDecimal.RoundingMode.HALF_UP)
  def round(s:String, decimals:Int) = 
    BigDecimal(s).setScale(decimals, BigDecimal.RoundingMode.HALF_UP).toString
  
  def compareBytes(a:Array[Byte], b:Array[Byte]) =
    if (a.length == b.length) { // length must be equal
      var res = true
      Util.getIntList(a).foreach (i => res = res && a.apply(i)== b.apply(i))
      // above line computes the AND of the comparison of individual bytes in this and a
      res
    } else false

  def readUserInput(prompt:String) = {
    val br = new BufferedReader(new InputStreamReader(System.in))
    print (prompt)
    br.readLine()
  }
  /**
   * Generic function
   * it invokes func and if an exception, returns the result of func2, otherwise it returns the result of func. Both methods output generic type T 
   */
  def getOrElse[T](func : => T, func2: => T) =
    getOrNone(func).getOrElse(func2)

  // following method not needed so commented out.
  //  def getOrElse[T](o:Option[Any], default: T): T = {
  //    o.getOrElse(None) match {
  //      case t:T => t
  //      case _ => default
  //    }
  //  }

  /**
   * Generic function
   * Takes as input func and func1, which are two methods. Both methods output generic type T and take no input.
   * This method invokes func and if an exception occurs, it returns None, otherwise it returns the result of func enclosed in a "Some" object. Therefore this method returns Option[T]
   */
  def getOrNone[T](func : => T) = {
    try{
      Some(func)
    } catch {
      case _ : Throwable => None
    }
  }
  def tryIt(f: => Any) = try f catch { case t:Throwable => 
    if (debug) 
      t.printStackTrace 
    else println(" [TRYIT]: "+t.getClass.getName+":"+t.getMessage)
  }
  def getOrElseExit[T](func:()=>T) = try { func.apply } catch { case e:Exception =>
      println (e.getMessage+"\nProgram will exit")
      if (debug) e.printStackTrace else println("Error:"+e.getMessage)
      System.exit(1)
  }

  /**
   * Used for closing DB connections implicitly.
   * Also used for writing / reading to files
   * Code is borrowed - need to check correctness.
   * @author From the book "Beginning Scala"
   */
  def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B =
  try { f(param) } finally { param.close() }

  def serialize(o:Object) = {
    val baos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(o)
    val bytes = baos.toByteArray
    Base64.encodeBytes(bytes)      
  }
  def deserialize(base64:String) = try {
    val bytes = Base64.decode(base64)
    val bais = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bais)
    ois.readObject 
  } catch {
      case a:Throwable => 
        if (debug) a.printStackTrace
        if (debug) println (" [DEBUG Deserialize]: "+base64)
        // throw a
        "Error: "+a.getMessage
  }

  /**
   * Takes in a list of methods mapping from Unit to B. 
   * Tries the first method, and if exception, tries the second (otherwise returns), and so on until no exception or no more methods.
   * Returns the output of the final method called either B or output of last method (which can be B or an exception).
   * 
   */
  type unitToT[T] = ()=>T
  def trycatch[B](list:List[unitToT[B]]):B = list.size match {
    case i if i > 1 => 
      try {
        list.head()
      } catch {
        case t:Any => trycatch(list.tail)
      }
    case 1 => 
      list(0)()
    case _ => throw new Exception("call list must be non-empty")
  }

  def trycatchParOld[B](list:List[unitToT[B]], timeOut:Long = 10000):B = { // 10000 = 10 seconds
    if (list.isEmpty) throw new Exception("call list must be non-empty")
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    import scala.language.postfixOps
    val p = Promise[B]
    list.map{fn => 
      Future{
       fn()
      }
    }.map{_.onComplete{
	case r@Success(_) => p.tryComplete(r)
	case r@Failure(_) => 
      }
    }
    Await.result(p.future, timeOut millis)
  }
  def trycatchPar[B](list:List[unitToT[B]]) = trycatch(list)
  
  def trycatchParNew[B](list:List[unitToT[B]], timeOut:Long = 10000):B = { // 10000 = 10 seconds
    if (list.isEmpty) throw new Exception("Call list must be non-empty")
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    import scala.language.postfixOps
    val p = Promise[B]
    val cancellables = list.map{fn => 
      Cancellable{
       fn()
      }
    }
    cancellables.map{
      _.future.onComplete{
	case r@Success(_) => p.tryComplete(r)
	case r@Failure(_) => 
      }
    }
    val res = Await.result(p.future, timeOut millis)
    cancellables.foreach{c => if (!c.future.isCompleted) c.cancel}
    res
  }

  // https://stackoverflow.com/a/39986418/243233
  import java.util.concurrent.{Callable, FutureTask}

  import scala.concurrent.{ExecutionContext, Promise}
  import scala.util.Try

  class Cancellable[T](executionContext: ExecutionContext, todo: => T) {
    private val promise = Promise[T]()

    def future = promise.future

    private val jf: FutureTask[T] = new FutureTask[T](
      new Callable[T] {
        override def call(): T = todo
      }
    ) {
      override def done() = promise.complete(Try(get()))
    }

    def cancel(): Unit = jf.cancel(true)

    executionContext.execute(jf)
  }

  object Cancellable {
    def apply[T](todo: => T)(implicit executionContext: ExecutionContext): Cancellable[T] =
      new Cancellable[T](executionContext, todo)
  }

  
  /** used for DB read */
  import scala.collection.mutable.ListBuffer
  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    val ret = new ListBuffer[T]
    while(test) ret += block
    ret.toList
  }
  
  def sendActorRespOrException[T](s:ActorRef, f: => T) = {
    try {
      val result = f
      s ! result
    } catch {
      case a:Any => 
        println(s" [sendActorRespOrException] Exception encountered. Sending failure: [${a.getClass}][${a.getMessage}]")
        //println(s"  [Actor error stacktrace start] $sender: "+a.getMessage)
        //a.printStackTrace      
        //println(s"  [Actor error stacktrace end] $sender")
        s ! akka.actor.Status.Failure(a) // Alert the sender of the failure
        // throw a // Alert any supervisor actor of the failure
    }
  }
  import akka.actor._
  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.Await

  def makeQueryToActor[T, S](actor:ActorRef, t:T)(implicit timeout:Timeout):S = {
    val future = actor ? t
    Await.result(future, timeout.duration).asInstanceOf[S]
  }

  // used to ensure that two types are not the same
  //https://stackoverflow.com/a/6944070/243233
  trait =!=[A, B]
  implicit def neq[A, B] : A =!= B = null
  // This pair excludes the A =:= B case
  implicit def neqAmbig1[A] : A =!= A = null
  implicit def neqAmbig2[A] : A =!= A = null    
  
  // used to ensure that type X is either A or B
  //  http://milessabin.com/blog/2011/06/09/scala-union-types-curry-howard/
  type ¬[A] = A => Nothing
  type ¬¬[A] = ¬[¬[A]]
  type ∨[T, U] = ¬[¬[T] with ¬[U]]

  private val randStringSize = 25
  def getID = randomAlphanumericString(randStringSize)

}
