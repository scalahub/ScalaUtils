package org.sh.utils.cache

import scala.util.Random._
import org.sh.utils.Util._

import scala.collection.immutable.TreeMap
import scala.reflect.ClassTag
//import scala.collection.mutable.{Map => MMap}  
//import scala.collection.immutable.{Map => MMap}  

object Cache {

  type Time = Long
  //type PriKey = String
  //type ItemID = Any // (implicit ev: ¬¬[ItemID] <:< (String ∨ Int))
  
  object DecreasingOrder extends Ordering[Time] { def compare(a:Time, b:Time) = b compare a }

  type ItemMap[PriKey, T] = Map[PriKey, TreeMap[Time, List[T]]]
  type GetItemsFromDB[PriKey, T] = PriKey => List[T]  
  type GetItemFromDB[ID, PriKey, T <: Item[ID]] = (PriKey, ID) => Option[T]
  
  //type ID <: (String ∨ Long)
  type Item[ID] = {
    val time:Time
    val id:ID
  }
}

import Cache._

class ListCache[
  PriKey, ID, T <: Item[ID]
](cacheSize:Int, 
  cachingEnabled:Boolean, 
  getItemsFromDB:GetItemsFromDB[PriKey, T], preventDuplicateIDs:Boolean)(
  implicit ev: ¬¬[ID] <:< (String ∨ Long), c: ClassTag[T]) extends Cache[PriKey, ID, T](cacheSize, cachingEnabled, preventDuplicateIDs) {
  
  private def loadUserItems(priKey:PriKey) = {
    if (cachingEnabled) {
      val items = getItemsFromDB(priKey)
      //items foreach println
      val all = items.map{o => (o.time, o)}.take(cacheSize).groupBy(_._1).map{
        case (time, listTimeT) => time -> listTimeT.unzip._2 //.map(_._2)
      }.toSeq
      itemMap.synchronized{ itemMap += priKey -> TreeMap(all :_*)}
      //itemMap foreach println
    }
  }
  
  def getItems(priKey:PriKey, id:ID):List[T] = if (cachingEnabled) {
    if (invalidPriKeys.contains(priKey)) Nil else {
      if (debug) println(s"  [Cache: getLoadedItem] $priKey, $id")
      getCachedItem(priKey, id) match {
        case Some(list) => list
        case _ =>
          // avoid recursive call to prevent infinite loop in case priKey
          // does not exist or some other error
          // Thus, calling itemMap.get(priKey) again
          loadUserItems(priKey)
          getCachedItem(priKey, id) match {
            case Some(list) => list
            case _ =>
              invalidPriKeys.synchronized{invalidPriKeys += priKey}
              Nil
         }   
      }
    }
  } else Nil

  def getItems(priKey: PriKey): List[T] = 
    if (cachingEnabled) {
      if (invalidPriKeys.contains(priKey)) Nil else {
        if (debug) println(s"  [Cache: getItemsCached] $priKey")
        getCachedItems(priKey) match {
          case Some(list) => list
          case _ => 
            loadUserItems(priKey)
            getCachedItems(priKey) match {
              case Some(list) => list
              case _ =>
                invalidPriKeys.synchronized{invalidPriKeys += priKey}
                Nil
            } 
        }
      }
    } else Nil

  def getNumItems(priKey:PriKey): Long = if (cachingEnabled) {
    if (invalidPriKeys.contains(priKey)) 0 else {
      if (debug) println(s"  [Cache: getNumItemsCached] $priKey")
      getNumCachedItems(priKey) match {
        case Some(int) => int
        case _ => 
          loadUserItems(priKey)
          getNumCachedItems(priKey) match {
            case Some(int) => int
            case _ => 
              invalidPriKeys.synchronized{invalidPriKeys += priKey}
              0
          }
      }
    }
  } else 0
  
  def getItemsByDate(priKey:PriKey, to:Long, max:Int, offset:Long): List[T] = if (cachingEnabled) {
    if (invalidPriKeys.contains(priKey)) Nil else {
      if (debug) println(s"  [Cache: getItemsByDateCached] $priKey")
      getCachedItemsByDate(priKey, to, max, offset) match{
        case Some(list) => list
        case _ =>
          loadUserItems(priKey)
          getCachedItemsByDate(priKey, to, max, offset) match{
            case Some(list) => list
            case _ =>
              invalidPriKeys.synchronized{invalidPriKeys += priKey}
              Nil
          }
      }
    }
  } else Nil
  
  def getItemsByDate(priKey:PriKey, from:Long, to:Long, max:Int, offset:Long): List[T] = if (cachingEnabled) {
    if (invalidPriKeys.contains(priKey)) Nil else {
      if (debug) println(s"  [Cache: getItemsByDateCached] $priKey")
      getCachedItemsByDate(priKey, from, to, max, offset) match{
        case Some(list) => list
        case _ =>
          loadUserItems(priKey)
          getCachedItemsByDate(priKey, from, to, max, offset) match{
            case Some(list) => list
            case _ =>
              invalidPriKeys.synchronized{invalidPriKeys += priKey}
              Nil
          }
      }
    }
  } else Nil
  
}
class ItemCache[
  PriKey, ID, T <: Item[ID]
](cacheSize:Int, 
  cachingEnabled:Boolean, 
  getData:GetItemFromDB[ID, PriKey, T], preventDuplicateIDs:Boolean)(
  implicit ev: ¬¬[ID] <:< (String ∨ Long), c: ClassTag[T]) extends Cache[PriKey, ID, T](cacheSize, cachingEnabled, preventDuplicateIDs) {
  
  private def loadUserItem(priKey:PriKey, id:ID) = {
    if (cachingEnabled) {
      getData(priKey, id).map{i => 
        if (itemMap.get(priKey).isEmpty) itemMap.synchronized{itemMap += priKey -> TreeMap()}
        putItem(priKey, i)
      }
    }
  }
  
  def getItem(priKey:PriKey, id:ID):List[T] = if (cachingEnabled) {
    if (invalidPriKeys.contains(priKey)) Nil else {
      if (debug) println(s"  [Cache: getLoadedItem] $priKey, $id")
      accessMap += (priKey -> getTime)
      getCachedItem(priKey, id) match {
        case Some(i) if i.nonEmpty => 
          i
        case _ =>
          loadUserItem(priKey, id)
          getCachedItem(priKey, id) match {
            case Some(i) =>  // do we check if i is nonEmpty
              i
            case _ => 
              Nil
          }
      }
    }
  } else Nil
}
abstract class Cache[
  PriKey, ID, T <: Item[ID]
](var cacheSize:Int, var cachingEnabled:Boolean, preventDuplicateIDs:Boolean)(implicit ev: ¬¬[ID] <:< (String ∨ Long), c: ClassTag[T]) {
  
  protected var itemMap:ItemMap[PriKey, T] = Map() 
  protected var accessMap:Map[PriKey, Time] = Map()
  //def this (cacheSize:Int, getData:GetItemsFromDB[T]) = this(cacheSize, true, getData)
  
  def removeItems(priKey:PriKey, id:ID) = if (cachingEnabled) {
    itemMap.get(priKey) map {treeMap=>
      accessMap += (priKey -> getTime)
      itemMap.synchronized{
        itemMap += (
          priKey -> treeMap.collect{
            case (k, v) if v.exists(_.id == id) && v.size > 1 => 
              k -> v.filterNot(_.id == id)
            case (k, v) if v.forall(_.id != id) => 
              k -> v
          }
        )
      }
    }
  }  
  def removeItems(id:ID) = if (cachingEnabled) {
    val newItemMap = itemMap.collect{
      case (key, treeMap) =>
        val newMap = treeMap.map{
          case (time, list) =>
            val newList = list.filterNot{
              case item => item.id == id
            }
            (time -> newList)
        }
        key -> newMap
    }
    itemMap.synchronized{itemMap = newItemMap}
  }  
  
  @deprecated("Iterates over entire collection")
  def getItemsByID(id:ID) = if (cachingEnabled) {
    itemMap.flatMap{
      case (key, treeMap) =>
        treeMap.flatMap{
          case (time, list) => 
            list.find{
              case item => item.id == id
            }
        }
    }.toList
  } else Nil
  
  var debug = false

  def removePriKey(priKey:PriKey) = itemMap.synchronized{itemMap -= priKey}
  
  protected var invalidPriKeys:Set[PriKey] = Set()
  
  def clearInvalidUniqueIDs = invalidPriKeys.synchronized{invalidPriKeys = Set()}
  
  def getCachedItems(priKey:PriKey):Option[List[T]] = { // does not load if cache is empty
    itemMap.get(priKey).map {
      accessMap += (priKey -> getTime)
      treeMap=> treeMap.values.toList.flatten 
    }
  }

  def getCachedItemsByDate(priKey:PriKey, to:Time, max:Int, offset:Long):Option[List[T]] = { // does not load if cache is empty
    itemMap.get(priKey).map{treeMap =>
      
      accessMap += (priKey -> getTime)
      
      treeMap.filterKeys(k => k <= to).toArray.sortBy(x => -x._1).flatMap(_._2).drop(offset.toInt).take(max).toList
      
      //treeMap.filterKeys(k => k <= to).values.flatten.drop(offset.toInt).take(max).toList.reverse
    }
  }
  def getCachedItemsByDate(priKey:PriKey, from:Time, to:Time, max:Int, offset:Long):Option[List[T]] = { // does not load if cache is empty
    itemMap.get(priKey).map{treeMap =>
      accessMap += (priKey -> getTime)
      val x: Array[(Time, List[T])] = treeMap.filterKeys(k => k <= to && k >= from).toArray.sortBy(
        x => -x._1
      )
      x.flatMap(
        x => x._2
      ).drop(
        offset.toInt
      ).take(max).toList
      // treeMap.filterKeys(k => k <= to && k >= from).values.flatten.drop(offset.toInt).take(max).toList.reverse
    }
  }

  def getNumCachedItems(priKey:PriKey):Option[Int] = { // does not load if cache is empty
    itemMap.get(priKey).map{treeMap => 
      accessMap += (priKey -> getTime)
      treeMap.values.flatten.size
    }
  }

  def getCachedItem(priKey:PriKey, id:ID):Option[List[T]] = if (cachingEnabled) {
    itemMap.get(priKey).map{treeMap => 
      accessMap += (priKey -> getTime)
      treeMap.collect{
        case (time, itemList) if itemList.exists(_.id == id) => itemList
      }.flatMap{
        case itemList => itemList.filter(_.id == id)
      }.toList
    }  
  } else None

  // will only put if item with id (and additionally time if duplicateIDsAllowed) does NOT exist in cache, else will be silently ignored
  def putItem(priKey:PriKey, item:T) = if (cachingEnabled) {
    if (debug) println(s"  [Cache: putUserItem] $priKey, ${item.id}")
    itemMap.get(priKey).map{treeMap =>
      accessMap += (priKey -> getTime)
      // if already loaded in cache
      if ( 
        !treeMap.values.exists{list =>
          list.exists{i =>
            i.id == item.id && (i.time == item.time || preventDuplicateIDs)
          }
        } // if only item does not exist in cache
      ) {
        //println(" ----> "+item)
        val tmpNewMap = treeMap + (item.time -> (treeMap.get(item.time).getOrElse(Nil) :+ item))
        val newMap = if (tmpNewMap.values.map(_.size).sum <= cacheSize) tmpNewMap else {
          val (time, list) = tmpNewMap.last
          if (list.size > 1) {
            tmpNewMap + (time -> list.dropRight(1))
          } else tmpNewMap.dropRight(1)
        }
        itemMap.synchronized{itemMap += (priKey -> newMap)}
      } 
    }    
  }
  def removeAllItems = itemMap.synchronized{itemMap = Map()}
  doEvery10Mins{
    //val old = getTime - OneHour
    val old = getTime - TenMins
    
    val (newMap, oldMap) = accessMap.partition{
      case (priKey, time) => time <= old
    }
    accessMap = newMap
    oldMap.foreach{
      case (priKey, time) => time <= old
        removePriKey(priKey)
    }
    clearInvalidUniqueIDs
  }
  //  doEvery30Mins{
  //    clearInvalidUniqueIDs
  //  }
}


