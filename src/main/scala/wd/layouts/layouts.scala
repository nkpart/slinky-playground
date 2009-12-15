package wd

import xml.NodeSeq

// TODO package object, couldn't get it to work last time
object layouts {
  val main: NodeSeq = {
    <html>
      <head>
        <title>YO TITLE</title>
          <slinky:head/>
      </head>
      <body>
          <slinky:yield/>
      </body>
    </html>
  }
}
