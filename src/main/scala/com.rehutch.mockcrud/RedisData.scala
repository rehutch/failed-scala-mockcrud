package com.rehutch.mockcrud

import java.net.URI


trait RedisData {
  val redisUrl = URI.create("http://127.0.0.1:1337")
}
