package wd
package views

import xml.NodeSeq
import com.google.appengine.api.users.{UserService, User}
import gae._
import rest._
import scalaz._
import Scalaz._
import scalaz.http.request._
import scapps._

trait RestHelpers {
  def createForm[T](xml: NodeSeq)(implicit resourced: Resourced[T]): NodeSeq = {
    <form method="post" action={resourced.index}>
      { xml }
    </form>
  }
  
  def editForm[T](t: T)(xml: NodeSeq)(implicit resourced: Resourced[T]): NodeSeq = {
    <form method="post" action={t.rr.show}>
      <input type="hidden" name="_method" value={PUT} />
      { xml }
    </form>
  }
}

// TODO package object, couldn't get it to work last time
object layouts {

  def main(us: UserService)(content: NodeSeq): NodeSeq = {
    <html>
      <head>
        <title>BEER ENGINE</title>
        <link rel="stylesheet" href="/main.css" />
      </head>
      <body>
        <div id="strip">
          {us.currentUser} : <a href={us.createLogoutURL("/")}>logout</a>
        </div>
        <div id="content">
        <h1 id="heading">Beer Engine<a href="/">.</a></h1>
        <div id="search_box">
          <form action="/search" method="get">
              <div><input id="search" type="text" name="qry" value=""/></div>
              <div><input id="search_submit" type="Submit" value="Search" /></div>
          </form>
        </div>
        <hr />
        { content }
        </div>
      </body>
    </html>
  }
}

object start extends RestHelpers {
  val menu = {
    <div>
      <a href="/breweries/new">New brewery</a> | 
      <a href="/beers/new">New beer</a> |
      <a href="/config">Config</a>
    </div>
  }
  
  def index(breweries: Iterable[Keyed[Brewery]]): NodeSeq = {
    <div>
    { menu }
    <h3>Breweries</h3>
    <ul>
      { breweries.map { brewery =>
        <li><a href={ brewery.rr.show }>{ brewery.value.name }</a></li>
      } }
    </ul>
    </div>
  }
  
  def config(styles: List[Style]) = {
    <table border="0" cellpadding="0">
      <thead>
        <th>Styles</th>
      </thead>
      <tbody>
        <tr>
          <td>
        { styles map { s =>
          <div>{s}</div>
        }}
          </td>
        </tr>
      </tbody>
    </table>
  }
}

object beers extends RestHelpers {
  def breweryChoice(choise: Either[Keyed[Brewery], Iterable[Keyed[Brewery]]]): NodeSeq = {
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
              { createForm[Keyed[Beer]] {
                  <div><label for="name">Name:</label>
                  <input type="text" name="name"/>
                  { breweryChoice(breweries) }
                  <a href="/">Cancel</a> <input type="submit"/>
                  </div>
              }}
            </div>
  }
}

object breweries extends RestHelpers {
  def show(brewery: Keyed[Brewery], beers: Iterable[Keyed[Beer]]) = {
    <h2>{brewery.value.name}</h2>
    <h3>{brewery.value.country.value}</h3>
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

  def newBrewery(errors: List[(String,String)]) = {
    val base = scapps.R.request.formBase(errors)
    base.form("/breweries", POST) {
      <p>{
        base.text("name", value = Some("hi ther"))
      }</p>
    }
  }
  
  def nu(base: FormBase): NodeSeq = {
    <h2>New Brewery</h2>
            <div>
              { ~((!base.errors.isEmpty).option(base.errors) map { errors => {
                <div id="errors">
                  <dl>
                    { errors map { case (k, v) => <dt>{k}</dt><dd>{v}</dd> } }
                  </dl>
                </div>
              }}) }
              {base.form("/breweries", POST) {
                <dl>
                  <dt><label for="name">Name:</label></dt>
                  <dd>{base.text("name")}</dd>
                  <dt><label for="name">Country:</label></dt>
                  <dd>{base.text("country")}</dd>
                </dl>
                <a href="/">Cancel</a> <input type="submit"/>
              }}
            </div>
  }

  def edit(brewery: Keyed[Brewery], errors: List[(String, String)]): NodeSeq = {
    <h2>Changing {brewery.value.name}</h2>
    <div>
      {editForm(brewery) {
        <input type="text" name="name" value={brewery.value.name} />
        <input type="text" name="country" value={brewery.value.country.value} />
        <input type="submit" />
      }}
     </div>
  }
}