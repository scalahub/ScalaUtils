package org.sh.utils

import scala.collection.immutable.Queue
import scala.collection.mutable.{Queue => MQueue}

object QueueUtil {
  val MaxQueueSize = 15
  class FiniteQueue[A](q: Queue[A]) {
    def enqueueFinite[B >: A](elem: B)(implicit maxSize:Int = MaxQueueSize): Queue[B] = {
      var ret = q.enqueue(elem)
      while (ret.size > maxSize) { ret = ret.dequeue._2 }
      ret
    }
  }
  implicit def queue2finitequeue[A](q: Queue[A]) = new FiniteQueue[A](q)

  class FiniteMQueue[A](q: MQueue[A]) {
    def enqueueFinite[B >: A](elem: A)(implicit maxSize:Int = MaxQueueSize): Unit = {
      q += elem
      while (q.size > maxSize) { q.dequeue }
    }
  }
  implicit def queue2finiteMqueue[A](q: MQueue[A]) = new FiniteMQueue[A](q)
}
