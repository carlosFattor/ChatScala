package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.EventSource

class Application extends Controller {

  val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]
  
  def welcome = Enumerator.apply[JsValue](Json.obj(
    "user" -> "bot",
    "message" -> "Welcome! Enter a message and hit Enter!"
  ))
  
  def index = Action { implicit req =>
    Ok(views.html.index(routes.Application.chatFeed(), routes.Application.postMessage()))
  }

  def chatFeed = Action { req => 
    println("Someone connected: "+ req.remoteAddress)
    Ok.chunked(welcome >>> chatOut &> EventSource()).as("text/event-stream")
  }
  
  def postMessage = Action(parse.json) {
    req => chatChannel.push(req.body); Ok
  }
}
