// TODO
//
// Make this an actor and write a message handler for at least the
// set method.
//

import akka.actor.{Props, ActorRef, ActorSystem, Actor}

sealed trait TracerMessage
case class TraceScene(im: Image, of: String, scene: Scene, width: Int, height: Int) extends TracerMessage
case class TraceLine(sc: Scene, width: Int, height: Int, y: Int) extends TracerMessage
case class SetPixel(x: Int, y: Int, c: Colour) extends TracerMessage

class LineTracer extends Actor {
  override def receive = {
    case TraceLine(scene, width, height, y) =>
      scene.traceLine(width, height, y, sender);
      context.stop(self)
  }
}

class Coordinator extends Actor {
  override def receive = {
    case TraceScene(im, of, scene, width, height) =>
      image = im
      outfile = of
      waiting = im.width * im.height
      for (y <- 0 until height) {
        val lineTracer = context.system.actorOf(Props[LineTracer], name = s"tracer-$y")
        lineTracer ! TraceLine(scene, width, height, y)
      }
    case SetPixel(x, y, c) =>
      image(x, y) = c
      waiting -= 1
      if (waiting == 0) {
        image.print(outfile)
        context.system.shutdown()   
      }
    case _ =>
      println("can't understand message")
  }
  
  // Number of pixels we're waiting for to be set.
  var waiting = 0
  var outfile: String = null
  var image: Image = null
}
