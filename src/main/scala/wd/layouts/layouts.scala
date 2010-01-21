package wd
package views

import xml.NodeSeq
import com.google.appengine.api.users.{UserService, User}
import gae._
import rest._
import scalaz.http.request.PUT

// TODO package object, couldn't get it to work last time
object layouts {
  val css = """
* {
  font-family: Helvetica
}
  """

  def main(us: UserService)(content: NodeSeq): NodeSeq = {
    <html>
      <head>
        <title>BEER ENGINE</title>
        <link rel="stylesheet" href="/main.css" />
      </head>
      <body>
        <h1>Beer Engine.</h1>
        { content }
        <div>{us.currentUser} : <a href={us.createLogoutURL("/")}>logout</a></div>
      </body>
    </html>
  }
}

object start {
  def index(breweries: Iterable[Brewery]): NodeSeq = {
    <div>
      <form action="/search" method="get">
          Search: <input type="text" name="qry" value=" "/>
      </form>
    </div>
    <hr />
    <div>
      <a href="/breweries/new">Add brewery</a> | <a href="/beers/new">Add beer</a>
    </div>

    <h3>Breweries</h3>
    <ul>
      { breweries.map { brewery =>
        <li><a href={ brewery.rr.show }>{ brewery.name }</a></li>
      } }
    </ul>
  }
}

object beers {
  def breweryChoice(choise: Either[Brewery, Iterable[Brewery]]) = {
    choise.fold(
      br => <input type="hidden" name="brewery" value={br.key.toString} />,
      breweries => (<select name="brewery">
                  { breweries flatMap (b => { <option value={b.key.toString}>{b.name}</option>  }) }
                </select>)
      )
  }

  def nnew(breweries: Either[Brewery, Iterable[Brewery]]): NodeSeq = {
    <h2>New Beer</h2>
            <div>
              <form action="/beers" method="post">
                <label for="name">Name:</label>
                  <input type="text" name="name"/>
                { breweryChoice(breweries) }
                  <input type="submit"/>
              </form>
            </div>
  }
}

object breweries {
  def show(brewery: Brewery, beers: Iterable[Beer]) = {
    <h2>{brewery.name}</h2>
    <hr />
    <ul>
      { beers.map { beer =>
      <li>{beer.name}</li>
    }}
    </ul>
    <hr />
    <div><small>
      <a href={brewery.rr.edit}>Edit</a> |
    <a href={"/beers/new?breweryKey=%s" format brewery.key.get.getName}>Add beer</a>
    </small></div>
  }

  def nnew: NodeSeq = {
    <h2>New Brewery</h2>
            <div>
              <form action="/breweries" method="post">
                <label for="name">Name:</label>
                  <input type="text" name="name"/>
                  <input type="submit"/>
              </form>
            </div>
  }

  def edit(brewery: Brewery, errors: List[(String, String)]): NodeSeq = {
    <h2>Changing {brewery.name}</h2>
    <div>
      <form action={brewery.rr.show} method="post">
        <input type="hidden" name="_method" value={PUT} />
        <input type="text" name="name" value={brewery.name} />
        <input type="submit" />
      </form>
     </div>
  }
}