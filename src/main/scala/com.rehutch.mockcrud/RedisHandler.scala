package com.rehutch.mockcrud

import redis.{ByteStringSerializer, RedisClient}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait Repo {

  def del(key: String): Future[Long]

  def upsert[V](key: String, value: V, expire: Option[Duration] = None)(implicit ev: ByteStringSerializer[V]): Future[Boolean]

  def get(key: String): Future[Option[String]]

}

abstract class RedisRepoImpl extends Repo {

  def db: RedisClient

  def del(key: String): Future[Long] = db.del(key)

  def upsert[V](key: String, value: V, expire: Option[Duration] = None)(implicit ev: ByteStringSerializer[V]): Future[Boolean] =
    db.set(key, value)

  def get(key: String): Future[Option[String]] = db.get[String](key)

}