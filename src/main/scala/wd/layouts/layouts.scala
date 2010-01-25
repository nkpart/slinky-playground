package wd
package views

import xml.NodeSeq
import com.google.appengine.api.users.{UserService, User}
import gae._
import rest._
import scalaz._
import Scalaz._
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
  def index(breweries: Iterable[Keyed[Brewery]]): NodeSeq = {
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
        <li><a href={ brewery.rr.show }>{ brewery.value.name }</a></li>
      } }
    </ul>
  }
}

object beers {
  def breweryChoice(choise: Either[Keyed[Brewery], Iterable[Keyed[Brewery]]]) = {
    choise.fold(
      br => <input type="hidden" name="brewery_id" value={br.key.getId.toString} />,
      breweries => (<select name="brewery_id">
                  { breweries flatMap (b => { <option value={b.key.getId.toString}>{b.value.name}</option>  }) }
                </select>)
      )
  }

  def nu(breweries: Either[Keyed[Brewery], Iterable[Keyed[Brewery]]]): NodeSeq = {
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
  def show(brewery: Keyed[Brewery], beers: Iterable[Keyed[Beer]]) = {
    <h2>{brewery.value.name}</h2>
    <hr />
    <ul>
      { beers.map { beer =>
      <li>{beer.value.name}</li>
    }}
    </ul>
    <hr />
    <div><small>
      <a href={brewery.rr.edit}>Edit</a> |
    <a href={"/beers/new?brewery_id=%s" format brewery.key.getId}>Add beer</a>
    </small></div>
  }

  def nu(errors: List[(String, String)] = Nil): NodeSeq = {
    <h2>New Brewery</h2>
            <div>
              { ~((!errors.isEmpty).option(errors) map { errors => {
                <div id="errors">
                  <dl>
                    { errors map { case (k, v) => <dt>{k}</dt><dd>{v}</dd> } }
                  </dl>
                </div>
              }}) }
              <form action="/breweries" method="post">
                <label for="name">Name:</label>
                  <input type="text" name="name"/>
                  <input type="submit"/>
              </form>
            </div>
  }

  def edit(brewery: Keyed[Brewery], errors: List[(String, String)]): NodeSeq = {
    <h2>Changing {brewery.value.name}</h2>
    <div>
      <form action={brewery.rr.show} method="post">
        <input type="hidden" name="_method" value={PUT} />
        <input type="text" name="name" value={brewery.value.name} />
        <input type="submit" />
      </form>
     </div>
  }
}