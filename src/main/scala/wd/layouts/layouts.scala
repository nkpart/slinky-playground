package wd

import xml.NodeSeq
import com.google.appengine.api.users.{UserService, User}
import gae.Gae._

// TODO package object, couldn't get it to work last time
object layouts {
  val css = """
* {
  font-family: Helvetica
}
  """

  def main(us : UserService): NodeSeq = {
    <html>
      <head>
        <title>BEER ENGINE</title>
        <link rel="stylesheet" href="/main.css" />
      </head>
      <body>
        <h1>Beer Engine.</h1>
          <slinky:yield/>
        <div>{us.currentUser} : <a href={us.createLogoutURL("/")}>logout</a></div>
      </body>
    </html>
  }
}

object partials {
  val index: NodeSeq = {
    <div>
      <form action="/search" method="get">
          <input type="text" name="qry" value=" "/>
      </form>
    </div>
            <div>
              <a href="/breweries/new">New Brewery</a>
              |
              <a href="/beers/new">New Beer</a>
            </div>
  }

  def newBeerForm(breweries: Iterable[Brewery]): NodeSeq = {
    <h2>New Beer</h2>
            <div>
              <form action="/beers" method="post">
                <label for="name">Name:</label>
                  <input type="text" name="name"/>
                <select name="brewery">
                  { breweries flatMap (b => { <option value={b.key.toString}>{b.name}</option>  }) }
                </select>
                  <input type="submit"/>
              </form>
            </div>
  }

  val newBreweryForm: NodeSeq = {
    <h2>New Brewery</h2>
            <div>
              <form action="/breweries" method="post">
                <label for="name">Name:</label>
                  <input type="text" name="name"/>
                  <input type="submit"/>
              </form>
            </div>
  }
}